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
 * Base for all the attribute types, that are not strictly defined as nodes.
 * The attributes for consist for example of the transformation and Material attributes.
 * @author fabio
 *
 */
public class BaseAttributeElement extends JTNode
{

	public long fieldInhibitFlags;
	/** from JT 10 */
	public long fieldFinalFlags;
	public int stateFlags;

	@Override
	public void read() throws IOException
	{
		super.read();

		if ( this.reader.MAJOR_VERSION >= 9 ) {
			this.reader.readVersionNumber();
		}

		this.stateFlags = this.reader.readU8();
		this.fieldInhibitFlags = this.reader.readU32();
		// System.out.println("Attribute ID: " + getObjectID());
		if ( this.reader.MAJOR_VERSION >= 10 ) {
			this.fieldFinalFlags = this.reader.readU32();
		}

	}

}
