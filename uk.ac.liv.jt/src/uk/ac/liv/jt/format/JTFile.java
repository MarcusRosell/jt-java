/*******************************************************************************
 * This Library is :
 * 
 *     Copyright © 2010 Jerome Fuselier and Fabio Corubolo - all rights reserved
 *     jerome.fuselier@gmail.com ; corubolo@gmail.com
 *
 *     Copyright © 2026 Marcus Rosell - all rights reserved
 *     marcus.rosell@solme.se
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
import java.net.URI;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.liv.jt.debug.DebugInfo;
import uk.ac.liv.jt.internal.BundleAccessor;
import uk.ac.liv.jt.segments.JTSegment;
import uk.ac.liv.jt.segments.LSGSegment;
import uk.ac.liv.jt.types.GUID;

/** 
 * Main class to interpret a JT file. 
 * 
 * Use the standard {@link #read()} method to read the header and Table of Contents.
 * 
 * @author fabio
 *
 */
public class JTFile
{
	private ByteReader reader;

	// File Header
	private String version;
	private int fileAttribute;
	private int tocOffset;
	private GUID lsgSegmentID;
	public URI uri;

	private Map<GUID, JTSegment> segments;

	private LSGSegment lsgSegment;

	public JTFile( ByteReader reader )
	{
		super();

		this.reader = reader;
		this.segments = new HashMap<>();
	}

	public JTFile( File f, URI uri ) throws IOException
	{
		super();
		this.uri = uri;
		this.reader = new ByteReader( f );
		this.segments = new HashMap<>();
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append( "Version:" ).append( getVersion() ).append( '\n' ); //$NON-NLS-1$
		sb.append( "Byte Order:" ).append( this.reader.getByteOrder() ).append( '\n' ); //$NON-NLS-1$
		sb.append( "File Attribute:" ).append( getFileAttribute() ).append( '\n' ); //$NON-NLS-1$
		sb.append( "TOC Offset:" ).append( getTocOffset() ).append( '\n' ); //$NON-NLS-1$
		sb.append( "LSG Segment ID:" ).append( getLsgSegmentID() ).append( '\n' ); //$NON-NLS-1$
		sb.append( "Number of segments:" ).append( this.segments.size() ).append( '\n' ); //$NON-NLS-1$

		return sb.toString();
	}

	public void print()
	{
		BundleAccessor.getLogger().info( "this: {}", this ); //$NON-NLS-1$

		BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$

		for ( Entry<GUID, JTSegment> me : this.segments.entrySet() ) {
			BundleAccessor.getLogger().info( "Segment: {} :\n{}", me.getKey(), me.getValue() ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
		}
	}

	public void addSegment( JTSegment segment )
	{
		this.segments.put( segment.getID(), segment );
	}

	/** reads the JT file header 
	 * 
	 * @throws IOException
	 */
	public void readHeader() throws IOException
	{
		this.version = this.reader.readString( 80 );

		this.reader.MAJOR_VERSION = parseMajorVersion( this.version );

		int a = this.reader.readUChar();
		if ( a == 0 ) {
			this.reader.setByteOrder( ByteOrder.LITTLE_ENDIAN );
		}
		else if ( a == 1 ) {
			this.reader.setByteOrder( ByteOrder.BIG_ENDIAN );
		}
		else {
			throw new IOException( "IN JT header: byteorder has non valid value: " + a ); //$NON-NLS-1$
		}
		this.fileAttribute = this.reader.readI32();
		// from JT 10 the TOC offset is an U64
		this.tocOffset = this.reader.MAJOR_VERSION >= 10 ? toInt( this.reader.readI64() ) : this.reader.readI32();
		this.lsgSegmentID = this.reader.readGUID();

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "Version: {}", this.version ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Byte Order: {}", this.reader.getByteOrder() ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "File Attribute: {}", getFileAttribute() ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "TOC Offset: {}", this.tocOffset ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "LSG Segment ID: {}", this.lsgSegmentID ); //$NON-NLS-1$
		}
	}

	/** reads the JT file Table of contents 
	 * 
	 * @throws IOException
	 */
	public void readTOC() throws IOException
	{
		this.reader.position( getTocOffset() );

		int entryCount = this.reader.readI32();

		// System.out.println("Entry Count:" + entryCount);

		for ( int i = 0; i < entryCount; i++ ) {
			JTSegment segment = readTOCEntry();
			addSegment( segment );
			if ( segment.getID().equals( this.lsgSegmentID ) ) {
				this.lsgSegment = (LSGSegment)segment;
			}

			if ( DebugInfo.debugMode ) {
				BundleAccessor.getLogger().info( "Segment Entry {}:", i ); //$NON-NLS-1$
				BundleAccessor.getLogger().info( "  GUID: {}", segment.getID() ); //$NON-NLS-1$
				BundleAccessor.getLogger().info( "  Offset: {}", segment.getOffset() ); //$NON-NLS-1$
				BundleAccessor.getLogger().info( "  Length: {}", segment.getLength() ); //$NON-NLS-1$
				BundleAccessor.getLogger().info( "  Segment type: {}", segment.getType() ); //$NON-NLS-1$

//            	if (segment.getType() == 7)
//                  System.out.println(segment.getOffset());
			}
		}
	}

	private JTSegment readTOCEntry() throws IOException
	{
		GUID segmentID = this.reader.readGUID();
		// from JT 10 the segment offset is an U64
		int segmentOffset = this.reader.MAJOR_VERSION >= 10 ? toInt( this.reader.readI64() ) : this.reader.readI32();
		int segmentLength = this.reader.readI32();
		long segmentAttributes = this.reader.readU32();

		return JTSegment.createJTSegment( this.reader, segmentID, segmentOffset, segmentLength, segmentAttributes, this );
	}

	/**
	 * Extracts the major version number from the version string of the header, e.g. 10 for "Version 10.3 JT".
	 */
	private static byte parseMajorVersion( String version ) throws IOException
	{
		Matcher m = Pattern.compile( "Version\\s+(\\d+)\\." ).matcher( version ); //$NON-NLS-1$
		if ( !m.find() ) {
			throw new IOException( "Invalid JT version string: " + version.trim() ); //$NON-NLS-1$
		}
		return Byte.parseByte( m.group( 1 ) );
	}

	private static int toInt( long offset ) throws IOException
	{
		if ( offset < 0 || offset > Integer.MAX_VALUE ) {
			throw new IOException( "File offset too large: " + offset ); //$NON-NLS-1$
		}
		return (int)offset;
	}

	/** Convenience methods, reads the header and Table of Contents, 
	 * and returns the pointer to the LSG segment
	 * 
	 * @return a pointer to the LSG segment
	 * @throws IOException
	 */
	public LSGSegment read() throws IOException
	{
		// read the file header
		readHeader();

		// Read the Table of Contents

		readTOC();

		return this.lsgSegment;
	}

	/** 
	 * Returns the collection of JT segments.
	 * @return
	 */
	public Collection<JTSegment> getSegments()
	{
		return this.segments.values();
	}

	public String getVersion()
	{
		return this.version;
	}

	public void setVersion( String version )
	{
		this.version = version;
	}

	public int getFileAttribute()
	{
		return this.fileAttribute;
	}

	public void setFileAttribute( int fileAttribute )
	{
		this.fileAttribute = fileAttribute;
	}

	public int getTocOffset()
	{
		return this.tocOffset;
	}

	public GUID getLsgSegmentID()
	{
		return this.lsgSegmentID;
	}

	public LSGSegment getLsgSegment()
	{
		return this.lsgSegment;
	}

	public JTSegment getSegment( GUID g )
	{
		return this.segments.get( g );
	}
}
