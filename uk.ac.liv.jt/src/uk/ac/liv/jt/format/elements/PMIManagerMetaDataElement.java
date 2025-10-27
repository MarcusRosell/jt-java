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

import uk.ac.liv.jt.format.JTElement;
import uk.ac.liv.jt.internal.BundleAccessor;

public class PMIManagerMetaDataElement extends JTElement
{
	long nodeFlags;
	int[] attObjectId;
	int versionNumber;
	int reservedField;
	int associationCount;
	int stringCount;
	int userAttributeCount;
	int modelViewCount;
	int genericEntityCount;
	int cadTagFlag;
	int cadTagIndexCount;
	ModelView[] views;
	private String[] stringTable;
	private GenericEntry[] genericEntries;

//     @Override
//     public void read() throws IOException {
//    
//     versionNumber = getReader().readI16();
//     reservedField = getReader().readI16();
//     readPMIEntities();
//     associationCount = getReader().readI32();
//     userAttributeCount = getReader().readI32();
//     stringCount = getReader().readI32();
//     if (stringCount > 0)
//         stringTable = new String[stringCount];
//     for (int i=0;i<stringCount;i++)
//         stringTable[i] = reader.readString8();
//     if (versionNumber > 5) {
//         modelViewCount = getReader().readI32();
//         views= readModelViews(modelViewCount);
//         
//         genericEntityCount = getReader().readI32();
//         genericEntries = readGenericEntries();
//         
//     }
//     if (versionNumber > 7) {
//         cadTagFlag = getReader().readI32();
//         if (cadTagFlag == 1) {
//             cadTagIndexCount = getReader().readI32();
//         }
//     }
//     }

	private GenericEntry[] readGenericEntries()
	{
		// TODO Auto-generated method stub
		return null;
	}

	private ModelView[] readModelViews( int modelViewCount2 ) throws IOException
	{
		ModelView[] ret = new ModelView[modelViewCount2];
		for ( int i = 0; i < modelViewCount2; i++ ) {
			ModelView c = ret[i] = new ModelView();
			c.eye_direction = this.reader.readCoordF32();
			c.angle = this.reader.readF32();
			c.eye_pos = this.reader.readCoordF32();
			c.target_point = this.reader.readCoordF32();
			c.view_angle = this.reader.readCoordF32();
			c.viewport_diameter = this.reader.readF32();
			c.reserved1 = this.reader.readF32();
			c.reserved2 = this.reader.readI32();
			c.active_flag = this.reader.readI32();

			c.view_id = this.reader.readI32();
			c.view_name_string_id = this.reader.readI32();
		}
		return ret;
	}

	public void readPMIEntities() throws IOException
	{
		// PMI Dimension Entities
		BundleAccessor.getLogger().info( "     PMI Dimension Entities" ); //$NON-NLS-1$

		int dimensionCount = getReader().readI32();
		BundleAccessor.getLogger().info( "       Dimension count: {}" + dimensionCount ); //$NON-NLS-1$

		// PMI Note Entities
		BundleAccessor.getLogger().info( "     PMI Note Entities" ); //$NON-NLS-1$

		int noteCount = getReader().readI32();
		BundleAccessor.getLogger().info( "       Note count: {}", noteCount ); //$NON-NLS-1$

		// PMI Datum Feature Symbol Entities
		BundleAccessor.getLogger().info( "     PMI Datum Feature Symbol Entities" ); //$NON-NLS-1$

		int dfsCount = getReader().readI32();
		BundleAccessor.getLogger().info( "       DFS count: {}", dfsCount ); //$NON-NLS-1$

		// PMI Datum Target Entities
		BundleAccessor.getLogger().info( "     PMI Datum Target Entities" ); //$NON-NLS-1$

		int datumTargetCount = getReader().readI32();
		BundleAccessor.getLogger().info( "       Datum Target count: {}", datumTargetCount ); //$NON-NLS-1$

		// PMI Feature Control Frame Entities
		BundleAccessor.getLogger().info( "     PMI Feature Control Frame Entities" ); //$NON-NLS-1$

		int fcfCount = getReader().readI32();
		BundleAccessor.getLogger().info( "       FCF count: {}", fcfCount ); //$NON-NLS-1$

		// PMI Line Weld Entities
		BundleAccessor.getLogger().info( "     PMI Line Weld Entities" ); //$NON-NLS-1$

		int lineWeldCount = getReader().readI32();
		BundleAccessor.getLogger().info( "       Line Weld count: {}", lineWeldCount ); //$NON-NLS-1$

		// PMI Spot Weld Entities
		BundleAccessor.getLogger().info( "     PMI Spot Weld Entities" ); //$NON-NLS-1$

		int spotWeldCount = getReader().readI32();
		BundleAccessor.getLogger().info( "       Spot Weld count: {}", spotWeldCount ); //$NON-NLS-1$

		// PMI Surface Finish Entities
		BundleAccessor.getLogger().info( "     PMI Surface Finish Entities" ); //$NON-NLS-1$

		int sfCount = getReader().readI32();
		BundleAccessor.getLogger().info( "       SF count: {}", sfCount ); //$NON-NLS-1$

		// PMI Measurement Point Entities
		BundleAccessor.getLogger().info( "     PMI Measurement Point Entities" ); //$NON-NLS-1$

		int mpCount = getReader().readI32();
		BundleAccessor.getLogger().info( "       MP count: {}", mpCount ); //$NON-NLS-1$

		// PMI Locator Entities
		BundleAccessor.getLogger().info( "     PMI Locator Entities" ); //$NON-NLS-1$

		int locatorCount = getReader().readI32();
		BundleAccessor.getLogger().info( "       Locator count: {}", locatorCount ); //$NON-NLS-1$

		// PMI Reference Geometry Entities
		BundleAccessor.getLogger().info( "     PMI Reference Geometry Entities" ); //$NON-NLS-1$

		int referenceGeometryCount = getReader().readI32();
		BundleAccessor.getLogger().info( "       Reference Geometry count: {}", referenceGeometryCount ); //$NON-NLS-1$

		// PMI Design Group Entities
		BundleAccessor.getLogger().info( "     PMI Design Group Entities" ); //$NON-NLS-1$

		int designGroupCount = getReader().readI32();
		BundleAccessor.getLogger().info( "       Design Group Count: {}", designGroupCount ); //$NON-NLS-1$

		// PMI Coordinate System Entities
		BundleAccessor.getLogger().info( "     PMI Coordinate System Entities" ); //$NON-NLS-1$

		int coordSysCount = getReader().readI32();
		BundleAccessor.getLogger().info( "       Coord Sys count: {}", coordSysCount ); //$NON-NLS-1$

	}

}
