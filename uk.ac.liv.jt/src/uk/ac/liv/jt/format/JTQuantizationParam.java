/*******************************************************************************
 * This Library is :
 * 
 *     Copyright Â© 2010 Jerome Fuselier and Fabio Corubolo - all rights reserved
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
package uk.ac.liv.jt.format;

import java.io.IOException;

import uk.ac.liv.jt.debug.DebugInfo;
import uk.ac.liv.jt.internal.BundleAccessor;

/** Quantization Parameters specifies for each shape data type grouping 
 * (i.e. Vertex, Normal, Texture Coordinates, Color) the number of 
 * quantization bits used for given qualitative compression level. p.57 */
public class JTQuantizationParam
{
	private ByteReader reader;

	private int bitsPerVertex;
	private int normalBitsFactor;
	private int bitsPerTextureCoord;
	private int bitsPerColor;
	private int bitsPerNormal;

	public int getBitsPerNormal()
	{
		return this.bitsPerNormal;
	}

	public JTQuantizationParam( ByteReader reader )
	{
		super();
		this.reader = reader;
	}

	public void read() throws IOException
	{
		/* Bits Per Vertex specifies the number of quantization bits per vertex 
		 * coordinate component. Value must be within range [0:24] inclusive. */
		this.bitsPerVertex = this.reader.readU8();

		/* Normal Bits Factor is a parameter used to calculate the number of 
		 * quantization bits for normal vectors. 
		 * The actual number of quantization bits per normal is computed using 
		 * this factor and the following formula: 
		 * â€œBitsPerNormal = 6 + 2 * Normal Bits Factorâ€� */
		this.normalBitsFactor = this.reader.readU8();
		this.bitsPerNormal = 6 + 2 * this.normalBitsFactor;

		/* Bits Per Texture Coord specifies the number of quantization bits per 
		 * texture coordinate component. */
		this.bitsPerTextureCoord = this.reader.readU8();

		/* Bits Per Color specifies the number of quantization bits per 
		 * color component. */
		this.bitsPerColor = this.reader.readU8();

		if ( DebugInfo.debugMode ) {

			BundleAccessor.getLogger().info( "Bits per vertex: {}", getBitsPerVertex() ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Normal bits factor: {}", getNormalBitsFactor() ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Bits per normal: {}", getBitsPerNormal() ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Bits per texture Coord: {}", getBitsPerTextureCoord() ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Bits per color: {}", getBitsPerColor() ); //$NON-NLS-1$
		}

	}

	public int getBitsPerVertex()
	{
		return this.bitsPerVertex;
	}

	public int getNormalBitsFactor()
	{
		return this.normalBitsFactor;
	}

	public int getBitsPerTextureCoord()
	{
		return this.bitsPerTextureCoord;
	}

	public int getBitsPerColor()
	{
		return this.bitsPerColor;
	}
}
