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

import uk.ac.liv.jt.types.BBoxF32;
import uk.ac.liv.jt.types.Int32Range;

/** basic shape node. These type of nodes can not have children */
public class BaseShapeNodeElement extends BaseNodeElement {

    public BBoxF32 transformedBBox;
    public BBoxF32 untransformedBBox;
    public float area;
    public Int32Range nodeRange;
    public Int32Range vertexRange;
    public Int32Range ploygonRange;
    public int size;
    public float compressionLevel;

    @Override
    public void read() throws IOException {
        super.read();

		this.transformedBBox = this.reader.readBBoxF32();
		this.untransformedBBox = this.reader.readBBoxF32();
		this.area = this.reader.readF32();
		this.vertexRange = this.reader.readRange();
		this.nodeRange = this.reader.readRange();
		this.ploygonRange = this.reader.readRange();
		this.size = this.reader.readI32();
		this.compressionLevel = this.reader.readF32();
    }
}
