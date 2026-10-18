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

public class StringPropertyAtomElement extends BasePropertyAtomData
{
	private String value;

	@Override
	public void read() throws IOException
	{
		super.read();

		if ( getReader().MAJOR_VERSION >= 9 ) {
			this.reader.readVersionNumber();
		}

		this.value = this.reader.readMbString();
		// System.out.println("Value: " + value);
		this.ovalue = this.value;
	}

	@Override
	public String toString()
	{
		return "" + this.value; //$NON-NLS-1$
	}

	public String getValue()
	{
		return this.value;
	}

	public void setValue( String value )
	{
		this.value = value;
	}
}
