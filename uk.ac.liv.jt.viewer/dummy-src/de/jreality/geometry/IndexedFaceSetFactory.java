package de.jreality.geometry;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;

public class IndexedFaceSetFactory
{
	private int faceCount;
	private int[] faceIndices;
	private int vertexCount;
	private double[] vertexCoordinates;
	private double[] vertexNormals;

	private IndexedFaceSet generatedFaceSet = null;

	public void setFaceCount( int faceCount )
	{
		this.faceCount = faceCount;
	}

	public void setFaceIndices( int[] faceIndices )
	{
		this.faceIndices = faceIndices;
	}

	public void setVertexCount( int vertexCount )
	{
		this.vertexCount = vertexCount;
	}

	public void setVertexCoordinates( double[] vertexCoordinates )
	{
		this.vertexCoordinates = vertexCoordinates;
	}

	public void setVertexNormals( double[] vertexNormals )
	{
		this.vertexNormals = vertexNormals;
	}

	public void update()
	{
		this.generatedFaceSet = new IndexedFaceSet();
	}

	public IndexedFaceSet getIndexedFaceSet()
	{
		return this.generatedFaceSet;
	}

	public void setGenerateEdgesFromFaces( boolean b )
	{
		// TODO Auto-generated method stub

	}

	public void setGenerateFaceNormals( boolean oGL_FIX )
	{
		// TODO Auto-generated method stub

	}

	public void setGenerateAABBTree( boolean b )
	{
		// TODO Auto-generated method stub

	}

	public void setGenerateEdgeLabels( boolean b )
	{
		// TODO Auto-generated method stub

	}

	public void setGenerateVertexLabels( boolean b )
	{
		// TODO Auto-generated method stub

	}

	public void setGenerateVertexNormals( boolean oGL_FIX )
	{
		// TODO Auto-generated method stub

	}

	public void setVertexCoordinates( double[][] vertices )
	{
		// TODO Auto-generated method stub

	}

	public void setFaceIndices( int[][] faceIndices )
	{
		// TODO Auto-generated method stub

	}

	public void setVertexNormals( double[][] normals )
	{
		// TODO Auto-generated method stub

	}

	public PointSet getPointSet()
	{
		// TODO Auto-generated method stub
		return new PointSet();
	}

}
