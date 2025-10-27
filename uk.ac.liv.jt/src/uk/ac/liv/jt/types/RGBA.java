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
package uk.ac.liv.jt.types;

import java.awt.Color;

/**
 *   A float representation of a colour. Used instead of {@link Color} so that it does not require 
 *   instatiating additional AWT system (for performance reasons).
 * @author fabio
 *
 */
public class RGBA
{

	public float r;
	public float g;
	public float b;
	public float a;

	public Color getColor()
	{
		return new Color( this.r, this.g, this.b, this.a );
	}

	public float[] getColorTable()
	{
		float[] ret = new float[4];
		ret[0] = this.r;
		ret[1] = this.g;
		ret[2] = this.b;
		ret[3] = this.a;
		return ret;
	}

	public RGBA( float r, float g, float b, float a )
	{
		super();
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public float getR()
	{
		return this.r;
	}

	public void setR( float r )
	{
		this.r = r;
	}

	public float getG()
	{
		return this.g;
	}

	public void setG( float g )
	{
		this.g = g;
	}

	public float getB()
	{
		return this.b;
	}

	public void setB( float b )
	{
		this.b = b;
	}

	public float getA()
	{
		return this.a;
	}

	public void setA( float a )
	{
		this.a = a;
	}

}
