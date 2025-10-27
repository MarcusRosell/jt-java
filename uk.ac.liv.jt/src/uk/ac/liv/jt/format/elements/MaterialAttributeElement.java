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

import uk.ac.liv.jt.types.RGBA;

/**
 * Material attribute specifies the Material properties used for rendering the shape data.
 * Currently only the colors (ambient, emission, specular, diffuse) are supported. 
 * 
 * @author fabio
 *
 */
public class MaterialAttributeElement extends BaseAttributeElement
{

	/** see documentation */
	public int dataFlags;

	public float shineness;

	public RGBA ambient, emission, specular, diffuse;

	@Override
	public void read() throws IOException
	{
		super.read();

		int versionNumber = -1;
		if ( this.reader.MAJOR_VERSION >= 9 ) {
			versionNumber = this.reader.readI16();
			if ( versionNumber != 1 ) {
				throw new IllegalArgumentException( "Found invalid version number: " + versionNumber ); //$NON-NLS-1$
			}
		}

		this.dataFlags = this.reader.readU16();

		if ( (this.dataFlags & 0x1) != 0 && (this.dataFlags & 0x2) != 0 ) {
			this.ambient = this.reader.readColorF();
		}
		else {
			this.ambient = this.reader.readColor();
		}

		this.diffuse = this.reader.readColor();
		if ( (this.dataFlags & 0x1) != 0 && (this.dataFlags & 0x4) != 0 ) {
			this.specular = this.reader.readColorF();
		}
		else {
			this.specular = this.reader.readColor();
		}
		if ( (this.dataFlags & 0x1) != 0 && (this.dataFlags & 0x8) != 0 ) {
			this.emission = this.reader.readColorF();
		}
		else {
			this.emission = this.reader.readColor();
		}
		this.shineness = this.reader.readF32();

	}

	@Override
	public String toString()
	{
		return "Material attribute"; //$NON-NLS-1$
	}

	/**
	 * Returns a AppearanceData containing the specific material attributes.
	 * @return AppearanceData
	 */
	public AppearanceData getAppearanceData()
	{
		AppearanceData appD = new AppearanceData();
		appD.shineness = this.shineness;
		appD.ambient = this.ambient;
		appD.specular = this.specular;
		appD.diffuse = this.diffuse;
		appD.emission = this.emission;
		return appD;
	}
}
