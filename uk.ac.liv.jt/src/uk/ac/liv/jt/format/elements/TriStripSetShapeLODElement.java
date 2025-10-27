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
package uk.ac.liv.jt.format.elements;

import java.io.IOException;

import uk.ac.liv.jt.codec.Int32Compression;
import uk.ac.liv.jt.codec.Predictors.PredictorType;
import uk.ac.liv.jt.debug.DebugInfo;
import uk.ac.liv.jt.format.JTQuantizationParam;
import uk.ac.liv.jt.format.LossyQuantizedRawVertexData;
import uk.ac.liv.jt.format.QuantizedVertexCoordArray;
import uk.ac.liv.jt.format.QuantizedVertexNormalArray;
import uk.ac.liv.jt.internal.BundleAccessor;
import uk.ac.liv.jt.types.Vec3D;

/** A Tri-Strip Set Shape LOD Element contains the geometric shape 
  definition data (e.g. vertices, polygons, normals, etc.) for a single 
  LOD of a collection of independent and unconnected triangle strips. 
  p.120 */
public class TriStripSetShapeLODElement extends ShapeLODElement
{

	public int[] vertexDataIndices;
	/** Primitive List Indices is a vector of indices into the 
	 uncompressed Raw Vertex Data marking the start/beginning of 
	 primitives. */
	public int[] primitiveListIndices;
	public QuantizedVertexCoordArray quantVertexCoord;
	public QuantizedVertexNormalArray quantVertexNorm;

	public double[] vertex;
	public double[] normal;

	/* Normals for the vertices */
	public Vec3D[] normals;
	private JTQuantizationParam quantParam;
	public boolean uncompressed;

//    public static boolean testDisplay = false;
	public static boolean testDisplayNormals = false;

	@Override
	public void read() throws IOException
	{
		readVertexShapeLODData();

		/* Version Number is the version identifier for this Tri-Strip Set 
		 * Shape LOD. Version number â€œ0x0001â€� is currently the only valid 
		 * value. p.120 */
		int versionNumber = getReader().readI16();

		readVertexBasedShapeCompressedRepData();
	}

	/** Vertex Shape LOD Data collection contains the bindings and 
	 * quantization settings for all shape LODs defined by a collection of 
	 * vertices. p.118
	 */
	public void readVertexShapeLODData() throws IOException
	{

		/* Version Number is the version identifier for this Vertex Shape LOD 
		 * Data. Version number â€œ0x0001â€� is currently the only valid value.
		 * p.119
		 */
		int versionNumber = getReader().readI16();

		/* Binding Attributes is a collection of normal, texture coordinate, 
		 * and color binding information encoded within a single I32 using the 
		 * following bit allocation. p.119
		 */
		int bindingAttributes = getReader().readI32();

		this.quantParam = new JTQuantizationParam( getReader() );
		this.quantParam.read();

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "*** Vertex Shape LOD Data ***" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Version Number: {}", versionNumber ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Binding Attributes: {}", bindingAttributes ); //$NON-NLS-1$
		}
	}

	public void readVertexBasedShapeCompressedRepData() throws IOException
	{
		/* The Vertex Based Shape Compressed Rep Data collection is the 
		 * compressed and/or encoded representation of the vertex coordinates, 
		 * normal, texture coordinate, and color data for a vertex based 
		 * shape. p.234 */

		/* Version Number is the version identifier for this Vertex Based 
		 * Shape Rep Data. Version number â€œ0x0001â€� is currently the only valid 
		 * value. */
		int versionNumber = getReader().readI16();

		/* Normal Binding specifies how (at what granularity) normal vector 
		 * data is supplied (â€œboundâ€�) for the Shape Rep in either the Lossless 
		 * Compressed Raw Vertex Data */
		int normalBinding = getReader().readU8();

		/* Texture Coord Binding specifies how (at what granularity) texture 
		 * coordinate data is supplied (â€œboundâ€�) for the Shape Rep in either 
		 * the Lossless Compressed Raw Vertex Data or Lossy Quantized Raw 
		 * Vertex Data collections. */
		int textureCoordBinding = getReader().readU8();

		/* Color Binding specifies how (at what granularity) color data is 
		 * supplied (â€œboundâ€�) for the Shape Rep in either the Lossless 
		 * Compressed Raw Vertex Data or Lossy Quantized Raw Vertex Data 
		 * collections. */
		int colorBinding = getReader().readU8();

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "*** Vertex Based Shape Compressed RepData ***" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Version Number: {}", versionNumber ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Normal Binding: {}", normalBinding ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Texture Coord Binding: {}", textureCoordBinding ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Color Binding: {}", colorBinding ); //$NON-NLS-1$
		}

		JTQuantizationParam quantParam = new JTQuantizationParam( getReader() );
		quantParam.read();

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "** Primitive List Indices **" ); //$NON-NLS-1$
		}

		/* Primitive List Indices is a vector of indices into the 
		 * uncompressed Raw Vertex Data marking the start/beginning of 
		 * primitives. Primitive List Indices uses the Int32 version of the 
		 * CODEC to compress and encode data. */
		this.primitiveListIndices = Int32Compression.read_VecI32_Int32CDP( getReader(), PredictorType.Stride1 );

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( " => Primitive List Indices ({})", this.primitiveListIndices.length ); //$NON-NLS-1$

			StringBuilder indices = new StringBuilder();
			for ( int primitiveListIndice : this.primitiveListIndices ) {
				indices.append( primitiveListIndice ).append( " " ); //$NON-NLS-1$
			}
			BundleAccessor.getLogger().info( "{}", indices.toString() ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
		}

		LossyQuantizedRawVertexData rawVertexData = new LossyQuantizedRawVertexData( this.reader );

		if ( quantParam.getBitsPerVertex() == 0 ) {
			readLosslessCompressedRawVertexData( normalBinding, textureCoordBinding, colorBinding );
			this.uncompressed = true;
		}
		else {
			rawVertexData.read( normalBinding, textureCoordBinding, colorBinding );
			this.quantVertexCoord = rawVertexData.getQuantVertex();
			this.vertexDataIndices = rawVertexData.getVertexDataIndices();
			this.normals = rawVertexData.getQuantVertexNorm().getNormals();
			this.uncompressed = false;
		}

//        if (testDisplay) {
//			Converter.display( this );
		//displayVertices();
//        }

	}

	private void readLosslessCompressedRawVertexData( int normalBinding, int textureCoordBinding, int colorBinding ) throws IOException
	{
		int uncompressedDataSize = getReader().readI32();
		int compressedDataSize = getReader().readI32();
		int len;
		if ( compressedDataSize > 0 ) {
			getReader().setInflating( true, compressedDataSize );
			len = uncompressedDataSize;
		}
		else {
			len = Math.abs( compressedDataSize );
		}

		int numFaces = this.primitiveListIndices.length - 1;
		int numVertices = this.primitiveListIndices[numFaces];
//        System.out.println(numFaces);
//        System.out.println(numVertices);

		this.normal = new double[numVertices * 3];
		this.vertex = new double[numVertices * 3];
		for ( int i = 0; i < numVertices; i++ ) {
			int j = i * 3;
			if ( textureCoordBinding == 1 ) {
				//reader.readF32();
				//reader.readF32();
			}
			if ( colorBinding == 1 ) {
				this.reader.readF32();
				this.reader.readF32();
				this.reader.readF32();
			}
			if ( normalBinding == 1 ) {
				this.normal[j + 0] = this.reader.readF32();
				this.normal[j + 1] = this.reader.readF32();
				this.normal[j + 2] = this.reader.readF32();

			}
			this.vertex[j + 0] = this.reader.readF32();
			this.vertex[j + 1] = this.reader.readF32();
			this.vertex[j + 2] = this.reader.readF32();
		}
	}
}
