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

import uk.ac.liv.jt.types.GUID;

public class LateLoadedPropertyAtomElement extends BasePropertyAtomData
{
	GUID segmentId;
	int segmentType;

	@Override
	public void read() throws IOException
	{
		super.read();

		if ( getReader().MAJOR_VERSION >= 9 ) {
			this.reader.readVersionNumber();
		}

		this.segmentId = this.reader.readGUID();
		this.segmentType = this.reader.readI32();
		this.ovalue = this.segmentId;

		int payLoadObjectID = -1;
		if ( getReader().MAJOR_VERSION >= 9 ) {
			payLoadObjectID = this.reader.readI32();
		}
		// reserved, no longer present from JT 10
		if ( getReader().MAJOR_VERSION == 9 ) {
			this.reader.readI32();
		}

		// System.out.println("Late loaded:" + segmentId + " type " +
		// segmentType);
	}

	@Override
	public String toString()
	{
		return "Late loaded segment: " + this.segmentId + " of type: " + this.segmentType; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
