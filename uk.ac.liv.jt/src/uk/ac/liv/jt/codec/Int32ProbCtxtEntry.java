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

public class Int32ProbCtxtEntry {
	
	/* An entry is a row of a table. p.229 */

    long occCount; // Number of occurrences
    long cumCount; // Cumulative number of occurrences

    long associatedValue;
    long symbol; // Symbol
    int nextContext; // Next context if this symbol seen

    public Int32ProbCtxtEntry(long symbol, long occCount, long cumCount,
            long associatedValue, int nextContext) {
        super();
        this.symbol = symbol;
        this.occCount = occCount;
        this.cumCount = cumCount;
        this.associatedValue = associatedValue;
        this.nextContext = nextContext;
    }

    public long getAssociatedValue() {
        return associatedValue;
    }

    public long getSymbol() {
        return symbol;
    }

    public long getCumCount() {
        return cumCount;
    }

    public long getOccCount() {
        return occCount;
    }

    public int getNextContext() {
        return nextContext;
    }

    @Override
    public String toString() {
        return String.format("%d - %d(%d) - => %d - %d", symbol, occCount,
                cumCount, associatedValue, nextContext);
    }

}
