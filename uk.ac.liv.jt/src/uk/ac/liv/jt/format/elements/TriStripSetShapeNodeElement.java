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
package uk.ac.liv.jt.format.elements;

import java.io.IOException;

import uk.ac.liv.jt.segments.LODSegment;
import uk.ac.liv.jt.types.GUID;

/** 
 * This class contains strip set data reference (usually as a reference to a LOD segment) 
 * that constitutes the faceted geometry of the objects. 
 * @author fabio
 *
 */
public class TriStripSetShapeNodeElement extends VertexShapeNodeElement
{
	public static boolean OGL_FIX = false;

	@Override
	public void read() throws IOException
	{
		super.read();
	}

	/** 
	 * Used to retrieve the segment LOD GUID for the shape implementation referenced. 
	 * This is used to allow late loading of the actual face data. 
	 * @return
	 * @throws IOException
	 */
	public GUID getLODSegmentId() throws IOException
	{
		LateLoadedPropertyAtomElement l = null;
		for ( BasePropertyAtomData p[] : this.properties ) {
			if ( p[1] instanceof LateLoadedPropertyAtomElement llpae && p[0] instanceof StringPropertyAtomElement s ) {
				if ( s.value.equals( "JT_LLPROP_SHAPEIMPL" ) ) { //$NON-NLS-1$
					l = llpae;
				}
			}
		}
		if ( l == null ) {
			throw new IOException( "Missing LOD segment reference" ); //$NON-NLS-1$
		}
		return l.segmentId;

	}

	private LODSegment readShape()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
