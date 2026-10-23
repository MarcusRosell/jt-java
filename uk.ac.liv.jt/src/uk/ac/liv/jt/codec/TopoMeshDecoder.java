/*******************************************************************************
 * This Library is :
 * 
 *     Copyright © 2026 Marcus Rosell - all rights reserved
 *     marcus.rosell@solme.se
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

import uk.ac.liv.jt.codec.DualVFMesh.IntList;

/**
 * Topology decoder of the JT 9 and later Topologically Compressed Rep Data: rebuilds the
 * {@link DualVFMesh} from the decoded symbol arrays, following the Polygon Mesh Topology Coder of
 * the JT specification (Annex D).
 */
public class TopoMeshDecoder
{
	// input symbols
	private final int[][] degreeSymbols; // face degrees, for 8 contexts
	private final int[] valenceSymbols;
	private final int[] groupSymbols;
	private final int[] flagSymbols;
	private final long[][] attrMaskSymbols; // <= 64 bit attribute masks, for 8 contexts
	private final int[] attrMaskLargeSymbols; // > 64 bit attribute masks
	private final int[] splitFaceSymbols;
	private final int[] splitPosSymbols;

	// read positions in the input symbols
	private final int[] degreeReadPos = new int[8];
	private int valenceReadPos;
	private int groupReadPos;
	private int flagReadPos;
	private final int[] attrMaskReadPos = new int[8];
	private int attrMaskLargeReadPos;
	private int splitFaceReadPos;
	private int splitPosReadPos;

	private final DualVFMesh mesh = new DualVFMesh();

	// stack of incomplete active faces
	private final IntList activeFaces = new IntList();
	private boolean[] removedActiveFaces = new boolean[64];
	// running counter assigning the face attribute (vertex record) numbers
	private int faceAttrCounter;

	public TopoMeshDecoder( int[][] degreeSymbols, int[] valenceSymbols, int[] groupSymbols, int[] flagSymbols, long[][] attrMaskSymbols,
			int[] attrMaskLargeSymbols, int[] splitFaceSymbols, int[] splitPosSymbols )
	{
		this.degreeSymbols = degreeSymbols;
		this.valenceSymbols = valenceSymbols;
		this.groupSymbols = groupSymbols;
		this.flagSymbols = flagSymbols;
		this.attrMaskSymbols = attrMaskSymbols;
		this.attrMaskLargeSymbols = attrMaskLargeSymbols;
		this.splitFaceSymbols = splitFaceSymbols;
		this.splitPosSymbols = splitPosSymbols;
	}

	/**
	 * Decodes the mesh.
	 *
	 * @return the decoded dual mesh
	 * @throws IOException if the symbols are corrupt
	 */
	public DualVFMesh decode() throws IOException
	{
		// decode the connected mesh components one at a time
		while ( runComponent() ) {
			// next component
		}
		if ( this.valenceReadPos != this.valenceSymbols.length || this.splitFaceReadPos != this.splitFaceSymbols.length ) {
			throw new IOException( "Mesh topology symbols not all consumed" ); //$NON-NLS-1$
		}
		return this.mesh;
	}

	/** number of face attributes (i.e. vertex attribute records) referenced by the mesh */
	public int numFaceAttrs()
	{
		return this.faceAttrCounter;
	}

	private boolean runComponent() throws IOException
	{
		int iVtx = ioVtx();
		if ( iVtx == -1 ) {
			// all the vertices are processed
			return false;
		}
		int valence = this.mesh.valence( iVtx );
		for ( int i = 0; i < valence; i++ ) {
			activateF( iVtx, i );
		}
		int iFace;
		while ( (iFace = nextActiveFace()) != -1 ) {
			completeF( iFace );
			this.removedActiveFaces[iFace] = true;
		}
		return true;
	}

	/** Completes the face by activating and completing each inactive vertex of its degree ring */
	private void completeF( int iFace ) throws IOException
	{
		int jVtxSlot;
		while ( (jVtxSlot = this.mesh.findVtxSlot( iFace, -1 )) != -1 ) {
			int iVtx = activateV( iFace, jVtxSlot );
			completeV( iVtx, jVtxSlot );
		}
	}

	/** Reads a face for the vertex at the given face slot, or connects to an existing face (split) */
	private int activateF( int iVtx, int iVSlot ) throws IOException
	{
		int iFace = ioFace( iVtx );
		if ( iFace >= 0 ) {
			// a new active face
			this.mesh.setVtxFace( iVtx, iVSlot, iFace );
			this.mesh.setFaceVtx( iFace, 0, iVtx );
			addActiveFace( iFace );
		}
		else {
			// the face already exists: split
			iFace = ioSplitFace();
			int jFSlot = ioSplitPos();
			if ( jFSlot < 0 || jFSlot >= this.mesh.degree( iFace ) ) {
				throw new IOException( "Corrupt mesh split position" ); //$NON-NLS-1$
			}
			this.mesh.setVtxFace( iVtx, iVSlot, iFace );
			addVtxToFace( iVtx, iVSlot, iFace, jFSlot );
		}
		return iFace;
	}

	/** Reads a vertex and attaches it to the face at the given vertex slot */
	private int activateV( int iFace, int iVSlot ) throws IOException
	{
		int iVtx = ioVtx();
		if ( iVtx == -1 ) {
			throw new IOException( "Missing mesh vertex valence symbols" ); //$NON-NLS-1$
		}
		this.mesh.setVtxFace( iVtx, 0, iFace );
		addVtxToFace( iVtx, 0, iFace, iVSlot );
		return iVtx;
	}

	/**
	 * Completes the vertex by connecting the faces that can be deduced from the already assembled
	 * topology, walking CCW then CW from face slot 0, and activating the remaining ones.
	 * iVSlot is the vertex slot of the vertex on its face 0.
	 */
	private void completeV( int iVtx, int iVSlot ) throws IOException
	{
		DualVFMesh m = this.mesh;
		int valence = m.valence( iVtx );

		int vp = m.face( iVtx, 0 );
		int jp = iVSlot;
		int i = 1;
		int vn;
		while ( i < valence && (vn = m.face( iVtx, i )) != -1 ) {
			jp = decMod( jp, m.degree( vp ) );
			int iVtx2 = m.vtx( vp, jp );
			if ( iVtx2 == -1 ) {
				break;
			}
			int jn = m.findVtxSlot( vn, iVtx2 );
			if ( jn < 0 ) {
				throw new IOException( "Inconsistent mesh topology" ); //$NON-NLS-1$
			}
			jn = decMod( jn, m.degree( vn ) );
			addVtxToFace( iVtx, i, vn, jn );
			vp = vn;
			jp = jn;
			i++;
			if ( i >= valence ) {
				return;
			}
		}

		int iLast = i;
		vp = m.face( iVtx, 0 );
		jp = iVSlot;
		i = valence - 1;
		while ( i >= iLast && (vn = m.face( iVtx, i )) != -1 ) {
			jp = incMod( jp, m.degree( vp ) );
			int iVtx2 = m.vtx( vp, jp );
			if ( iVtx2 == -1 ) {
				break;
			}
			int jn = m.findVtxSlot( vn, iVtx2 );
			if ( jn < 0 ) {
				throw new IOException( "Inconsistent mesh topology" ); //$NON-NLS-1$
			}
			jn = incMod( jn, m.degree( vn ) );
			addVtxToFace( iVtx, i, vn, jn );
			vp = vn;
			jp = jn;
			i--;
			if ( i < iLast ) {
				return;
			}
		}

		// activate the remaining faces, that cannot be deduced
		for ( ; iLast <= i; iLast++ ) {
			activateF( iVtx, iLast );
		}
	}

	/**
	 * Connects the vertex to the face at vertex slot iVSlot, and into the faces at the other ends
	 * of the edges shared with the CW and CCW vertices of the face.
	 */
	private void addVtxToFace( int iVtx, int jFSlot, int iFace, int iVSlot )
	{
		DualVFMesh m = this.mesh;
		int degree = m.degree( iFace );
		int iVSlotCCW = incMod( iVSlot, degree );
		int iVSlotCW = decMod( iVSlot, degree );

		m.setFaceVtx( iFace, iVSlot, iVtx );

		int fp = m.vtx( iFace, iVSlotCW );
		if ( fp != -1 ) {
			int ip = m.findFaceSlot( fp, iFace );
			int slot = incMod( jFSlot, m.valence( iVtx ) );
			if ( ip != -1 && m.face( iVtx, slot ) == -1 ) {
				ip = decMod( ip, m.valence( fp ) );
				m.setVtxFace( iVtx, slot, m.face( fp, ip ) );
			}
		}

		int fn = m.vtx( iFace, iVSlotCCW );
		if ( fn != -1 ) {
			int in = m.findFaceSlot( fn, iFace );
			int slot = decMod( jFSlot, m.valence( iVtx ) );
			if ( in != -1 && m.face( iVtx, slot ) == -1 ) {
				in = incMod( in, m.valence( fn ) );
				m.setVtxFace( iVtx, slot, m.face( fn, in ) );
			}
		}
	}

	private void addActiveFace( int iFace )
	{
		this.activeFaces.add( iFace );
		if ( iFace >= this.removedActiveFaces.length ) {
			this.removedActiveFaces = java.util.Arrays.copyOf( this.removedActiveFaces, Math.max( iFace + 1, this.removedActiveFaces.length * 2 ) );
		}
	}

	/**
	 * Returns the active face to complete: among the 16 most recent ones, the first one with the
	 * smallest number of incomplete vertex slots.
	 */
	private int nextActiveFace()
	{
		IntList active = this.activeFaces;
		while ( active.size() > 0 && this.removedActiveFaces[active.get( active.size() - 1 )] ) {
			active.removeLast();
		}
		int iFace = -1;
		int lowestEmptyDegree = Integer.MAX_VALUE;
		for ( int i = active.size() - 1; i >= Math.max( 0, active.size() - 16 ); i-- ) {
			int iFace0 = active.get( i );
			if ( this.removedActiveFaces[iFace0] ) {
				active.remove( i );
				continue;
			}
			int emptyDegree = this.mesh.emptyFaceSlots( iFace0 );
			if ( emptyDegree < lowestEmptyDegree ) {
				lowestEmptyDegree = emptyDegree;
				iFace = iFace0;
			}
		}
		return iFace;
	}

	// ========== Decoder I/O ==========

	/** Reads a vertex valence, group and flags and creates the vertex; -1 at the end of the symbols */
	private int ioVtx()
	{
		if ( this.valenceReadPos >= this.valenceSymbols.length ) {
			return -1;
		}
		int valence = this.valenceSymbols[this.valenceReadPos++];
		int group = this.groupReadPos < this.groupSymbols.length ? this.groupSymbols[this.groupReadPos++] : -1;
		int flags = this.flagReadPos < this.flagSymbols.length ? this.flagSymbols[this.flagReadPos++] : 0;
		return this.mesh.newVtx( valence, group, flags );
	}

	/** Reads a face degree and attribute mask and creates the face; -1 for a split face */
	private int ioFace( int iVtx ) throws IOException
	{
		int context = faceContext( iVtx );
		if ( this.degreeReadPos[context] >= this.degreeSymbols[context].length ) {
			throw new IOException( "Missing mesh face degree symbols" ); //$NON-NLS-1$
		}
		int degree = this.degreeSymbols[context][this.degreeReadPos[context]++];
		if ( degree == 0 ) {
			return -1;
		}
		if ( degree < 0 ) {
			throw new IOException( "Corrupt mesh face degree" ); //$NON-NLS-1$
		}
		int attrCount;
		long attrMask = 0;
		long[] attrMaskLarge = null;
		if ( degree <= 64 ) {
			int maskContext = Math.min( 7, Math.max( 0, degree - 2 ) );
			if ( this.attrMaskReadPos[maskContext] < this.attrMaskSymbols[maskContext].length ) {
				attrMask = this.attrMaskSymbols[maskContext][this.attrMaskReadPos[maskContext]++];
			}
			attrCount = Long.bitCount( attrMask );
		}
		else {
			int nWords = (degree + 31) >> 5;
			attrMaskLarge = new long[(degree + 63) >> 6];
			attrCount = 0;
			if ( this.attrMaskLargeReadPos + nWords <= this.attrMaskLargeSymbols.length ) {
				for ( int w = 0; w < nWords; w++ ) {
					long word = this.attrMaskLargeSymbols[this.attrMaskLargeReadPos++] & 0xffffffffL;
					attrMaskLarge[w >> 1] |= word << ((w & 1) * 32);
					attrCount += Long.bitCount( word );
				}
			}
		}
		if ( attrCount > degree ) {
			throw new IOException( "Corrupt mesh face attribute mask" ); //$NON-NLS-1$
		}
		int iFace = this.mesh.newFace( degree, attrMask, attrMaskLarge, attrCount );
		for ( int iAttrSlot = 0; iAttrSlot < attrCount; iAttrSlot++ ) {
			this.mesh.setFaceAttr( iFace, iAttrSlot, this.faceAttrCounter++ );
		}
		return iFace;
	}

	/** Reads a split face offset and returns the referenced active face */
	private int ioSplitFace() throws IOException
	{
		if ( this.splitFaceReadPos >= this.splitFaceSymbols.length ) {
			throw new IOException( "Missing mesh split face symbols" ); //$NON-NLS-1$
		}
		int offset = this.splitFaceSymbols[this.splitFaceReadPos++];
		int length = this.activeFaces.size();
		if ( offset <= 0 || offset > length ) {
			throw new IOException( "Corrupt mesh split face offset" ); //$NON-NLS-1$
		}
		return this.activeFaces.get( length - offset );
	}

	/** Reads a split position: the vertex slot of the split face */
	private int ioSplitPos() throws IOException
	{
		if ( this.splitPosReadPos >= this.splitPosSymbols.length ) {
			throw new IOException( "Missing mesh split position symbols" ); //$NON-NLS-1$
		}
		return this.splitPosSymbols[this.splitPosReadPos++];
	}

	/**
	 * Compression context (0 to 7) for the faces of a vertex, from its valence and the total
	 * degree of its already known faces.
	 */
	private int faceContext( int iVtx )
	{
		DualVFMesh m = this.mesh;
		int valence = m.valence( iVtx );
		int nKnownFaces = 0;
		int knownTotalDegree = 0;
		for ( int i = 0; i < valence; i++ ) {
			int iFace = m.face( iVtx, i );
			if ( m.isValidFace( iFace ) ) {
				nKnownFaces++;
				knownTotalDegree += m.degree( iFace );
			}
		}
		if ( valence == 3 ) {
			// regular tristrip-like meshes tend to have degree 6 faces
			return knownTotalDegree < nKnownFaces * 6 ? 0 : knownTotalDegree == nKnownFaces * 6 ? 1 : 2;
		}
		if ( valence == 4 ) {
			// regular quadstrip-like meshes tend to have degree 4 faces
			return knownTotalDegree < nKnownFaces * 4 ? 3 : knownTotalDegree == nKnownFaces * 4 ? 4 : 5;
		}
		return valence == 5 ? 6 : 7;
	}

	private static int incMod( int i, int n )
	{
		return i + 1 == n ? 0 : i + 1;
	}

	private static int decMod( int i, int n )
	{
		return i == 0 ? n - 1 : i - 1;
	}
}
