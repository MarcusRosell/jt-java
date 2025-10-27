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
package uk.ac.liv.jt.segments;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import uk.ac.liv.jt.format.ByteReader;
import uk.ac.liv.jt.format.JTElement;
import uk.ac.liv.jt.format.JTFile;
import uk.ac.liv.jt.internal.BundleAccessor;
import uk.ac.liv.jt.types.GUID;

/**
 * JTSegment is parent to all the segment classes. 
 * The factory method {@link #createJTSegment} instantiates segments of the appropriate type. 
 * The {@link #read} method is overridden to implement the segment specific code. 
 * The header and ZLIB compression is handled transparently in here.
 * 
 * @author fabio
 *
 */
public class JTSegment
{

	protected ByteReader reader;

	protected GUID id;
	protected int offset;
	protected int length;
	protected long attributes;
	protected int segType;
	public JTFile file;
	protected Map<GUID, JTElement> elements;

	public long compressionFlag;

	protected int compressedDataLength;

	protected int compressionAlgorithm;

	protected JTSegment()
	{}

	/**
	 * Factory method to instantiate segments of the appropriate type. 
	 * 
	 * @param reader a ByteReader for the JT file, allowing random access 
	 * @param segmentID the ID of the segment (from TOC)
	 * @param segmentOffset the offset from the start of the file (from TOC)
	 * @param segmentLength the length of the segment (from TOC)
	 * @param segmentAttributes (from TOC)
	 * @param f reference to the JTFile class used 
	 * @return a new JTSegment of the appropriate type
	 * @throws IOException
	 */
	public static JTSegment createJTSegment( ByteReader reader, GUID segmentID,
			int segmentOffset, int segmentLength, long segmentAttributes,
			JTFile f )
			throws IOException
	{
		JTSegment t;

		int segType = (int)segmentAttributes >> 24;
		switch ( segType ) {
			case 1:
				t = new LSGSegment();
				break;
			case 2:
				t = new JTBRepSegment();
				break;
			/*
			 * 7.2.7 PMI Data Segment The PMI Manager Meta Data Element (as
			 * documented in 7.2.6.2 PMI Manager Meta Data Element) can sometimes
			 * also be represented in a PMI Data Segment. This can occur when a pre
			 * JT 8 version file is migrated to JT 8.1 version file. So from a
			 * parsing point of view a PMI Data Segment should be treated exactly
			 * the same as a 7.2.6 Meta Data Segment.
			 */
			case 3:
			case 4:
				t = new MetadataSegment();
				break;
			case 6:
				t = new LODSegment( -1 );
				break;
			// LOD levels 0 to 9
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
				t = new LODSegment( segType - 7 );
				break;
			case 17:
				t = new XTSegment();
				break;
			case 18:
				t = new WireFrameSegment();
				break;

			default:
				throw new IOException( "Invalid segment Type" ); //$NON-NLS-1$
		}

		t.reader = reader;

		t.elements = new HashMap<>( 1 );
		t.file = f;
		t.id = segmentID;
		t.offset = segmentOffset;
		t.length = segmentLength;
		t.attributes = segmentAttributes;

		t.segType = (int)segmentAttributes >> 24;

		return t;
	}

	/**
	 * reads the header and contents of the JT Segment. The header and ZLIB compression is handled here
	 * Subclasses should override and read the remaining content
	 * 
	 * @throws IOException
	 */
	public void read() throws IOException
	{
		// Position the cursor to the beginning of the segment
		this.reader.position( getOffset() );

		readHeader();

		// handle ZLIB segment header here
		if ( this.segType < 5 || this.segType > 16 ) {
			readHeaderZLIB();
			if ( this.compressionFlag == 2 && this.compressionAlgorithm == 2 ) {
				this.reader.setInflating( true, this.compressedDataLength );
			}
		}
	}

	void readHeaderZLIB() throws IOException
	{
		this.compressionFlag = this.reader.readU32();
		this.compressedDataLength = this.reader.readI32();
		this.compressionAlgorithm = this.reader.readU8();
		// Note that data field compressionAlgorithm is included in
		// compressedDataLength count =>
		this.compressedDataLength -= 1;
	}

	private void readHeader() throws IOException
	{
		// The header in the TOC should be equal to the header read here
		this.id = this.reader.readGUID();
		int tocType = this.segType;
		int tocLength = this.length;
		this.segType = this.reader.readI32();
		this.length = this.reader.readI32();
		if ( tocType != this.segType ) {
			BundleAccessor.getLogger().error( "TOC segment type different from segment type... " ); //$NON-NLS-1$
		}
		if ( tocLength != this.length ) {
			BundleAccessor.getLogger().error( "TOC segment lenght different from segment lenght... " ); //$NON-NLS-1$
		}
		BundleAccessor.getLogger().trace( this.toString() );
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "Segment ID:" ).append( getID() ).append( '\n' ); //$NON-NLS-1$
		sb.append( "Segment Offset:" ).append( getOffset() ).append( '\n' ); //$NON-NLS-1$
		sb.append( "Segment Length:" ).append( getLength() ).append( '\n' ); //$NON-NLS-1$
		sb.append( "Segment Attributes:" ).append( getAttributes() ).append( '\n' ); //$NON-NLS-1$
		sb.append( "Segment Type:" ).append( getType() ).append( '\n' ); //$NON-NLS-1$

		return sb.toString();
	}

	public GUID getID()
	{
		return this.id;
	}

	public void setID( GUID segmentID )
	{
		this.id = segmentID;
	}

	public int getOffset()
	{
		return this.offset;
	}

	public void setOffset( int segmentOffset )
	{
		this.offset = segmentOffset;
	}

	public int getLength()
	{
		return this.length;
	}

	public void setLength( int segmentLength )
	{
		this.length = segmentLength;
	}

	public long getAttributes()
	{
		return this.attributes;
	}

	public void setAttributes( long segmentAttributes )
	{
		this.attributes = segmentAttributes;
	}

	public int getType()
	{
		return this.segType;
	}

	public void setType( int segmentType )
	{
		this.segType = segmentType;
	}

	public Map<GUID, JTElement> getElements()
	{
		return this.elements;
	}

}
