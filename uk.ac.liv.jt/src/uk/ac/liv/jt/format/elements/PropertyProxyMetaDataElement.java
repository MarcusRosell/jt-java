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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import uk.ac.liv.jt.format.JTElement;
import uk.ac.liv.jt.internal.BundleAccessor;

public class PropertyProxyMetaDataElement extends JTElement
{
	public Map<String, BasePropertyAtomData> properties;

	@Override
	public void read() throws IOException
	{
		String key;
		this.properties = new HashMap<>();

		while ( (key = this.reader.readMbString()) != null ) {
			int type = this.reader.readU8();
			BasePropertyAtomData d;
			switch ( type ) {
				case 0:
					d = null;
					break;
				case 1:
					d = new StringPropertyAtomElement();
					((StringPropertyAtomElement)d).value = this.reader.readMbString();
					break;
				case 2:
					d = new IntegerPropertyAtomElement();
					((IntegerPropertyAtomElement)d).value = this.reader.readI32();
					break;
				case 3:
					d = new FloatingPointPropertyAtomElement();
					((FloatingPointPropertyAtomElement)d).value = this.reader.readF32();
					break;
				case 4:
					d = new DatePropertyAtomElement();
					short y = this.reader.readI16();
					short month = this.reader.readI16();
					short da = this.reader.readI16();
					short h = this.reader.readI16();
					short minute = this.reader.readI16();
					short s = this.reader.readI16();

					// Specification does not mention time zone or else; so we
					// assume GMT
					// lacking better information
					Calendar c = Calendar.getInstance( TimeZone.getTimeZone( "GMT" ) );
					c.set( y, month, da, h, minute, s );
					((DatePropertyAtomElement)d).date = c.getTime();
					break;
				default:
					d = null;
					BundleAccessor.getLogger().error( "invalid data type in PropertyProxyMetaDataElement" ); //$NON-NLS-1$
					break;
			}
			this.properties.put( key, d );
			//System.out.println(key + " = " + d);

		}
	}
}
