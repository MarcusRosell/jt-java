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

import java.io.IOException;

import uk.ac.liv.jt.debug.DebugInfo;
import uk.ac.liv.jt.format.BitReader;
import uk.ac.liv.jt.internal.BundleAccessor;

public class Int32ProbCtxtTable
{

	/* A Probability Context Table is a trimmed and scaled histogram of the 
	 * input values. It tallies the frequencies of the several most frequently 
	 * occurring values. It is central to the operation of the arithmetic CODEC, 
	 * and gives all theinformation necessary to reconstruct the Huffman codes 
	 * for the Huffman CODEC. p.227
	 */

	private BitReader reader;
	Int32ProbCtxts probCtxt;

	long nbSymbolBits;
	long nbOccCountBits;
	long nbNextContextBits;

	Int32ProbCtxtEntry[] entries;

	public Int32ProbCtxtEntry[] getEntries()
	{
		return this.entries;
	}

	public Int32ProbCtxtTable( Int32ProbCtxts probCtxt, BitReader reader )
	{
		this.reader = reader;
		this.probCtxt = probCtxt;
	}

	public int getTotalCount()
	{
		int totalCount = 0;

		for ( Int32ProbCtxtEntry entrie : this.entries ) {
			totalCount += entrie.getOccCount();
		}

		return totalCount;
	}

	public void read( Boolean isFirstTable, int codecType ) throws IOException
	{

		// Header of the table
		long tableEntryCount = this.reader.readU32( 32 );
		this.nbSymbolBits = this.reader.readU32( 6 );
		this.nbOccCountBits = this.reader.readU32( 6 );

		this.probCtxt.nbOccCountBits = this.nbOccCountBits;

		if ( isFirstTable ) {
			this.probCtxt.setNbValueBits( this.reader.readU32( 6 ) );
		}

		this.nbNextContextBits = this.reader.readU32( 6 );

		if ( isFirstTable ) {
			this.probCtxt.setMinValue( this.reader.readU32( 32 ) );
		}

		if ( DebugInfo.debugCodec ) {
			BundleAccessor.getLogger().info( "Prob Context Table Entry Count: {}", tableEntryCount ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Number symbol bits: {}", this.nbSymbolBits ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Number occurence count bits: {}", this.nbOccCountBits ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Number value bits: {}", this.probCtxt.getNbValueBits() ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Number context bits: {}", this.nbNextContextBits ); //$NON-NLS-1$
			BundleAccessor.getLogger().info( "Min value: {}", this.probCtxt.getMinValue() ); //$NON-NLS-1$
		}

		// Probability Context Table Entries

		this.entries = new Int32ProbCtxtEntry[(int)tableEntryCount];
		long cumCount = 0;

		for ( int i = 0; i < tableEntryCount; i++ ) {

			long symbol = this.reader.readU32( (int)this.nbSymbolBits ) - 2;
			long occCount = this.reader.readU32( (int)this.nbOccCountBits );
			long associatedValue = 0;

			if ( codecType == Int32Compression.HUFFMAN_CODEC ) {
				associatedValue = this.reader.readU32( (int)this.probCtxt.getNbValueBits() );
			}
			else if ( codecType == Int32Compression.ARITHMETIC_CODEC ) {
				// For the first table the associated value is read
				if ( isFirstTable ) {
					associatedValue = this.reader.readU32( (int)this.probCtxt.getNbValueBits() ) + this.probCtxt.getMinValue();
					this.probCtxt.assValues.put( symbol, associatedValue );
					// For the second table we take the associated value of the 
					// symbol in the first table
				}
				else {
					associatedValue = this.probCtxt.assValues.get( symbol );
				}
			}
			int nextContext = (int)this.reader.readU32( (int)this.nbNextContextBits );

			this.entries[i] = new Int32ProbCtxtEntry( symbol, occCount, cumCount, associatedValue, nextContext );
			cumCount += occCount;

			if ( DebugInfo.debugCodec ) {
				BundleAccessor.getLogger().info( "{} => Symbol: {}, Occurence Count: {}, Cum Count : {}, Associated Value: {}, Next Context: {}", i, symbol, occCount, this.entries[i].getCumCount(), associatedValue, nextContext ); //$NON-NLS-1$
			}
		}

	}

	public long getNbValueBits()
	{
		return this.probCtxt.getNbValueBits();
	}

	public long getMinValue()
	{
		return this.probCtxt.getMinValue();
	}

	// Looks up the index of the context entry that falls just above
	// the accumulated count.
	public Int32ProbCtxtEntry lookupEntryByCumCount( long count )
	{
		long sum = this.entries[0].getOccCount();
		int idx = 0;

		while ( count >= sum ) {
			idx += 1;
			sum += this.entries[idx].getOccCount();
		}

		return this.entries[idx];
	}
}
