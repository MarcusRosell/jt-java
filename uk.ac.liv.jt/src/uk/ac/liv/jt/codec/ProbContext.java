package uk.ac.liv.jt.codec;

import java.io.IOException;

import uk.ac.liv.jt.format.ByteReader;

/**
 * Probability context table of the Arithmetic CODEC in the JT 9 and later Int32 compressed data
 * packets (see {@link Int32CDP2}), and the corresponding arithmetic decoder.
 * <p>
 * The table is stored as bit fields, read most significant bit first from the bytes of the stream,
 * followed by alignment bits up to the next byte.
 */
class ProbContext
{
	private final boolean[] escape;
	private final int[] count;
	private final int[] cumCount;
	private final int[] value;
	private final int totalCount;

	private ProbContext( boolean[] escape, int[] count, int[] value )
	{
		this.escape = escape;
		this.count = count;
		this.value = value;
		this.cumCount = new int[count.length];
		int total = 0;
		for ( int i = 0; i < count.length; i++ ) {
			this.cumCount[i] = total;
			total += count[i];
		}
		this.totalCount = total;
	}

	static ProbContext read( ByteReader reader ) throws IOException
	{
		StreamBitReader bits = new StreamBitReader( reader );
		int entryCount = bits.readUnsigned( 16 );
		boolean[] escape = new boolean[entryCount];
		int[] count = new int[entryCount];
		int[] value = new int[entryCount];
		if ( reader.MAJOR_VERSION >= 10 ) {
			// JT 10: escape flag, occurrence count, value relative to min value
			int occCountBits = bits.readUnsigned( 6 );
			int valueBits = bits.readUnsigned( 7 );
			int minValue = bits.readUnsigned( 32 );
			for ( int i = 0; i < entryCount; i++ ) {
				escape[i] = bits.readUnsigned( 1 ) != 0;
				count[i] = bits.readUnsigned( occCountBits );
				value[i] = bits.readUnsigned( valueBits ) + minValue;
			}
		}
		else {
			// JT 9: symbol (-2 for the escape symbol), occurrence count, value relative to min value
			int symbolBits = bits.readUnsigned( 6 );
			int occCountBits = bits.readUnsigned( 6 );
			int valueBits = bits.readUnsigned( 6 );
			int minValue = bits.readUnsigned( 32 );
			for ( int i = 0; i < entryCount; i++ ) {
				escape[i] = bits.readUnsigned( symbolBits ) - 2 == -2;
				count[i] = bits.readUnsigned( occCountBits );
				value[i] = bits.readUnsigned( valueBits ) + minValue;
			}
		}
		// the remaining bits of the last byte are alignment bits
		return new ProbContext( escape, count, value );
	}

	boolean hasEscape()
	{
		for ( boolean e : this.escape ) {
			if ( e ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Decodes the values from the arithmetic coded CodeText, using this context.
	 *
	 * @param oobValues the out-of-band values, used in place of the escape symbols
	 */
	int[] decodeArithmetic( int valueCount, int[] codeText, int codeTextLength, int[] oobValues ) throws IOException
	{
		if ( this.totalCount <= 0 ) {
			throw new IOException( "Empty probability context" ); //$NON-NLS-1$
		}
		Int32CDP2.CodeTextReader bits = new Int32CDP2.CodeTextReader( codeText, codeTextLength );
		int[] values = new int[valueCount];
		int iOob = 0;
		int low = 0;
		int high = 0xffff;
		int code = bits.readUnsigned( 16 );
		for ( int i = 0; i < valueCount; i++ ) {
			// Scale the current code into the range of counts of the context to look up the symbol
			int rescaledCode = (int)((((long)(code - low) + 1) * this.totalCount - 1) / ((long)(high - low) + 1));
			int entry = lookupEntryByCumCount( rescaledCode );
			if ( this.escape[entry] ) {
				if ( iOob >= oobValues.length ) {
					throw new IOException( "Missing out-of-band values" ); //$NON-NLS-1$
				}
				values[i] = oobValues[iOob++];
			}
			else {
				values[i] = this.value[entry];
			}

			// Remove the symbol from the stream
			long range = (long)(high - low) + 1;
			high = low + (int)((range * (this.cumCount[entry] + this.count[entry])) / this.totalCount - 1);
			low = low + (int)((range * this.cumCount[entry]) / this.totalCount);
			for ( ;; ) {
				if ( ((high ^ low) & 0x8000) == 0 ) {
					// the most significant digits match, they are shifted out
				}
				else if ( (low >> 14) == 1 && (high >> 14) == 2 ) {
					// underflow is threatening, the 2nd most significant digit is shifted out
					code ^= 0x4000;
					low &= 0x3fff;
					high |= 0x4000;
				}
				else {
					break;
				}
				low = (low << 1) & 0xffff;
				high = ((high << 1) | 1) & 0xffff;
				code = ((code << 1) | bits.readBit()) & 0xffff;
			}
		}
		return values;
	}

	private int lookupEntryByCumCount( int cumCount ) throws IOException
	{
		for ( int i = 0; i < this.count.length; i++ ) {
			if ( cumCount < this.cumCount[i] + this.count[i] ) {
				return i;
			}
		}
		throw new IOException( "Bad probability table" ); //$NON-NLS-1$
	}

	/**
	 * Reads bit fields from the bytes of the stream, most significant bit first.
	 */
	private static class StreamBitReader
	{
		private final ByteReader reader;
		private int currentByte;
		private int bitsLeft;

		StreamBitReader( ByteReader reader )
		{
			this.reader = reader;
		}

		int readUnsigned( int n ) throws IOException
		{
			int value = 0;
			for ( int i = 0; i < n; i++ ) {
				if ( this.bitsLeft == 0 ) {
					this.currentByte = this.reader.readU8();
					this.bitsLeft = 8;
				}
				this.bitsLeft--;
				value = (value << 1) | ((this.currentByte >> this.bitsLeft) & 1);
			}
			return value;
		}
	}
}
