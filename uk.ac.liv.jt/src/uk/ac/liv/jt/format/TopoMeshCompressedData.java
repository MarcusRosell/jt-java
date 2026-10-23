/*******************************************************************************
 * This Library is :
 * 
 *     Copyright © 2026 Marcus Rosell - all rights reserved
 *     marcus.rosell@solme.se
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

import uk.ac.liv.jt.codec.DeeringNormalCodec;
import uk.ac.liv.jt.codec.DualVFMesh;
import uk.ac.liv.jt.codec.Int32CDP2;
import uk.ac.liv.jt.codec.Int32CDP2.Predictor;
import uk.ac.liv.jt.codec.TopoMeshDecoder;
import uk.ac.liv.jt.debug.DebugInfo;
import uk.ac.liv.jt.internal.BundleAccessor;
import uk.ac.liv.jt.types.Vec3D;

/**
 * Reads the Topologically Compressed Rep Data used by the JT 9 and later Tri-Strip Set Shape LOD
 * Elements: the topology of the mesh, compressed as a dual vertex-facet mesh, followed by the
 * Topologically Compressed Vertex Records. The result is exported as a list of triangles, with one
 * vertex coordinate and normal per triangle corner.
 */
public class TopoMeshCompressedData
{
	private static final long COORD_BINDING = 0x7L;
	private static final long NORMAL_BINDING = 0x8L;
	private static final long COLOUR_BINDING = 0x30L;
	private static final long FLAG_BINDING = 0x40L;
	private static final long AUX_BINDING = 0x8000000000000000L;

	private final ByteReader reader;

	/** coordinates (x, y, z) of each topological vertex */
	private float[] coords;
	/** normals (x, y, z) of each vertex attribute record, null if absent */
	private float[] normals;

	/** coordinates, 3 per triangle corner */
	private double[] triangleVertices;
	/** normals, 3 per triangle corner */
	private double[] triangleNormals;
	private int nTriangles;

	public TopoMeshCompressedData( ByteReader reader )
	{
		this.reader = reader;
	}

	public int getTriangleCount()
	{
		return this.nTriangles;
	}

	/** coordinates of the triangle corners (x, y, z per corner, 3 corners per triangle) */
	public double[] getTriangleVertices()
	{
		return this.triangleVertices;
	}

	/** normals of the triangle corners (x, y, z per corner, 3 corners per triangle) */
	public double[] getTriangleNormals()
	{
		return this.triangleNormals;
	}

	/**
	 * Reads the Topologically Compressed Rep Data and decodes the mesh.
	 */
	public void read() throws IOException
	{
		int[][] degreeSymbols = new int[8][];
		for ( int i = 0; i < 8; i++ ) {
			degreeSymbols[i] = Int32CDP2.readVecI32( this.reader, Predictor.NULL );
		}
		int[] valenceSymbols = Int32CDP2.readVecI32( this.reader, Predictor.NULL );
		int[] groupSymbols = Int32CDP2.readVecI32( this.reader, Predictor.NULL );
		int[] flagSymbols = Int32CDP2.readVecI32( this.reader, Predictor.LAG1 );
		long[][] attrMasks = new long[8][];
		for ( int i = 0; i < 8; i++ ) {
			int[] lsb = Int32CDP2.readVecI32( this.reader, Predictor.NULL );
			attrMasks[i] = new long[lsb.length];
			for ( int k = 0; k < lsb.length; k++ ) {
				attrMasks[i][k] = lsb[k] & 0xffffffffL;
			}
		}
		// the most significant bits of the masks of the 8th context
		int[] msb = Int32CDP2.readVecI32( this.reader, Predictor.NULL );
		for ( int k = 0; k < msb.length && k < attrMasks[7].length; k++ ) {
			attrMasks[7][k] |= (msb[k] & 0xffffffffL) << 32;
		}
		if ( this.reader.MAJOR_VERSION < 10 ) {
			// JT 9 stores them in two fields: 30 bits, then the remaining ones
			int[] msb2 = Int32CDP2.readVecI32( this.reader, Predictor.NULL );
			for ( int k = 0; k < msb2.length && k < attrMasks[7].length; k++ ) {
				attrMasks[7][k] |= (msb2[k] & 0xffffffffL) << 62;
			}
		}
		int[] attrMasksLarge = readVecU32();
		int[] splitFaceSymbols = Int32CDP2.readVecI32( this.reader, Predictor.LAG1 );
		int[] splitPosSymbols = Int32CDP2.readVecI32( this.reader, Predictor.NULL );
		this.reader.readU32(); // composite hash

		TopoMeshDecoder decoder = new TopoMeshDecoder( degreeSymbols, valenceSymbols, groupSymbols, flagSymbols, attrMasks, attrMasksLarge,
				splitFaceSymbols, splitPosSymbols );
		DualVFMesh mesh = decoder.decode();

		readVertexRecords();

		buildTriangles( mesh );
	}

	/**
	 * Reads the Topologically Compressed Vertex Records: the unique coordinates of the topological
	 * vertices, and the vertex attribute records (normals...).
	 */
	private void readVertexRecords() throws IOException
	{
		long bindings = this.reader.readI64();
		// Quantization Parameters (the quantizers of each array are used instead). They are present
		// in JT 9 and JT 10.3 files, although not documented by the JT 10.0 reference
		new JTQuantizationParam( this.reader ).read();
		int nTopoVertices = this.reader.readI32();
		if ( nTopoVertices <= 0 ) {
			this.coords = new float[0];
			return;
		}
		int nAttributes = this.reader.readI32();
		if ( (bindings & COORD_BINDING) != 0 ) {
			this.coords = readVertexCoordinates();
		}
		if ( (bindings & NORMAL_BINDING) != 0 ) {
			this.normals = readVertexNormals();
		}
		// the remaining vertex attributes (colours, texture coordinates, flags...) are not used
		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().debug( "Vertex records: bindings {}, {} topological vertices, {} attributes", Long.toHexString( bindings ), //$NON-NLS-1$
					nTopoVertices, nAttributes );
		}
		if ( (bindings & (COLOUR_BINDING | FLAG_BINDING | AUX_BINDING | 0xffffffffff00L)) != 0 ) {
			BundleAccessor.getLogger().debug( "Vertex colours, texture coordinates, flags and auxiliary fields are ignored" ); //$NON-NLS-1$
		}
	}

	/** Reads a Compressed Vertex Coordinate Array */
	private float[] readVertexCoordinates() throws IOException
	{
		int nVertices = this.reader.readI32();
		int nComponents = this.reader.readU8();
		float[] min = new float[nComponents];
		float[] max = new float[nComponents];
		int[] nBits = new int[nComponents];
		// Point Quantizer Data: one Uniform Quantizer Data per component
		for ( int c = 0; c < nComponents; c++ ) {
			min[c] = this.reader.readF32();
			max[c] = this.reader.readF32();
			nBits[c] = this.reader.readU8();
		}
		float[] result = new float[nVertices * 3];
		for ( int c = 0; c < nComponents; c++ ) {
			int[] codes = nBits[c] == 0 ? readBinaryFloats( Predictor.LAG1 ) : Int32CDP2.readVecI32( this.reader, Predictor.LAG1 );
			if ( codes.length != nVertices ) {
				throw new IOException( "Vertex coordinate count mismatch" ); //$NON-NLS-1$
			}
			if ( c >= 3 ) {
				continue;
			}
			for ( int i = 0; i < nVertices; i++ ) {
				result[i * 3 + c] = nBits[c] == 0 ? Float.intBitsToFloat( codes[i] ) : dequantize( codes[i], min[c], max[c], nBits[c] );
			}
		}
		this.reader.readI32(); // vertex coordinate hash
		return result;
	}

	/** Reads a Compressed Vertex Normal Array */
	private float[] readVertexNormals() throws IOException
	{
		int nNormals = this.reader.readI32();
		int nComponents = this.reader.readU8();
		int quantBits = this.reader.readU8();
		float[] result = new float[nNormals * 3];
		if ( quantBits == 0 ) {
			for ( int c = 0; c < nComponents; c++ ) {
				int[] values = readBinaryFloats( Predictor.NULL );
				if ( values.length != nNormals ) {
					throw new IOException( "Vertex normal count mismatch" ); //$NON-NLS-1$
				}
				if ( c < 3 ) {
					for ( int i = 0; i < nNormals; i++ ) {
						result[i * 3 + c] = Float.intBitsToFloat( values[i] );
					}
				}
			}
		}
		else if ( this.reader.MAJOR_VERSION < 10 ) {
			// JT 9: separate arrays for the sextant, octant, theta and psi codes
			int[] sextants = Int32CDP2.readVecI32( this.reader, Predictor.NULL );
			int[] octants = Int32CDP2.readVecI32( this.reader, Predictor.NULL );
			int[] thetas = Int32CDP2.readVecI32( this.reader, Predictor.NULL );
			int[] psis = Int32CDP2.readVecI32( this.reader, Predictor.NULL );
			if ( sextants.length != nNormals || octants.length != nNormals || thetas.length != nNormals || psis.length != nNormals ) {
				throw new IOException( "Vertex normal count mismatch" ); //$NON-NLS-1$
			}
			DeeringNormalCodec codec = new DeeringNormalCodec( quantBits );
			for ( int i = 0; i < nNormals; i++ ) {
				Vec3D n = codec.convertCodeToVec( sextants[i], octants[i], thetas[i], psis[i] );
				result[i * 3] = (float)n.getX();
				result[i * 3 + 1] = (float)n.getY();
				result[i * 3 + 2] = (float)n.getZ();
			}
		}
		else {
			int[] codes = Int32CDP2.readVecI32( this.reader, Predictor.NULL );
			if ( codes.length != nNormals ) {
				throw new IOException( "Vertex normal count mismatch" ); //$NON-NLS-1$
			}
			DeeringNormalCodec codec = new DeeringNormalCodec( quantBits );
			for ( int i = 0; i < nNormals; i++ ) {
				long code = codes[i] & 0xffffffffL;
				int shift = 2 * quantBits;
				long mask = (1L << quantBits) - 1;
				Vec3D n = codec.convertCodeToVec( (code >> (shift + 3)) & 0x7, (code >> shift) & 0x7, (code >> quantBits) & mask, code & mask );
				result[i * 3] = (float)n.getX();
				result[i * 3 + 1] = (float)n.getY();
				result[i * 3 + 2] = (float)n.getZ();
			}
		}
		this.reader.readU32(); // vertex normal hash
		return result;
	}

	/**
	 * Reads the binary representation of an array of floats (lossless compression). JT 9 stores
	 * them in two packets, the exponents (with the sign) and the mantissas, JT 10 in a single one.
	 */
	private int[] readBinaryFloats( Predictor predictor ) throws IOException
	{
		if ( this.reader.MAJOR_VERSION >= 10 ) {
			return Int32CDP2.readVecI32( this.reader, predictor );
		}
		int[] exponents = Int32CDP2.readVecI32( this.reader, predictor );
		int[] mantissas = Int32CDP2.readVecI32( this.reader, predictor );
		if ( exponents.length != mantissas.length ) {
			throw new IOException( "Exponent and mantissa count mismatch" ); //$NON-NLS-1$
		}
		int[] values = new int[exponents.length];
		for ( int i = 0; i < values.length; i++ ) {
			values[i] = (exponents[i] << 23) | mantissas[i];
		}
		return values;
	}

	private static float dequantize( int code, float min, float max, int nBits )
	{
		long maxCode = nBits < 32 ? (1L << nBits) - 1 : 0xffffffffL;
		return (float)(min + (code & 0xffffffffL) * ((double)max - min) / maxCode);
	}

	private int[] readVecU32() throws IOException
	{
		int n = this.reader.readI32();
		int[] values = new int[n];
		for ( int i = 0; i < n; i++ ) {
			values[i] = this.reader.readI32();
		}
		return values;
	}

	/**
	 * Exports the triangles of the dual mesh: each dual vertex not flagged as a cover face is a
	 * polygon (normally a triangle) of the original mesh, whose corners are the dual faces around it.
	 */
	private void buildTriangles( DualVFMesh mesh ) throws IOException
	{
		int nCorners = 0;
		for ( int v = 0; v < mesh.numVts(); v++ ) {
			if ( mesh.vtxFlags( v ) == 0 && mesh.valence( v ) >= 3 ) {
				nCorners += 3 * (mesh.valence( v ) - 2);
			}
		}
		this.nTriangles = nCorners / 3;
		this.triangleVertices = new double[nCorners * 3];
		this.triangleNormals = new double[nCorners * 3];
		int iCorner = 0;
		int[] polygon = new int[16];
		for ( int v = 0; v < mesh.numVts(); v++ ) {
			int valence = mesh.valence( v );
			if ( mesh.vtxFlags( v ) != 0 || valence < 3 ) {
				continue;
			}
			if ( polygon.length < valence ) {
				polygon = new int[valence];
			}
			for ( int i = 0; i < valence; i++ ) {
				polygon[i] = mesh.face( v, i );
				if ( polygon[i] < 0 ) {
					throw new IOException( "Incomplete mesh topology" ); //$NON-NLS-1$
				}
			}
			// fan triangulation of the polygon
			for ( int i = 1; i < valence - 1; i++ ) {
				setCorner( mesh, v, polygon[0], iCorner++ );
				setCorner( mesh, v, polygon[i], iCorner++ );
				setCorner( mesh, v, polygon[i + 1], iCorner++ );
			}
		}
	}

	private void setCorner( DualVFMesh mesh, int iDualVtx, int iDualFace, int iCorner ) throws IOException
	{
		// the dual faces are the topological vertices, in the order of the vertex coordinates
		if ( (iDualFace + 1) * 3 > this.coords.length ) {
			throw new IOException( "Missing vertex coordinates" ); //$NON-NLS-1$
		}
		System.arraycopy( toDouble( this.coords, iDualFace ), 0, this.triangleVertices, iCorner * 3, 3 );
		if ( this.normals != null ) {
			int iAttr = attributeOf( mesh, iDualFace, iDualVtx );
			if ( iAttr >= 0 && (iAttr + 1) * 3 <= this.normals.length ) {
				System.arraycopy( toDouble( this.normals, iAttr ), 0, this.triangleNormals, iCorner * 3, 3 );
			}
		}
	}

	/**
	 * Returns the vertex attribute record of the topological vertex (dual face) for the polygon
	 * (dual vertex): the attribute mask has one bit per polygon around the vertex, set when the
	 * polygon starts a new attribute record, unset when it shares the record of the previous one.
	 */
	private static int attributeOf( DualVFMesh mesh, int iDualFace, int iDualVtx )
	{
		int nAttrs = mesh.numAttrs( iDualFace );
		if ( nAttrs == 0 ) {
			return -1;
		}
		int degree = mesh.degree( iDualFace );
		int slot = mesh.findVtxSlot( iDualFace, iDualVtx );
		if ( slot < 0 ) {
			return -1;
		}
		// find the slot starting the attribute record, going backwards (cyclically)
		int start = slot;
		while ( !mesh.attrMaskBit( iDualFace, start ) ) {
			start = start == 0 ? degree - 1 : start - 1;
		}
		// the rank of this slot among the set bits gives the attribute slot
		int rank = 0;
		for ( int i = 0; i < start; i++ ) {
			if ( mesh.attrMaskBit( iDualFace, i ) ) {
				rank++;
			}
		}
		return mesh.faceAttr( iDualFace, rank );
	}

	private static double[] toDouble( float[] values, int index )
	{
		return new double[] { values[index * 3], values[index * 3 + 1], values[index * 3 + 2] };
	}
}
