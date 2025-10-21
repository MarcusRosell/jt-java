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

public class PrimitiveSetShapeNodeElement extends BaseShapeNodeElement {

    private int textureCoordBinding;
    private int colorBinding;
    private int psqpbitPerColor;
    private int psqpbitPerVertex;
    private short versionNumber;
    private int textureCoordGenType;

    @Override
    public void read() throws IOException {
        super.read();

        textureCoordBinding = reader.readI32();

        colorBinding = reader.readI32();
        psqpbitPerVertex = reader.readU8();
        psqpbitPerColor = reader.readU8();
        versionNumber = reader.readI16();
        if (versionNumber == 1)
            textureCoordGenType = reader.readI32();

    }

}
