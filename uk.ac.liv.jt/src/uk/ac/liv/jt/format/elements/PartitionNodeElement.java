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
/**
 * basically indicates the root for the lsg; used also when referenced from
 * external files
 */

public class PartitionNodeElement extends GroupNodeElement {

	public static final int MAX_VERTICES = 400000;
    public int partitionFlags;
    public String filename;
    public BBoxF32 transformedBox;
    public BBoxF32 reservedField;
    public float area;
    public Int32Range vertexCountRange;

    public Int32Range nodeCountRange;

    public Int32Range polygonCountRange;

    public BBoxF32 untransformedBox;

    public float getArea() {
        return area;
    }

    public String getFilename() {
        return filename;
    }

    public Int32Range getNodeCountRange() {
        return nodeCountRange;
    }

    public int getPartitionFlags() {
        return partitionFlags;
    }

    public Int32Range getPolygonCountRange() {
        return polygonCountRange;
    }

    public BBoxF32 getReservedField() {
        return reservedField;
    }

    public BBoxF32 getTransformedBox() {
        return transformedBox;
    }

    public BBoxF32 getUntransformedBox() {
        return untransformedBox;
    }

    public Int32Range getVertexCountRange() {
        return vertexCountRange;
    }

    @Override
    public void read() throws IOException {
        super.read();

        partitionFlags = getReader().readI32();

        filename = getReader().readMbString();

        if ((partitionFlags & 0x00000001) == 0) {
            transformedBox = getReader().readBBoxF32();
            //            System.out.println("Transformed Box: " + transformedBox);
        } else
            reservedField = getReader().readBBoxF32();

        area = getReader().readF32();

        vertexCountRange = reader.readRange();
        nodeCountRange = reader.readRange();
        polygonCountRange = reader.readRange();

        if ((partitionFlags & 0x00000001) != 0) {
            untransformedBox = getReader().readBBoxF32();
            //            System.out.println("Untransformed Box: " + untransformedBox);
        }


        //        System.out.println("Partition Flags: " + partitionFlags);
        //        System.out.println("Filename: " + filename);
        //        System.out.println("Area: " + area);
        //        System.out.println("Vertex: " + vertexCountRange);
        //        System.out.println("Node: " + nodeCountRange);
        //        System.out.println("Polygon: " + polygonCountRange);




    }

    @Override
    public String toString() {
        return "FILE: " + this.filename;
    }

}
