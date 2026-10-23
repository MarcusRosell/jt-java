/*******************************************************************************
 * This Library is :
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

package uk.ac.liv.jt.codec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;

import org.tukaani.xz.XZInputStream;

/**
 * Inflater for LZMA compressed data (compression algorithm 3), used by JT 10 and later.
 * The compressed data is an XZ stream, decoded with the org.tukaani.xz library.
 * All the compressed data has to be given in a single call to {@link #setInput(byte[])}.
 */
public class LzmaInflater implements IInflater
{
	private InputStream in;

	private boolean finished;

	private String error;

	@Override
	public void init( boolean nowrap )
	{
		this.in = null;
		this.finished = false;
		this.error = null;
	}

	@Override
	public boolean finished()
	{
		return this.finished;
	}

	@Override
	public boolean needsInput()
	{
		return this.in == null && this.error == null;
	}

	@Override
	public void setInput( byte[] tmp )
	{
		try {
			this.in = new XZInputStream( new ByteArrayInputStream( tmp ) );
		}
		catch ( IOException e ) {
			this.error = "Invalid LZMA data: " + e.getMessage(); //$NON-NLS-1$
		}
		catch ( LinkageError e ) {
			this.error = "LZMA compressed JT data requires the org.tukaani.xz library: " + e; //$NON-NLS-1$
		}
	}

	@Override
	public int inflate( byte[] p ) throws DataFormatException
	{
		if ( this.error != null ) {
			throw new DataFormatException( this.error );
		}
		try {
			int n = this.in.read( p );
			if ( n < 0 ) {
				this.finished = true;
				return 0;
			}
			return n;
		}
		catch ( IOException e ) {
			throw new DataFormatException( "Invalid LZMA data: " + e.getMessage() ); //$NON-NLS-1$
		}
	}

	@Override
	public void end()
	{
		if ( this.in != null ) {
			try {
				this.in.close();
			}
			catch ( IOException e ) {
				// nothing to release for in-memory streams
			}
			this.in = null;
		}
	}
}
