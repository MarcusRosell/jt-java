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

import uk.ac.liv.jt.codec.DeeringNormalCodec;
import uk.ac.liv.jt.codec.Int32Compression;
import uk.ac.liv.jt.codec.Predictors.PredictorType;
import uk.ac.liv.jt.debug.DebugInfo;
import uk.ac.liv.jt.internal.BundleAccessor;
import uk.ac.liv.jt.types.U32Vector;
import uk.ac.liv.jt.types.Vec3D;

/** The Quantized Vertex Normal Array data collection contains the 
 * quantization data/representation for a set of vertex normals. Quantized 
 * Vertex Normal Array data collection is only present if previously read
 * Normal Binding value is not equal to zero. p.239 */
public class QuantizedVertexNormalArray
{

	ByteReader reader;

	/* Normals for the vertices */
	Vec3D[] normals;

	public QuantizedVertexNormalArray( ByteReader reader )
	{
		super();
		this.reader = reader;

	}

	public void read() throws IOException
	{
		/* Number of Bits specifies the quantized size (i.e. the number of bits
		 *  of precision) for the Theta and Psi	angles*/
		int nbBits = this.reader.readU8();

		/* Normal Count specifies the count (number of unique) Normal Codes. */
		int normalCount = this.reader.readI32();

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "** Sextant Codes: **" ); //$NON-NLS-1$
		}
		/* Sextant Codes is a vector of â€œcodesâ€� (one per normal) for a set of 
		 * normals identifying which Sextant of the corresponding sphere Octant 
		 * each normal is located in. */
		U32Vector sextantCodes = Int32Compression.read_VecU32_Int32CDP( this.reader, PredictorType.Lag1 );

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Float values ({})", sextantCodes.length() ); //$NON-NLS-1$
		}

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "** Octant Codes: **" ); //$NON-NLS-1$
		}
		/* Octant Codes is a vector of â€œcodesâ€� (one per normal) for a set of
		 * normals identifying which sphere Octant each normal is located in. */
		U32Vector octantCodes = Int32Compression.read_VecU32_Int32CDP( this.reader, PredictorType.Lag1 );

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Float values ({})", octantCodes.length() ); //$NON-NLS-1$
		}

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "** Theta Codes: **" ); //$NON-NLS-1$
		}
		/* Theta Codes is a vector of â€œcodesâ€� (one per normal) for a set of 
		 * normals representing in Sextant coordinates the quantized theta 
		 * angle for each normalâ€™s location on the unit radius sphere; where 
		 * theta angle is defined as the angle in spherical coordinates about 
		 * the Y-axis on a unit radius sphere. */
		U32Vector thetaCodes = Int32Compression.read_VecU32_Int32CDP( this.reader, PredictorType.Lag1 );

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Float values ({})", thetaCodes.length() ); //$NON-NLS-1$
		}

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "** Psi Codes: **" ); //$NON-NLS-1$
		}
		/* Psi Codes is a vector of â€œcodesâ€� (one per normal) for a set of 
		 * normals representing in Sextant coordinates the quantized Psi angle 
		 * for each normalâ€™s location on the unit radius sphere; where Psi 
		 * angle is defined as the longitudinal angle in spherical coordinates 
		 * from the y = 0 plane on the unit radius sphere. */
		U32Vector psiCodes = Int32Compression.read_VecU32_Int32CDP( this.reader, PredictorType.Lag1 );

		if ( DebugInfo.debugMode ) {
			BundleAccessor.getLogger().info( "" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Float values ({})", psiCodes.length() ); //$NON-NLS-1$
		}

		this.normals = new Vec3D[psiCodes.length()];
		DeeringNormalCodec deeringCodec = new DeeringNormalCodec( nbBits );

		for ( int i = 0; i < psiCodes.length(); i++ ) {
			this.normals[i] = deeringCodec.convertCodeToVec( sextantCodes.get( i ), octantCodes.get( i ), thetaCodes.get( i ), psiCodes.get( i ) );
		}
	}

	public Vec3D[] getNormals()
	{
		return this.normals;
	}

}
