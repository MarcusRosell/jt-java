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

import java.util.Arrays;

/**
 * Dual Vertex-Facet mesh built by the {@link TopoMeshDecoder} (JT 9 and later topologically
 * compressed meshes).
 * <p>
 * In the dual mesh, each face represents a vertex of the original triangle mesh (its degree is the
 * number of triangles around the vertex), and each vertex represents a triangle of the original
 * mesh (its valence is the number of triangle corners, normally 3).
 */
public class DualVFMesh
{
	// vertices
	private int nVts;
	private int[] vtxValence = new int[64];
	private int[] vtxFlags = new int[64];
	private int[] vtxGroup = new int[64];
	private int[] vtxFaceIdx = new int[64];
	private final IntList vtxFaces = new IntList();

	// faces
	private int nFaces;
	private int[] faceDegree = new int[64];
	private int[] faceEmptyDegree = new int[64];
	private int[] faceAttrCount = new int[64];
	private long[] faceAttrMask = new long[64];
	private long[][] faceAttrMaskLarge = new long[64][];
	private int[] faceVtxIdx = new int[64];
	private int[] faceAttrIdx = new int[64];
	private final IntList faceVts = new IntList();
	private final IntList faceAttrs = new IntList();

	public int numVts()
	{
		return this.nVts;
	}

	public int numFaces()
	{
		return this.nFaces;
	}

	/** creates a new vertex, its index is numVts() */
	int newVtx( int valence, int group, int flags )
	{
		int iVtx = this.nVts++;
		if ( iVtx == this.vtxValence.length ) {
			int n = iVtx * 2;
			this.vtxValence = Arrays.copyOf( this.vtxValence, n );
			this.vtxFlags = Arrays.copyOf( this.vtxFlags, n );
			this.vtxGroup = Arrays.copyOf( this.vtxGroup, n );
			this.vtxFaceIdx = Arrays.copyOf( this.vtxFaceIdx, n );
		}
		this.vtxValence[iVtx] = valence;
		this.vtxFlags[iVtx] = flags;
		this.vtxGroup[iVtx] = group;
		this.vtxFaceIdx[iVtx] = this.vtxFaces.size();
		this.vtxFaces.addFilled( valence, -1 );
		return iVtx;
	}

	/** creates a new face, its index is numFaces() */
	int newFace( int degree, long attrMask, long[] attrMaskLarge, int attrCount )
	{
		int iFace = this.nFaces++;
		if ( iFace == this.faceDegree.length ) {
			int n = iFace * 2;
			this.faceDegree = Arrays.copyOf( this.faceDegree, n );
			this.faceEmptyDegree = Arrays.copyOf( this.faceEmptyDegree, n );
			this.faceAttrCount = Arrays.copyOf( this.faceAttrCount, n );
			this.faceAttrMask = Arrays.copyOf( this.faceAttrMask, n );
			this.faceAttrMaskLarge = Arrays.copyOf( this.faceAttrMaskLarge, n );
			this.faceVtxIdx = Arrays.copyOf( this.faceVtxIdx, n );
			this.faceAttrIdx = Arrays.copyOf( this.faceAttrIdx, n );
		}
		this.faceDegree[iFace] = degree;
		this.faceEmptyDegree[iFace] = degree;
		this.faceAttrCount[iFace] = attrCount;
		this.faceAttrMask[iFace] = attrMask;
		this.faceAttrMaskLarge[iFace] = attrMaskLarge;
		this.faceVtxIdx[iFace] = this.faceVts.size();
		this.faceVts.addFilled( degree, -1 );
		this.faceAttrIdx[iFace] = this.faceAttrs.size();
		this.faceAttrs.addFilled( attrCount, -1 );
		return iFace;
	}

	public boolean isValidFace( int iFace )
	{
		return iFace >= 0 && iFace < this.nFaces && this.faceDegree[iFace] != 0;
	}

	public int valence( int iVtx )
	{
		return this.vtxValence[iVtx];
	}

	public int vtxFlags( int iVtx )
	{
		return this.vtxFlags[iVtx];
	}

	public int vtxGroup( int iVtx )
	{
		return this.vtxGroup[iVtx];
	}

	public int degree( int iFace )
	{
		return this.faceDegree[iFace];
	}

	public int emptyFaceSlots( int iFace )
	{
		return this.faceEmptyDegree[iFace];
	}

	public int numAttrs( int iFace )
	{
		return this.faceAttrCount[iFace];
	}

	/** returns true if the attribute mask bit of the given vertex slot is set */
	public boolean attrMaskBit( int iFace, int iVtxSlot )
	{
		long[] large = this.faceAttrMaskLarge[iFace];
		if ( large != null ) {
			return (large[iVtxSlot >> 6] >>> (iVtxSlot & 63) & 1) != 0;
		}
		return (this.faceAttrMask[iFace] >>> iVtxSlot & 1) != 0;
	}

	/** face incident to the vertex at the given face slot, -1 if not known yet */
	public int face( int iVtx, int iFaceSlot )
	{
		return this.vtxFaces.get( this.vtxFaceIdx[iVtx] + iFaceSlot );
	}

	/** vertex incident to the face at the given vertex slot, -1 if not known yet */
	public int vtx( int iFace, int iVtxSlot )
	{
		return this.faceVts.get( this.faceVtxIdx[iFace] + iVtxSlot );
	}

	public int faceAttr( int iFace, int iAttrSlot )
	{
		return this.faceAttrs.get( this.faceAttrIdx[iFace] + iAttrSlot );
	}

	void setFaceAttr( int iFace, int iAttrSlot, int iFaceAttr )
	{
		this.faceAttrs.set( this.faceAttrIdx[iFace] + iAttrSlot, iFaceAttr );
	}

	void setVtxFace( int iVtx, int iFaceSlot, int iFace )
	{
		this.vtxFaces.set( this.vtxFaceIdx[iVtx] + iFaceSlot, iFace );
	}

	void setFaceVtx( int iFace, int iVtxSlot, int iVtx )
	{
		int i = this.faceVtxIdx[iFace] + iVtxSlot;
		if ( this.faceVts.get( i ) != iVtx ) {
			this.faceEmptyDegree[iFace]--;
		}
		this.faceVts.set( i, iVtx );
	}

	/** returns the vertex slot of iTargVtx in the face, -1 if not found */
	public int findVtxSlot( int iFace, int iTargVtx )
	{
		int start = this.faceVtxIdx[iFace];
		for ( int i = 0; i < this.faceDegree[iFace]; i++ ) {
			if ( this.faceVts.get( start + i ) == iTargVtx ) {
				return i;
			}
		}
		return -1;
	}

	/** returns the face slot of iTargFace in the vertex, -1 if not found */
	public int findFaceSlot( int iVtx, int iTargFace )
	{
		int start = this.vtxFaceIdx[iVtx];
		for ( int i = 0; i < this.vtxValence[iVtx]; i++ ) {
			if ( this.vtxFaces.get( start + i ) == iTargFace ) {
				return i;
			}
		}
		return -1;
	}

	/** growable int array */
	static class IntList
	{
		private int[] data = new int[256];
		private int size;

		int size()
		{
			return this.size;
		}

		int get( int i )
		{
			return this.data[i];
		}

		void set( int i, int v )
		{
			this.data[i] = v;
		}

		void add( int v )
		{
			if ( this.size == this.data.length ) {
				this.data = Arrays.copyOf( this.data, this.size * 2 );
			}
			this.data[this.size++] = v;
		}

		void addFilled( int n, int v )
		{
			if ( this.size + n > this.data.length ) {
				this.data = Arrays.copyOf( this.data, Math.max( this.data.length * 2, this.size + n ) );
			}
			Arrays.fill( this.data, this.size, this.size + n, v );
			this.size += n;
		}

		void remove( int i )
		{
			System.arraycopy( this.data, i + 1, this.data, i, this.size - i - 1 );
			this.size--;
		}

		void removeLast()
		{
			this.size--;
		}
	}
}
