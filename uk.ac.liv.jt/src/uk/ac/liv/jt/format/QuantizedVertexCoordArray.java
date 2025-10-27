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

import uk.ac.liv.jt.codec.Int32Compression;
import uk.ac.liv.jt.codec.Predictors.PredictorType;
import uk.ac.liv.jt.codec.UniformQuantisation;
import uk.ac.liv.jt.debug.DebugInfo;
import uk.ac.liv.jt.internal.BundleAccessor;
import uk.ac.liv.jt.types.U32Vector;

/** The Quantized Vertex Coord Array data collection contains the 
 * quantization data/representation for a set of vertex coordinates. 
 * p.238 */
public class QuantizedVertexCoordArray
{

	ByteReader reader;

	float[] xValues;
	float[] yValues;
	float[] zValues;

	public QuantizedVertexCoordArray( ByteReader reader )
	{
		super();
		this.reader = reader;

		this.xValues = new float[0];
		this.yValues = new float[0];
		this.zValues = new float[0];
	}

	public void read() throws IOException
	{
		PointQuantizer ptQuant = new PointQuantizer( this.reader );
		ptQuant.read();

		/* Vertex Count specifies the count (number of unique) vertices in 
		 * the Vertex Codes arrays. */
		int vertexCount = this.reader.readI32();

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "** X Vertex Coord Codes: **" ); //$NON-NLS-1$
		}
		/* X Vertex Coord Codes is a vector of quantizer â€œcodesâ€� for all 
		 * the X-components of a set of vertex coordinates. */
		U32Vector xVertexCoordCodesVecU32 = Int32Compression.read_VecU32_Int32CDP( this.reader, PredictorType.Lag1 );
		this.xValues = UniformQuantisation.dequantize( xVertexCoordCodesVecU32, ptQuant.getXQuantizerData() );

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Float values ({})", this.xValues.length ); //$NON-NLS-1$
//            for (double xValue : xValues)
//            	System.out.print(xValue + " ");
//            System.out.println();
		}

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "** Y Vertex Coord Codes: **" ); //$NON-NLS-1$
		}
		/* Y Vertex Coord Codes is a vector of quantizer â€œcodesâ€� for all 
		 * the Y-components of a set of vertex coordinates. */
		U32Vector yVertexCoordCodesVecU32 = Int32Compression.read_VecU32_Int32CDP( this.reader, PredictorType.Lag1 );
		this.yValues = UniformQuantisation.dequantize( yVertexCoordCodesVecU32, ptQuant.getYQuantizerData() );

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Float values ({})", this.yValues.length ); //$NON-NLS-1$
//            for (double yValue : yValues)
//            	System.out.print(yValue + " ");
//            System.out.println();
		}

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "** Z Vertex Coord Codes: **" ); //$NON-NLS-1$
		}
		/* Z Vertex Coord Codes is a vector of quantizer â€œcodesâ€� for all 
		 * the Z-components of a set of vertex coordinates. */
		U32Vector zVertexCoordCodesVecU32 = Int32Compression.read_VecU32_Int32CDP( this.reader, PredictorType.Lag1 );
		this.zValues = UniformQuantisation.dequantize( zVertexCoordCodesVecU32, ptQuant.getZQuantizerData() );

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Float values ({})", this.zValues.length ); //$NON-NLS-1$
//                    for (double zValue : zValues)
//                    	System.out.print(zValue + " ");
//                    System.out.println();
		}

	}

	public float[] getXValues()
	{
		return this.xValues;
	}

	public float[] getYValues()
	{
		return this.yValues;
	}

	public float[] getZValues()
	{
		return this.zValues;
	}

}
