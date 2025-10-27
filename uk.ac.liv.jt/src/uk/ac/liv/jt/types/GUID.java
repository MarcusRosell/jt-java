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

import java.util.StringTokenizer;

/**
 * GUID are 16 bytes numbers used as unique identifiers in the JT system.
 * @author fabio
 *
 */

public class GUID
{
	/**
	 * Special GUID indicating the end of elements in a section of the Logical Scene graph
	 */
	private static final String DASH = "-"; //$NON-NLS-1$

	public static final GUID END_OF_ELEMENTS = fromString( "{FFFFFFFF-FFFF-FFFF-FF-FF-FF-FF-FF-FF-FF-FF}" ); //$NON-NLS-1$

	public static final int LENGTH = 16;
	private long w1;
	private int w2;
	private int w3;
	private int w4;
	private int w5;
	private int w6;
	private int w7;
	private int w8;
	private int w9;
	private int w10;
	private int w11;

	@Override
	public boolean equals( Object argObj )
	{
		Object obj = argObj;
		if ( obj instanceof String strObj ) {
			try {
				obj = fromString( strObj );
			}
			catch ( Exception e ) {
				return false;
			}
		}

		if ( !(obj instanceof GUID g) ) {
			return false;
		}

		return (g.w1 == this.w1 && g.w2 == this.w2 && g.w3 == this.w3 && g.w4 == this.w4 && g.w5 == this.w5
				&& g.w6 == this.w6 && g.w7 == this.w7 && g.w8 == this.w8 && g.w9 == this.w9
				&& g.w10 == this.w10 && g.w11 == this.w11);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "{" + Long.toHexString( this.w1 ).toUpperCase() + DASH //$NON-NLS-1$
				+ Integer.toHexString( this.w2 ).toUpperCase() + DASH
				+ Integer.toHexString( this.w3 ).toUpperCase() + DASH
				+ Integer.toHexString( this.w4 ).toUpperCase() + DASH
				+ Integer.toHexString( this.w5 ).toUpperCase() + DASH
				+ Integer.toHexString( this.w6 ).toUpperCase() + DASH
				+ Integer.toHexString( this.w7 ).toUpperCase() + DASH
				+ Integer.toHexString( this.w8 ).toUpperCase() + DASH
				+ Integer.toHexString( this.w9 ).toUpperCase() + DASH
				+ Integer.toHexString( this.w10 ).toUpperCase() + DASH
				+ Integer.toHexString( this.w11 ).toUpperCase() + "}" ); //$NON-NLS-1$

		return sb.toString();
	}

	/** Converts a string representation of a GUID to a GUID.
	 *  The String is expressed in the format {FFFFFFFF-FFFF-FFFF-FF-FF-FF-FF-FF-FF-FF-FF} with hexadecimal values.
	 * @param s the String
	 * @return the corresponding GUID
	 */
	public static GUID fromString( String s )
	{
		GUID g = new GUID();
		if ( s.charAt( 0 ) != '{' )
			throw new IllegalArgumentException();
		StringTokenizer st = new StringTokenizer( s.substring( 1, s.length() - 1 ), "-" );

		g.w1 = Long.parseLong( st.nextToken(), 16 );
		g.w2 = Integer.parseInt( st.nextToken(), 16 );
		g.w3 = Integer.parseInt( st.nextToken(), 16 );
		g.w4 = Integer.parseInt( st.nextToken(), 16 );
		g.w5 = Integer.parseInt( st.nextToken(), 16 );
		g.w6 = Integer.parseInt( st.nextToken(), 16 );
		g.w7 = Integer.parseInt( st.nextToken(), 16 );
		g.w8 = Integer.parseInt( st.nextToken(), 16 );
		g.w9 = Integer.parseInt( st.nextToken(), 16 );
		g.w10 = Integer.parseInt( st.nextToken(), 16 );
		g.w11 = Integer.parseInt( st.nextToken(), 16 );
		return g;
	}

	public static GUID of( long w1, int w2, int w3, int w4, int w5, int w6, int w7, int w8, int w9, int w10, int w11 )
	{
		GUID g = new GUID();
		g.w1 = w1;
		g.w2 = w2;
		g.w3 = w3;
		g.w4 = w4;
		g.w5 = w5;
		g.w6 = w6;
		g.w7 = w7;
		g.w8 = w8;
		g.w9 = w9;
		g.w10 = w10;
		g.w11 = w11;
		return g;
	}

	@Override
	public int hashCode()
	{
		return (int)(this.w1 + this.w2 + this.w3 + this.w4 + this.w5 + this.w6 + this.w7 + this.w8 + this.w9 + this.w10 + this.w11);
	}

}
