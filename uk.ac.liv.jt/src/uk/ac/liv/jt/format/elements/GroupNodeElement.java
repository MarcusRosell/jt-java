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

/** 
 * 
 * Nodes of this types can have Children so are proper graph nodes
 * @author fabio
 *
 */
public class GroupNodeElement extends BaseNodeElement
{
	/** contain reference to the node's children, than can be of any NODE type */

	public int[] childNodeObjectId;

	@Override
	public void read() throws IOException
	{
		super.read();

		short versionNumber = -1;
		if ( this.reader.MAJOR_VERSION >= 9 ) {
			versionNumber = getReader().readI16();
			if ( versionNumber != 1 ) {
				throw new IllegalArgumentException( "Found invalid version number: " + versionNumber ); //$NON-NLS-1$
			}
		}

		int childCount = this.reader.readI32();
		this.childNodeObjectId = new int[childCount];

		// System.out.print("Child IDs: ");

		for ( int i = 0; i < childCount; i++ ) {
			this.childNodeObjectId[i] = this.reader.readI32();
		}
		// System.out.print(childNodeObjectId[i]+ " ");
	}
}
