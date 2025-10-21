package de.jreality.scene;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class Appearance
{
	public static Appearance NORMAL = new Appearance( "Normal" ); //$NON-NLS-1$

	private String name = ""; //$NON-NLS-1$

	private Map<String, Object> keyValuePairs = new HashMap<>();

	public Appearance( String name )
	{
		this.name = name;
	}

	public Appearance()
	{
	}

	public void setAttribute( String vertexDraw, boolean state )
	{
		this.keyValuePairs.put( vertexDraw, state );
	}
	public void setAttribute( String vertexDraw, Color color )
	{
		this.keyValuePairs.put( vertexDraw, color );
	}

	public void setAttribute( String vertexDraw, double d )
	{
		this.keyValuePairs.put( vertexDraw, d );
	}

}
