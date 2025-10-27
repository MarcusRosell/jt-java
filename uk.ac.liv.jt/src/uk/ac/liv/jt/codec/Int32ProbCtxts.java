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
package uk.ac.liv.jt.codec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import uk.ac.liv.jt.debug.DebugInfo;
import uk.ac.liv.jt.format.BitReader;
import uk.ac.liv.jt.format.ByteReader;
import uk.ac.liv.jt.internal.BundleAccessor;

public class Int32ProbCtxts
{

	/* Int32 Probability Contexts data collection is a list of Probability 
	 * Context Tables. The Int32 Probability Contexts data collection is only 
	 * present for Huffman and Arithmetic CODEC Types. p.226
	 */

	private ByteReader reader;
	long nbValueBits;
	long minValue;
	int contextTableCount = 0;
	public Int32ProbCtxtTable[] probContextTables = new Int32ProbCtxtTable[0];
	int[] outOfBandValues;

	public long nbOccCountBits;

	public Map<Object, Long> assValues = new HashMap<>();

	public int[] getOutOfBandValues()
	{
		return this.outOfBandValues;
	}

	public void setOutOfBandValues( int[] outOfBandValues )
	{
		this.outOfBandValues = outOfBandValues;
	}

	public Int32ProbCtxtTable[] getProbContextTables()
	{
		return this.probContextTables;
	}

	public int getContextTableCount()
	{
		return this.probContextTables.length;
	}

	public Int32ProbCtxts( ByteReader reader )
	{
		this.reader = reader;
		this.outOfBandValues = new int[0];
	}

	public long getNbValueBits()
	{
		return this.nbValueBits;
	}

	public void setNbValueBits( long nbValueBits )
	{
		this.nbValueBits = nbValueBits;
	}

	public long getMinValue()
	{
		return this.minValue;
	}

	public void setMinValue( long minValue )
	{
		this.minValue = minValue;
	}

	/*
	 * totalCount â€“ Refers to the sum of the â€œOccurrence Countâ€� values for all
	 * the symbols associated with a Probability Context.
	 */
	public int getTotalCount()
	{
		int totalCount = 0;

		for ( Int32ProbCtxtTable probContextTable : this.probContextTables ) {
			totalCount += probContextTable.getTotalCount();
		}
		return totalCount;
	}

	// Returns the probability context for a given index
	public Int32ProbCtxtTable getContext( int ctxt )
	{
		return this.probContextTables[ctxt];
	}

	public void read( int codecType ) throws IOException
	{
		this.contextTableCount = this.reader.readU8();

		if ( DebugInfo.debugCodec ) {
			BundleAccessor.getLogger().info( "\n == Probability Context ==\n" ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Prob Context Table Count: {}", this.contextTableCount ); //$NON-NLS-1$
		}

		BitReader bitReader = new BitReader( this.reader );

		this.probContextTables = new Int32ProbCtxtTable[this.contextTableCount];

		for ( int i = 0; i < this.contextTableCount; i++ ) {
			this.probContextTables[i] = new Int32ProbCtxtTable( this, bitReader );
			this.probContextTables[i].read( i == 0, codecType );
		}

		// Alignments bits : See rev-D specification on page 228
		if ( bitReader.getNbBitsLeft() > 0 ) {
			int aligmentsBits = bitReader.getBitBuf().readAsInt( bitReader.getNbBitsLeft() );
			if ( aligmentsBits != 0 ) {
				BundleAccessor.getLogger().info( "Problem with alignments bits in the parsing of a probabilistic context table" ); //$NON-NLS-1$
			}
		}
	}

}
