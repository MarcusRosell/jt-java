package uk.ac.liv.jt.codec;

import java.io.IOException;

import uk.ac.liv.jt.format.ByteReader;

/**
 * Decoder for the Int32 Compressed Data Packet of JT 9 and later ("Int32CDP2" in JT 9,
 * "Int32CDP" in JT 10, which supersedes the JT 8 format implemented in {@link Int32Compression}).
 * <p>
 * A packet starts with the number of values, followed (when not 0) by the CODEC type: Null,
 * Bitlength, Arithmetic, or the Chopper and Move-to-Front pseudo-CODECs that contain nested packets.
 * The CodeText is a sequence of 32-bit words read most significant bit first.
 */
public class Int32CDP2
{
	public static final int CODEC_NULL = 0;
	public static final int CODEC_BITLENGTH = 1;
	public static final int CODEC_ARITHMETIC = 3;
	public static final int CODEC_CHOPPER = 4;
	public static final int CODEC_MTF = 5;

	/** Type of value predictor, applied on the decoded residual values */
	public enum Predictor
	{
		/** Predicts as last value */
		LAG1,
		/** Predicts as last value, but uses xor instead of subtract */
		XOR1,
		/** No prediction */
		NULL
	}

	/** maximal nesting of packets (out-of-band data, chopped fields) */
	private static final int MAX_DEPTH = 16;

	private Int32CDP2()
	{}

	/**
	 * Reads a packet and returns the values, after applying the predictor.
	 */
	public static int[] readVecI32( ByteReader reader, Predictor predictor ) throws IOException
	{
		return unpackResiduals( readPacket( reader, 0 ), predictor );
	}

	private static int[] readPacket( ByteReader reader, int depth ) throws IOException
	{
		if ( depth > MAX_DEPTH ) {
			throw new IOException( "Int32 compressed data packet nested too deeply" ); //$NON-NLS-1$
		}
		int valueCount = reader.readI32();
		if ( valueCount <= 0 ) {
			return new int[0];
		}
		int codec = reader.readU8();
		switch ( codec ) {
			case CODEC_CHOPPER: {
				int chopBits = reader.readU8();
				if ( chopBits == 0 ) {
					// nothing chopped, the values are in a nested packet
					return readPacket( reader, depth + 1 );
				}
				int bias = reader.readI32();
				int spanBits = reader.readU8();
				int[] msb = readPacket( reader, depth + 1 );
				int[] lsb = readPacket( reader, depth + 1 );
				if ( msb.length != valueCount || lsb.length != valueCount ) {
					throw new IOException( "Chopped data size mismatch" ); //$NON-NLS-1$
				}
				int[] values = new int[valueCount];
				int shift = spanBits - chopBits;
				for ( int i = 0; i < valueCount; i++ ) {
					values[i] = (lsb[i] | (msb[i] << shift)) + bias;
				}
				return values;
			}
			case CODEC_MTF:
				// The reference does not document how the window offsets encode the new values:
				// not decoded rather than risking wrong data
				throw new IOException( "Move-to-front Int32 compressed data packets are not supported" ); //$NON-NLS-1$
			case CODEC_NULL:
			case CODEC_BITLENGTH:
			case CODEC_ARITHMETIC:
				break;
			default:
				throw new IOException( "Unsupported Int32 compressed data packet CODEC: " + codec ); //$NON-NLS-1$
		}

		int codeTextLength = reader.readI32();
		int[] codeText = new int[(int)((codeTextLength + 31L) / 32)];
		for ( int i = 0; i < codeText.length; i++ ) {
			codeText[i] = reader.readI32();
		}

		switch ( codec ) {
			case CODEC_NULL: {
				int[] values = new int[valueCount];
				System.arraycopy( codeText, 0, values, 0, Math.min( valueCount, codeText.length ) );
				return values;
			}
			case CODEC_BITLENGTH:
				return decodeBitLength( valueCount, new CodeTextReader( codeText, codeTextLength ), reader.MAJOR_VERSION >= 10 );
			default: {
				ProbContext context = ProbContext.read( reader );
				// the out-of-band values replace the escape symbols. From JT 10 they are only
				// stored when the context has an escape symbol
				int[] oobValues = reader.MAJOR_VERSION < 10 || context.hasEscape() ? readPacket( reader, depth + 1 ) : new int[0];
				return context.decodeArithmetic( valueCount, codeText, codeTextLength, oobValues );
			}
		}
	}

	/**
	 * Decodes the Bitlength CODEC.
	 *
	 * @param jt10 true for the JT 10 variant, where the min, max and mean values are "nibbled"
	 *            and the block header field widths are fixed
	 */
	private static int[] decodeBitLength( int valueCount, CodeTextReader bits, boolean jt10 ) throws IOException
	{
		int[] values = new int[valueCount];
		if ( bits.readUnsigned( 1 ) == 0 ) {
			// fixed width: all the values are stored with the bit width of max - min
			int min;
			int max;
			if ( jt10 ) {
				min = bits.readNibbled();
				max = bits.readNibbled();
			}
			else {
				int minBits = bits.readUnsigned( 6 );
				int maxBits = bits.readUnsigned( 6 );
				min = bits.readSigned( minBits );
				max = bits.readSigned( maxBits );
			}
			int width = bitSize( max - min );
			for ( int i = 0; i < valueCount; i++ ) {
				values[i] = bits.readUnsigned( width ) + min;
			}
		}
		else {
			// variable width: runs of values whose field width is adjusted between runs
			int mean;
			int changeWidthBits;
			int runLengthBits;
			if ( jt10 ) {
				mean = bits.readNibbled();
				changeWidthBits = 4;
				runLengthBits = 4;
			}
			else {
				mean = bits.readSigned( 32 );
				changeWidthBits = bits.readUnsigned( 3 );
				runLengthBits = bits.readUnsigned( 3 );
			}
			int maxDecrement = -(1 << (changeWidthBits - 1));
			int maxIncrement = (1 << (changeWidthBits - 1)) - 1;
			int width = 0;
			int i = 0;
			while ( i < valueCount ) {
				int delta;
				do {
					delta = bits.readSigned( changeWidthBits );
					width += delta;
				}
				while ( delta == maxDecrement || delta == maxIncrement );
				int runLength = bits.readUnsigned( runLengthBits );
				if ( width < 0 || width > 32 || i + runLength > valueCount ) {
					throw new IOException( "Corrupt bitlength CODEC data" ); //$NON-NLS-1$
				}
				for ( int k = 0; k < runLength; k++ ) {
					values[i++] = bits.readSigned( width ) + mean;
				}
			}
		}
		return values;
	}

	/** Number of bits necessary to encode the unsigned value */
	private static int bitSize( int x )
	{
		return 32 - Integer.numberOfLeadingZeros( x );
	}

	/**
	 * Applies the predictor to the residual values. The first four values are primers stored as is.
	 */
	public static int[] unpackResiduals( int[] residuals, Predictor predictor )
	{
		if ( predictor == Predictor.NULL ) {
			return residuals;
		}
		int[] values = new int[residuals.length];
		for ( int i = 0; i < residuals.length; i++ ) {
			if ( i < 4 ) {
				values[i] = residuals[i];
			}
			else if ( predictor == Predictor.XOR1 ) {
				values[i] = residuals[i] ^ values[i - 1];
			}
			else {
				values[i] = residuals[i] + values[i - 1];
			}
		}
		return values;
	}

	/**
	 * Reads bits from CodeText words, most significant bit first.
	 */
	static class CodeTextReader
	{
		private final int[] words;
		private final long nBits;
		private long position;

		CodeTextReader( int[] words, long nBits )
		{
			this.words = words;
			this.nBits = nBits;
		}

		/** reads the next bit, 0 once all the CodeText has been read */
		int readBit()
		{
			int word = (int)(this.position >>> 5);
			int bit = 0;
			if ( this.position < this.nBits && word < this.words.length ) {
				bit = (this.words[word] >>> (31 - (int)(this.position & 31))) & 1;
			}
			this.position++;
			return bit;
		}

		int readUnsigned( int n ) throws IOException
		{
			if ( n < 0 || n > 32 ) {
				throw new IOException( "Invalid bit field width: " + n ); //$NON-NLS-1$
			}
			int value = 0;
			for ( int i = 0; i < n; i++ ) {
				value = (value << 1) | readBit();
			}
			return value;
		}

		/**
		 * Reads a "nibbled" signed value: 4-bit nibbles, least significant first, each followed by
		 * a continuation bit, the value being sign-extended from its last nibble.
		 */
		int readNibbled() throws IOException
		{
			int value = 0;
			int shift = 0;
			int more;
			do {
				if ( shift >= 32 ) {
					throw new IOException( "Corrupt nibbled value" ); //$NON-NLS-1$
				}
				value |= readUnsigned( 4 ) << shift;
				shift += 4;
				more = readBit();
			}
			while ( more != 0 );
			return shift >= 32 ? value : (value << (32 - shift)) >> (32 - shift);
		}

		int readSigned( int n ) throws IOException
		{
			if ( n == 0 ) {
				return 0;
			}
			int value = readUnsigned( n );
			return n == 32 ? value : (value << (32 - n)) >> (32 - n);
		}
	}
}
