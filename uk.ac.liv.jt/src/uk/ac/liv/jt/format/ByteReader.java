/*******************************************************************************
 * This Library is :
 * 
 *     Copyright © 2010 Jerome Fuselier and Fabio Corubolo - all rights reserved
 *     jerome.fuselier@gmail.com ; corubolo@gmail.com
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * see COPYING.LESSER.txt
 * 
 * ---
 * 
 * This library used data structures from the JT Specification, that are subject to the JT specification license: 
 * JT_Specification_License.txt
 * 
 ******************************************************************************/
package uk.ac.liv.jt.format;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.zip.DataFormatException;

import uk.ac.liv.jt.codec.IInflater;
import uk.ac.liv.jt.codec.IInflaterFactory;
import uk.ac.liv.jt.codec.InflaterFactory;
import uk.ac.liv.jt.types.BBoxF32;
import uk.ac.liv.jt.types.CoordF32;
import uk.ac.liv.jt.types.GUID;
import uk.ac.liv.jt.types.Int32Range;
import uk.ac.liv.jt.types.RGBA;

/**
 * Contains convenience methods to support random access reading of the data
 * types used in JT. This class uses a Memory Mapped file to allow fast, random
 * access, and support on fly (sequential) ZLIB decoding, as used in JT
 * segments.
 * 
 * 
 * @author fabio
 * 
 */
public class ByteReader
{

	MappedByteBuffer backBuffer;

	ByteBuffer inflated;

	IInflater inf;

	private boolean inflating;

	public int compressedDataLength;

	public int compressedDataLeft;

	public int uncompressedRead = 0;

	int bitBufferLeft = 0;

	int bitBuffer = 0;

	int bitBufferLength = 0;

	long bitPosition = 0;

	byte[] bitByteBuff = new byte[4];

	public byte MAJOR_VERSION;

	final protected static int[] BIT_MASK = new int[8];

	final protected static int[] FIELD_MASK = new int[8];

	static {
		int tempBit = 1;
		int tempField = 1;
		for ( int i = 0; i < 8; i++ ) {
			BIT_MASK[i] = tempBit;
			FIELD_MASK[i] = tempField;
			tempBit <<= 1;
			tempField <<= 1;
			tempField++;
		}
	}

	public long getBitPosition()
	{
		return this.bitPosition;
	}

	public long getBitBufferLength()
	{
		return this.bitBufferLength;
	}

	/**
	 * creates a file backed byte reader.
	 * 
	 * @param f
	 *            the file that will be used to read contents
	 * @throws IOException
	 */
	public ByteReader( File f ) throws IOException
	{
		super();
		RandomAccessFile raf = new RandomAccessFile( f, "r" ); //$NON-NLS-1$
		FileChannel fc = raf.getChannel();
		if ( fc.size() > Integer.MAX_VALUE ) {
			throw new IOException( "File size too large to handle in memory maps" ); //$NON-NLS-1$
		}
		this.backBuffer = fc.map( MapMode.READ_ONLY, 0, fc.size() );
		this.backBuffer.order( ByteOrder.LITTLE_ENDIAN );

	}

	/** Sets length bytes to be read as in a order sensitive byte array.
	 * This has no effect if the bytes are in Big endian order; in little endian order, the bytes are 
	 * read in the order i+3, i+2, i+1, i in groups of four. 
	 * @param length the length of the data to be read; after that the data will be read bac as normal.
	 */
	public void setU32BytesToRead( int length )
	{
		this.bitBufferLength = length;
	}

	/** Reads a single bit of data 
	 * 
	 * @return
	 * @throws IOException
	 */

	public int readBit() throws IOException
	{

		if ( this.bitBufferLeft == 0 ) {
			this.bitBuffer = get();
			this.bitBufferLeft = 8;
		}
		int bit = (this.bitBuffer & BIT_MASK[--this.bitBufferLeft]) == 0 ? 0 : 1;
		this.bitPosition++;
		return bit;
	}

	/**
	 * Nullifies the bit buffer (thus aligning to the byte length)
	 */
	public void byteAling()
	{
		this.bitBufferLeft = 0;
	}

	/** reads an unsigned integer of up to 32 bits; 
	 * 
	 * @param bitLength
	 * @return
	 * @throws IOException
	 */
	public long readU32( int bitLength ) throws IOException
	{
		long value = 0;
		this.bitPosition += bitLength;

		while ( bitLength > 0 ) {
			if ( this.bitBufferLeft == 0 ) {
				this.bitBuffer = get();
				this.bitBufferLeft = 8;
			}
			int nbits = (bitLength > this.bitBufferLeft) ? this.bitBufferLeft : bitLength;

			int temp = ((this.bitBuffer >> (this.bitBufferLeft - nbits)) & FIELD_MASK[nbits - 1]);
			this.bitBufferLeft -= nbits;
			bitLength -= nbits;

			value <<= nbits;
			value |= temp;
		}
		return value;

//        if (bitLength > 32)
//            throw new IOException(
//                    "ByteReader: Trying to read Uint32 longer then 32 bits");
//
//        if (bitLength < 0)
//            throw new IOException(
//                    "ByteReader: Trying to read Uint32 <0 bits long");
//
//        if (bitLength == 0)
//            return 0;
//        long ret = 0;
//        while (bitLength > 0) {
//            ret = ret | (readBit() << --bitLength);
//        }
//        
//        return ret & 0x00000000FFFFFFFF;
	}

	/** reads an signed integer of up to 32 bits; first bit being the sign
	 * 
	 * @param bitLength
	 * @return
	 * @throws IOException
	 */

	public long readI32( int n ) throws IOException
	{
		if ( n == 0 ) {
			return 0;
		}
		int value = (readBit());
		value <<= (--n);
		this.bitPosition++;
		return (value | readU32( n ));
	}

//    public int readI32(int bitLength) throws IOException {
//
//        if (bitLength > 32)
//            throw new IOException(
//                    "ByteReader: Trying to read int32 longer then 32 bits");
//
//        if (bitLength < 0)
//            throw new IOException(
//                    "ByteReader: Trying to read int32 <0 bits long");
//
//        if (bitLength == 0)
//            return 0;
//        int sign = -1 * readBit();
//        return (sign * (int)readU32(--bitLength));
//    }

	// An unsigned 16-bit integer
	public int readU16() throws IOException
	{
		try {
			int ch1 = get();
			int ch2 = get();
			if ( getByteOrder() == ByteOrder.LITTLE_ENDIAN ) {
				return ((ch2 << 8) + (ch1 << 0));
			}
			else {
				return ((ch1 << 8) + (ch2 << 0));
			}
		}
		catch ( BufferOverflowException e ) {
			throw new IOException( e );
		}

	}

	private int get() throws IOException
	{
		// Reorder the bytes if needed

		if ( getByteOrder() == ByteOrder.LITTLE_ENDIAN && this.bitBufferLength > 0 ) {
			int pos = this.bitBufferLength % 4;
			if ( pos == 0 ) {
				this.bitByteBuff[3] = internalGetByte();
				this.bitByteBuff[2] = internalGetByte();
				this.bitByteBuff[1] = internalGetByte();
				this.bitByteBuff[0] = internalGetByte();
			}
			this.bitBufferLength--;
			return this.bitByteBuff[pos];

		}
		else {
			return internalGet();
		}

	}

	private byte internalGetByte() throws IOException
	{
		if ( !this.inflating ) {
			return this.backBuffer.get();
		}
		int n = 1;
		if ( this.inflated.remaining() < n ) {
			fillInflated( n );
		}
		this.uncompressedRead++;
		return this.inflated.get();
	}

	private int internalGet() throws IOException
	{
		if ( !this.inflating ) {
			return this.backBuffer.get() & 0x000000FF;
		}
		int n = 1;
		if ( this.inflated.remaining() < n ) {
			fillInflated( n );
		}
		this.uncompressedRead++;
		return this.inflated.get() & 0x000000FF;
	}

	/**
	 * reads a rgba color expressed by float values
	 * 
	 * @return
	 * @throws IOException
	 */
	public RGBA readColorF() throws IOException
	{
		float c = readF32();

		return new RGBA( c, c, c, 1.0f );
	}

	/** reads a rbga color with a single value for all components */
	public RGBA readColor() throws IOException
	{
		return new RGBA( readF32(), readF32(), readF32(), readF32() );
	}

	/**
	 * reads a two integer range
	 * 
	 * @return
	 * @throws IOException
	 */
	public Int32Range readRange() throws IOException
	{
		Int32Range r = new Int32Range();
		r.min = readI32();
		r.max = readI32();
		return r;
	}

	private void fillInflated( int n ) throws IOException
	{
		if ( this.inf.finished() ) {
			throw new IOException( "Inflated stream is finished" ); //$NON-NLS-1$
		}
		if ( this.inf.needsInput() ) {
			if ( this.compressedDataLeft <= 0 ) {
				throw new IOException( "Out of compressed data!" ); //$NON-NLS-1$
			}
			int size = Math.min( this.compressedDataLeft, 5 * 1024 );
			this.compressedDataLeft -= size;
			byte[] tmp = new byte[size];
			this.backBuffer.get( tmp );
			this.inf.setInput( tmp );
		}

		try {
			this.inflated.compact();
			byte[] p = new byte[2048];
			int c = this.inf.inflate( p );
			this.inflated.put( p, 0, c );
			this.inflated.flip();
		}
		catch ( DataFormatException e ) {
			throw new IOException( e );
		}

	}

	private int getInt() throws IOException
	{
		if ( !this.inflating ) {
			return this.backBuffer.getInt();
		}
		int n = 4;
		if ( this.inflated.remaining() < n ) {
			fillInflated( n );
		}
		this.uncompressedRead += n;
		return this.inflated.getInt();
	}

	private short getShort() throws IOException
	{
		if ( !this.inflating ) {
			return this.backBuffer.getShort();
		}
		int n = 2;
		if ( this.inflated.remaining() < n ) {
			fillInflated( n );
		}
		this.uncompressedRead += n;
		return this.inflated.getShort();
	}

	private long getLong() throws IOException
	{
		if ( !this.inflating ) {
			return this.backBuffer.getLong();
		}
		int n = 8;
		if ( this.inflated.remaining() < n ) {
			fillInflated( n );
		}
		this.uncompressedRead += n;
		return this.inflated.getLong();
	}

	private double getDouble() throws IOException
	{
		if ( !this.inflating ) {
			return this.backBuffer.getDouble();
		}
		int n = 8;
		if ( this.inflated.remaining() < n ) {
			fillInflated( n );
		}
		this.uncompressedRead += n;
		return this.inflated.getDouble();
	}

	private float getFloat() throws IOException
	{
		if ( !this.inflating ) {
			return this.backBuffer.getFloat();
		}
		int n = 4;
		if ( this.inflated.remaining() < n ) {
			fillInflated( n );
		}
		this.uncompressedRead += n;
		return this.inflated.getFloat();
	}

	private void get( byte[] b ) throws IOException
	{

		if ( !this.inflating ) {
			this.backBuffer.get( b );
		}
		else {
			int toRead = b.length;
			this.uncompressedRead += toRead;
			while ( toRead > 0 ) {
				if ( this.inflated.remaining() < toRead )
					fillInflated( toRead );
				int toWrite = Math.min( this.inflated.remaining(), toRead );
				this.inflated.get( b, b.length - toRead, toWrite );
				toRead -= toWrite;
			}
		}
	}

	// An unsigned 32-bit integer
	public long readU32() throws IOException
	{
		try {
			int ch1 = get();
			int ch2 = get();
			int ch3 = get();
			int ch4 = get();

			if ( getByteOrder() == ByteOrder.LITTLE_ENDIAN ) {
				return ((ch4 << 24) | (ch3 << 16) | (ch2 << 8) | (ch1 << 0)) & 0xFFFFFFFFL;
			}
			else {
				return ((ch1 << 24) | (ch2 << 16) | (ch3 << 8) | (ch4 << 0)) & 0xFFFFFFFFL;
			}
		}
		catch ( BufferOverflowException e ) {
			throw new IOException( e );
		}

	}

	// An unsigned 8-bit integer
	public int readU8() throws IOException
	{
		try {
			int ch1 = get();
			return ch1;
		}
		catch ( BufferOverflowException e ) {
			throw new IOException( e );
		}

	}

	// An unsigned 8-bit byte
	public int readUChar() throws IOException
	{
		try {
			int ch1 = get();
			return ch1;
		}
		catch ( BufferOverflowException e ) {
			throw new IOException( e );
		}
	}

	/**
	 * reads a 16 bytes GUID
	 * 
	 * @return
	 * @throws IOException
	 */
	public GUID readGUID() throws IOException
	{
		return GUID.of( readU32(), // 4
				readU16(), readU16(), // 2
				readU8(), readU8(), readU8(), readU8(), readU8(), readU8(), readU8(), readU8() );
	}

	// A signed two’s complement 16-bit integer value.
	public short readI16() throws IOException
	{
		try {
			short ch1 = getShort();
			return ch1;
		}
		catch ( BufferOverflowException e ) {
			throw new IOException( e );
		}
	}

	// A signed two’s complement 32-bit integer value.
	public int readI32() throws IOException
	{
		try {
			int ch1 = getInt();
			return ch1;
		}
		catch ( BufferOverflowException e ) {
			throw new IOException( e );
		}
	}

	// A signed two’s complement 64-bit integer value.
	public long readI64() throws IOException
	{
		try {
			long ch1 = getLong();
			return ch1;
		}
		catch ( BufferOverflowException e ) {
			throw new IOException( e );
		}
	}

	public String readString( int len ) throws IOException
	{
		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < len; i++ ) {
			int c = get();
			sb.append( (char)c );
		}
		return sb.toString();
	}

	public ByteOrder getByteOrder()
	{
		return this.backBuffer.order();
	}

	/**
	 * sets the byte reading order
	 * 
	 * @param bo
	 */

	public void setByteOrder( ByteOrder bo )
	{
		this.backBuffer.order( bo );
	}

	// An IEEE 32-bit floating point number
	public float readF32() throws IOException
	{
		try {
			return getFloat();
		}
		catch ( BufferOverflowException e ) {
			throw new IOException( e );
		}
	}

	// An IEEE 64-bit double precision floating point number
	public double readF64() throws IOException
	{
		try {
			return getDouble();
		}
		catch ( BufferOverflowException e ) {
			throw new IOException( e );
		}
	}

	/**
	 * reads len bytes
	 * 
	 * @param len
	 * @return a new byte[len]
	 * @throws IOException
	 */
	public byte[] readBytes( int len ) throws IOException
	{
		if ( len > 1024 * 1024 * 10 ) {
			return null;
		}
		byte[] b = new byte[len];
		try {
			get( b );
			return b;
		}
		catch ( BufferOverflowException e ) {
			throw new IOException( e );
		}
	}

	public String readMbString() throws IOException
	{
		int count = readI32();
		if ( count == 0 )
			return null;
		int[] sInt = new int[count];
		StringBuffer s = new StringBuffer();
		for ( int i = 0; i < count; i++ ) {
			sInt[i] = readU16();
			s.append( (char)sInt[i] );
		}
		return s.toString();
	}

	public String readString8() throws IOException
	{
		int count = readI32();
		if ( count == 0 )
			return null;
		int[] sInt = new int[count];
		StringBuffer s = new StringBuffer();
		for ( int i = 0; i < count; i++ ) {
			sInt[i] = readU8();
			s.append( (char)sInt[i] );
		}
		return s.toString();
	}

	public CoordF32 readCoordF32() throws IOException
	{
		float x = readF32();
		float y = readF32();
		float z = readF32();
		return new CoordF32( x, y, z );
	}

	public BBoxF32 readBBoxF32() throws IOException
	{
		CoordF32 min = readCoordF32();
		CoordF32 max = readCoordF32();
		return new BBoxF32( min, max );
	}

	/**
	 * sets the reading position to 0
	 */
	public void reset()
	{
		if ( this.inflating ) {
			setInflating( false );
		}
		this.backBuffer.position( 0 );
	}

	/**
	 * sets the absolute reading position
	 * 
	 * @param offset
	 */
	public void position( int newPosition )
	{
		if ( this.inflating ) {
			setInflating( false );
		}
		this.backBuffer.position( newPosition );

	}

	private void setInflating( boolean inflating )
	{
		this.inflating = inflating;
		if ( inflating ) {

			IInflaterFactory factory = InflaterFactory.getInflaterFactoryInstance();
			this.inf = factory.createInflater();
			this.inf.init( false );

			this.inflated = java.nio.ByteBuffer.allocate( 1024 * 30 );
			this.inflated.order( this.backBuffer.order() );
			this.inflated.flip();
			// System.out.println("S CDL " + compressedDataLeft);
		}
		else if ( this.inf != null ) {
			// System.out.println("E CDL " + compressedDataLeft + " = " +
			// (compressedDataLength - inf.getTotalIn()));
			// System.out.println("E Uncompressed data len " + inf.getTotalOut()
			// + " finished? " + inf.finished());
			this.inf.end();
			this.inf = null;
			this.inflated = null;
			;
		}
	}

	public float[] readVecF32() throws IOException
	{
		int n = readI32();
		float[] r = new float[n];
		for ( int i = 0; i < n; i++ ) {
			r[i] = readF32();
		}
		return r;
	}

	public double[] readVecF64() throws IOException
	{
		int n = readI32();
		double[] r = new double[n];
		for ( int i = 0; i < n; i++ ) {
			r[i] = readF64();
		}
		return r;
	}

	public int[] readVecI32() throws IOException
	{
		int n = readI32();
		int[] r = new int[n];
		for ( int i = 0; i < n; i++ ) {
			r[i] = readI32();
		}
		return r;
	}

	public long[] readVecU32() throws IOException
	{
		int n = readI32();
		long[] r = new long[n];
		for ( int i = 0; i < n; i++ ) {
			r[i] = readU32();
		}
		return r;
	}

	public boolean isInflating()
	{
		return this.inflating;
	}

	/**
	 * Enables or disables the ZLIB decompression of the data
	 * 
	 * @param b
	 * @param compressedDataLength
	 */
	public void setInflating( boolean b, int compressedDataLength )
	{
		this.compressedDataLength = compressedDataLength;
		this.compressedDataLeft = compressedDataLength;
		this.uncompressedRead = 0;
		setInflating( b );
	}

	public long position()
	{
		return this.backBuffer.position();
	}

	public int getNbBitsLeft()
	{
		// TODO Auto-generated method stub
		return this.bitBufferLeft;
	}

}
