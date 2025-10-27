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
 * @author fabio
 *
 */
public class BaseNodeElement extends JTNode
{

	public long nodeFlags;
	public int[] attObjectId;
	/** attributes for the node are filled in in the LSGSegmetn class */
	public BaseAttributeElement[] attributes;
	/** properties for the node are filled in in the LSGSegmetn class */
	public BasePropertyAtomData[][] properties;

	public boolean ignore()
	{
		if ( (this.nodeFlags & 1) == 1 ) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public void read() throws IOException
	{
		super.read();
//         System.out.println("Object ID: " + objectID + " class " +
//         this.getClass().getSimpleName());

		short versionNumber = -1;
		if ( this.reader.MAJOR_VERSION >= 9 ) {
			versionNumber = getReader().readI16();
			if ( versionNumber != 1 ) {
				throw new IllegalArgumentException( "Found invalid version number: " + versionNumber ); //$NON-NLS-1$
			}
		}

		this.nodeFlags = getReader().readU32();

		int attCount = getReader().readI32();

		this.attObjectId = new int[attCount];
		this.attributes = new BaseAttributeElement[attCount];

		// if (attCount > 0)
		// System.out.print("Attr IDs: ");
		for ( int i = 0; i < attCount; i++ ) {
			this.attObjectId[i] = getReader().readI32();
		}
		// System.out.print(attObjectId[i] + " ");

	}

	@Override
	public String toString()
	{
		if ( this.properties != null ) {
			for ( int i = 0; i < this.properties.length; i++ ) {
				if ( this.properties[i][0] != null && this.properties[i][0].ovalue instanceof String s && s.compareTo( "JT_PROP_NAME" ) == 0 ) { //$NON-NLS-1$
					return this.properties[i][1].ovalue + (this.getClass().getSimpleName().replaceAll( "[a-z]", "" )) + super.toString(); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		return (this.getClass().getSimpleName().replaceAll( "[a-z]", "" )) + super.toString(); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
