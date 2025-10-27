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

import java.nio.ByteBuffer;

import uk.ac.liv.jt.format.BitBuffer;

public class ArithmeticCodec
{

	/* Arithmetic encoding is a lossless compression algorithm that replaces an 
	 * input stream of symbols or bytes with a single fixed point output number 
	 * (i.e. only the mantissa bits to the right of the binary point are output 
	 * from MSB to LSB). The total number of bits needed in the output number 
	 * is dependent upon the length/complexity of the input message (i.e. the 
	 * longer the input message the more bits needed in the output number). 
	 * This single fixed point number output from an arithmetic encoding 
	 * process must be uniquely decodable to create the exact stream of input 
	 * symbols that were used to create it.
	 */

	int code; // Present input code value, for decoding only
	int low; // Start of the current code range
	int high; // End of the current code range

	long bitBuffer; // Temporary i/o buffer
	int nBits; // Number of bits in _bitBuffer

	BitBuffer encodedBits; // The bitbuffer

	public ArithmeticCodec()
	{
		super();
		this.code = 0x0000;
		this.low = 0x0000;
		this.high = 0xffff;

		this.bitBuffer = 0x00000000;
		this.nBits = 0;
	}

	public int[] decode( byte[] encodedBytes, int codeTextLength,
			Int32ProbCtxts probCtxt, int numSymbolsToRead, int valueElelemtCount )
	{
		//ArrayList<Integer> tmpList = new ArrayList<Integer>();
		int[] result = new int[valueElelemtCount];
		int position = 0;
		ArithmeticProbabilityRange newSymbolRange;
		int currContext = 0;
		int dummyTotalBits;
		int symbolsCurrCtx;

		int cptOutOfBand = 0;
		int[] outofBandValues = probCtxt.getOutOfBandValues();

		Int32ProbCtxtTable pCurrContext;

		int nBitsRead = -1;

		this.encodedBits = new BitBuffer( ByteBuffer.wrap( encodedBytes ) );

		this.bitBuffer = this.encodedBits.readAsInt( 32 ) & 0xFFFFFFFFL;

		this.low = 0x0000;
		this.high = 0xffff;

		this.code = (int)(this.bitBuffer >> 16);
		this.bitBuffer = (this.bitBuffer << 16) & 0xFFFFFFFFL;

		this.nBits = 16;

		// Begin decoding
		// Returns index of the first context entry and total number of bits
		// pDriver->getDecodeData(currContext, dummyTotalBits);

		for ( int ii = 0; ii < numSymbolsToRead; ii++ ) {
			// Returns the probability context for a given index
			pCurrContext = probCtxt.getContext( currContext );

			symbolsCurrCtx = pCurrContext.getTotalCount();

			long rescaledCode = ((((long)(this.code - this.low) + 1) * symbolsCurrCtx - 1) / ((long)(this.high - this.low) + 1));

			Int32ProbCtxtEntry currEntry = pCurrContext.lookupEntryByCumCount( rescaledCode );

			newSymbolRange = new ArithmeticProbabilityRange( currEntry.getCumCount(), currEntry.getCumCount()
					+ currEntry.getOccCount(), symbolsCurrCtx );

			removeSymbolFromStream( newSymbolRange );

			int symbol = (int)currEntry.getSymbol();
			int outValue;

			if ( (symbol == -2) && (currContext == 0) ) {
				outValue = outofBandValues[cptOutOfBand];
				cptOutOfBand++;
			}
			else {
				outValue = (int)currEntry.getAssociatedValue();
			}

			if ( (symbol != -2) || (currContext == 0) ) {
				result[position++] = outValue;
			}
			//tmpList.add(outValue);

			/*  if ( currContext == 0 ) {
				if ( symbol == -2 ) {
			        outValue = outofBandValues[cptOutOfBand];
			        cptOutOfBand++;
			    } else {
			        outValue = (int) currEntry.getAssociatedValue();
			    }
				tmpList.add(outValue);
				
			} else {
				if ( symbol != -2 )
			    	tmpList.add((int) currEntry.getAssociatedValue()) ;
			}*/

			currContext = currEntry.getNextContext();
		}

//        int[] res = new int[tmpList.size()];
//        for (int i = 0; i < tmpList.size(); i++)
//            res[i] = tmpList.get(i);
		return result;
	}

	private void removeSymbolFromStream( ArithmeticProbabilityRange sym )
	{

		// First, the range is expanded to account for the symbol removal.
		int range = this.high - this.low + 1;
		this.high = this.low + (int)((range * sym.getHigh()) / sym.getScale() - 1);
		this.low = this.low + (int)((range * sym.getLow()) / sym.getScale());

		// Next, any possible bits are shipped out.
		for ( ;; ) {
			// If the MSB match, the bits will be shifted out.
			if ( ((~(this.high ^ this.low)) & 0x8000) != 0 ) // Should be equal to 0x8000
			{}
			// 2nd MSB of high is 0 and 2nd MSB of low is 1
			else if ( (this.low & 0x4000) == 0x4000 && (this.high & 0x4000) == 0 ) {
				// Underflow is threatening, shift out 2nd most signif digit.
				this.code ^= 0x4000;
				this.low &= 0x3fff;
				this.high |= 0x4000;
			}
			else {
				// Nothing can be shifted out, so return.
				return;
			}

			this.low <<= 1;
			this.low &= 0xFFFF; // int are on 32 bits, we want to get rid of the 1st
			// 2 bytes when we shift
			this.high <<= 1;
			this.high &= 0xFFFF; // int are on 32 bits, we want to get rid of the 1st
			// 2 bytes when we shift
			this.high |= 1;
			this.code <<= 1;
			this.code &= 0xFFFF; // int are on 32 bits, we want to get rid of the 1st
			// 2 bytes when we shift

			if ( this.nBits == 0 ) {
				this.bitBuffer = this.encodedBits.readAsInt( 32 ) & 0xFFFFFFFFL;
				this.nBits = 32;
			}

			// Add the msb of bitbuffer as the lsb of code
			this.code |= (int)(this.bitBuffer >> 31);

			// Get rid of the msb of bitbuffer;
			this.bitBuffer <<= 1;
			this.bitBuffer &= 0xFFFFFFFFL; // long are on 64 bits, we want UInt32
			this.nBits--;
		}
	}

}
