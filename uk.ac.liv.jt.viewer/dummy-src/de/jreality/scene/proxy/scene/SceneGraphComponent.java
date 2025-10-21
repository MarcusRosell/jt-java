package de.jreality.scene.proxy.scene;

import java.util.ArrayList;
import java.util.List;

import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;

public class SceneGraphComponent
{
	private IndexedFaceSet indexedFaceSet = null;
	private Appearance appearance = Appearance.NORMAL;

	private List<de.jreality.scene.SceneGraphComponent> children = new ArrayList<>();

	public void setGeometry( IndexedFaceSet indexedFaceSet )
	{
		this.indexedFaceSet = indexedFaceSet;
	}

	public void setAppearance( Appearance appearance )
	{
		this.appearance = appearance;

	}

	public void addChild( de.jreality.scene.SceneGraphComponent sgc )
	{
		this.children.add( sgc );
	}
}
