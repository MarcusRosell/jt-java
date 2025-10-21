package de.jreality.scene;

import java.util.ArrayList;
import java.util.List;

public class SceneGraphComponent
{
	private String name;
	private IndexedFaceSet indexedFaceSet;
	private Appearance appearance;
	private Transformation transformation;

	private List<SceneGraphComponent> children = new ArrayList<>();

	public SceneGraphComponent( String name )
	{
		this.name = name;
	}

	public SceneGraphComponent()
	{
		this.name = ""; //$NON-NLS-1$
	}

	public void setGeometry( IndexedFaceSet indexedFaceSet )
	{
		this.indexedFaceSet = indexedFaceSet;
	}

	public void setAppearance( Appearance appearance )
	{
		this.appearance = appearance;
	}

	public void addChild( SceneGraphComponent sgc )
	{
		this.children.add( sgc );
	}
	
	public void setTransformation( Transformation transformation )
	{
		this.transformation = transformation;
	}
}
