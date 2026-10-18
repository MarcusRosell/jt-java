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

import uk.ac.liv.jt.format.JTQuantizationParam;

public class VertexShapeNodeElement extends BaseShapeNodeElement
{
	private int textureBinding;
	private int colorBinding;
	private int normalBinding;
	private JTQuantizationParam quantParam;
	private long vertexBinding;

	@Override
	public void read() throws IOException
	{
		super.read();

		if ( this.reader.MAJOR_VERSION >= 9 ) {
			// From JT 9 all the bindings are encoded in a single U64
			int versionNumber = this.reader.readVersionNumber();
			this.vertexBinding = this.reader.readI64();
			if ( versionNumber != 1 ) {
				this.quantParam = new JTQuantizationParam( this.reader );
				this.quantParam.read();
				this.vertexBinding = this.reader.readI64();
			}
			return;
		}

		this.normalBinding = this.reader.readI32();
		this.textureBinding = this.reader.readI32();
		this.colorBinding = this.reader.readI32();

		this.quantParam = new JTQuantizationParam( this.reader );

		this.getQuantParam().read();
	}

	/**
	 * @return the vertex bindings (from JT 9), with the normal, texture coordinate, color and flag bindings
	 */
	public long getVertexBinding()
	{
		return this.vertexBinding;
	}

	public int getTextureBinding()
	{
		return this.textureBinding;
	}

	public JTQuantizationParam getQuantParam()
	{
		return this.quantParam;
	}

	public int getNormalBinding()
	{
		return this.normalBinding;
	}

	public int getColorBinding()
	{
		return this.colorBinding;
	}
}
