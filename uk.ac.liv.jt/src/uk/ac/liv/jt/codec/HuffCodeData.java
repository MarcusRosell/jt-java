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
package uk.ac.liv.jt.codec;

public class HuffCodeData
{
	/* The data associated to each nodes */

	int symbol;
	int codeLen;
	long bitCode;
	int assValue;
	int index;

	public HuffCodeData( int symbol, int assValue )
	{
		super();
		this.symbol = symbol;
		this.assValue = assValue;
	}

	public HuffCodeData( int symbol, int assValue, int codeLen, long bitCode )
	{
		super();
		this.symbol = symbol;
		this.assValue = assValue;
		this.codeLen = codeLen;
		this.bitCode = bitCode;
	}

	public int getSymbol()
	{
		return this.symbol;
	}

	public void setSymbol( int symbol )
	{
		this.symbol = symbol;
	}

	public long getCodeLen()
	{
		return this.codeLen;
	}

	public void setCodeLen( int codeLen )
	{
		this.codeLen = codeLen;
	}

	public long getBitCode()
	{
		return this.bitCode;
	}

	public void setBitCode( long bitCode )
	{
		this.bitCode = bitCode;
	}

	public String codeToString()
	{
		String tmp = Long.toBinaryString( this.bitCode );

		if ( tmp.length() != this.codeLen ) {
			// Add n "0" at the beginning where
			// n = codeLen - tmp.length()
			long nb_0 = this.codeLen - tmp.length();
			for ( int i = 0; i < nb_0; i++ ) {
				tmp = "0" + tmp; //$NON-NLS-1$
			}
		}

		return tmp;

	}

	@Override
	public String toString()
	{
		return String.format( "Code: %s => Symbol: %d (%d) %d", codeToString(), this.symbol, this.assValue, this.index ); //$NON-NLS-1$
	}

	public int getAssValue()
	{
		return this.assValue;
	}

	public void setAssValue( int assValue )
	{
		this.assValue = assValue;
	}

}
