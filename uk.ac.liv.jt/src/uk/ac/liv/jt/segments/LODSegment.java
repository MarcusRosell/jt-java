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
package uk.ac.liv.jt.segments;

import java.io.IOException;

import uk.ac.liv.jt.debug.DebugInfo;
import uk.ac.liv.jt.format.JTElement;
import uk.ac.liv.jt.internal.BundleAccessor;
/** 
 * Shape LOD Segment contains an Element that defines the geometric shape 
 * definition data (e.g. vertices, polygons, normals, etc) for a particular 
 * shape Level Of Detail or alternative representation. (p. 117)
 */

public class LODSegment extends JTSegment {
	
    int level;
    public JTElement e;

    protected LODSegment(int level) {
        this.level = level;
    }

    @Override
    public void read() throws IOException {
        super.read();

        if (DebugInfo.debugMode) {

			BundleAccessor.getLogger().debug( "- Header" ); //$NON-NLS-1$
			BundleAccessor.getLogger().debug( "  Id: {}", this.id ); //$NON-NLS-1$
			BundleAccessor.getLogger().debug( "  Type: {}", this.segType ); //$NON-NLS-1$
			BundleAccessor.getLogger().debug( "  Length: {}", this.length ); //$NON-NLS-1$
			BundleAccessor.getLogger().debug( "" ); //$NON-NLS-1$

			BundleAccessor.getLogger().debug( "**** Shape LOD Element ****" ); //$NON-NLS-1$
			BundleAccessor.getLogger().debug( "" ); //$NON-NLS-1$
        }

		JTElement element = JTElement.createJTElement( this.reader );

        if (DebugInfo.debugMode) {
			BundleAccessor.getLogger().debug( "Element length: {}", element.getLength() ); //$NON-NLS-1$
			BundleAccessor.getLogger().debug( "Object Type Id: {}", element.getId() ); //$NON-NLS-1$
			BundleAccessor.getLogger().debug( "Object Base Type: {}", element.getObjectBaseType() ); //$NON-NLS-1$
        }

        /* Shape LOD Element p. 118 */        
        element.read();
		this.e = element;
    }
}
