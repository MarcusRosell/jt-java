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

public class PartitionNodeElement extends GroupNodeElement
{

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

	public float getArea()
	{
		return this.area;
	}

	public String getFilename()
	{
		return this.filename;
	}

	public Int32Range getNodeCountRange()
	{
		return this.nodeCountRange;
	}

	public int getPartitionFlags()
	{
		return this.partitionFlags;
	}

	public Int32Range getPolygonCountRange()
	{
		return this.polygonCountRange;
	}

	public BBoxF32 getReservedField()
	{
		return this.reservedField;
	}

	public BBoxF32 getTransformedBox()
	{
		return this.transformedBox;
	}

	public BBoxF32 getUntransformedBox()
	{
		return this.untransformedBox;
	}

	public Int32Range getVertexCountRange()
	{
		return this.vertexCountRange;
	}

	@Override
	public void read() throws IOException
	{
		super.read();

		this.partitionFlags = getReader().readI32();

		this.filename = getReader().readMbString();

		if ( (this.partitionFlags & 0x00000001) == 0 ) {
			this.transformedBox = getReader().readBBoxF32();
			//            System.out.println("Transformed Box: " + transformedBox);
		}
		else {
			this.reservedField = getReader().readBBoxF32();
		}
		this.area = getReader().readF32();

		this.vertexCountRange = this.reader.readRange();
		this.nodeCountRange = this.reader.readRange();
		this.polygonCountRange = this.reader.readRange();

		if ( (this.partitionFlags & 0x00000001) != 0 ) {
			this.untransformedBox = getReader().readBBoxF32();
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
	public String toString()
	{
		return "FILE: " + this.filename; //$NON-NLS-1$
	}

}
