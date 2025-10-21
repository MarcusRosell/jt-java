package uk.ac.liv.jt.viewer;

import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.PointSetUtility;
import de.jreality.math.Matrix;
import de.jreality.math.Pn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import uk.ac.liv.jt.format.JTFile;
import uk.ac.liv.jt.format.elements.BaseAttributeElement;
import uk.ac.liv.jt.format.elements.BaseNodeElement;
import uk.ac.liv.jt.format.elements.GeometricTransformAttributeElement;
import uk.ac.liv.jt.format.elements.GroupNodeElement;
import uk.ac.liv.jt.format.elements.MaterialAttributeElement;
import uk.ac.liv.jt.format.elements.PartitionNodeElement;
import uk.ac.liv.jt.format.elements.TriStripSetShapeLODElement;
import uk.ac.liv.jt.format.elements.TriStripSetShapeNodeElement;
import uk.ac.liv.jt.segments.LODSegment;
import uk.ac.liv.jt.segments.LSGSegment;
import uk.ac.liv.jt.types.GUID;

public class Converter
{
	public static SceneGraphComponent generateSceneGraph( LSGSegment segment ) throws IOException
	{
		if ( segment.getGraph() == null ) {
			segment.read();
		}

		return createSceneGraph( segment );
	}

	/** creates the JReality scene graph if not previously generated */
	private static SceneGraphComponent createSceneGraph( LSGSegment segment ) throws IOException
	{
		SceneGraphComponent theWorld = new JTSceneGraphComponent( "JT File" );

		HashMap<TriStripSetShapeNodeElement, SceneGraphComponent> geomCache = new HashMap<TriStripSetShapeNodeElement, SceneGraphComponent>( 10 );
		Appearance app = new Appearance();
		app.setAttribute( CommonAttributes.VERTEX_DRAW, false );
		app.setAttribute( CommonAttributes.EDGE_DRAW, false );
		theWorld.setAppearance( app );
		
		GroupNodeElement start = segment.getStartElement();
		addChilds( segment, start, theWorld, geomCache );

		geomCache.clear();

		return theWorld;
	}

	/** recursively visits the internal graph representation to create the JReality graph 
	 *  the late loaded Geometry data is actually loaded here 
	 * 
	 * */
	private static void addChilds( LSGSegment rootSegment, BaseNodeElement current, SceneGraphComponent sg, HashMap<TriStripSetShapeNodeElement, SceneGraphComponent> geomCache ) throws IOException
	{
//        if (current.ignore()){
//            System.out.println("Ignore node");
//            return;
//        }
		JTSceneGraphComponent s = new JTSceneGraphComponent( current.toString() );
		sg.addChild( s );

		if ( current.properties != null )
			s.properties = current.properties;

		// the only case currently supported for geometry data:
		if ( current instanceof TriStripSetShapeNodeElement ) {
			TriStripSetShapeNodeElement t = (TriStripSetShapeNodeElement)current;
			try {
				// if the data is already used, reuse it 
				if ( geomCache.containsKey( t ) ) {
					s.addChild( geomCache.get( t ) );
				}
				// otherwise read the geometry data from the file and add it 
				else {
					s.addChildren( getGemoetryComponents( t, rootSegment ) );
					geomCache.put( t, s );
				}

			}
			catch ( Exception e ) {
				e.printStackTrace();
			}
		}
		if ( current instanceof PartitionNodeElement ) {
			PartitionNodeElement pne = (PartitionNodeElement)current;
			if ( !pne.equals( rootSegment.getStartElement() ) ) {
				s.addChild( getGeometry( pne, rootSegment ) );
			}

		}

		// in case of attributes
		for ( BaseAttributeElement bae : current.attributes ) {
			if ( bae instanceof GeometricTransformAttributeElement ) {
				GeometricTransformAttributeElement gt = (GeometricTransformAttributeElement)bae;
				s.setTransformation( getTransformation( gt ) );
			}
			if ( bae instanceof MaterialAttributeElement ) {
				MaterialAttributeElement mae = (MaterialAttributeElement)bae;
				s.setAppearance( getAppereance( mae ) );
			}
		}
		// add all the children, navigating the structure
		Collection<BaseNodeElement> c = rootSegment.getGraph().get( current );
		for ( BaseNodeElement b : c ) {
			addChilds( rootSegment, b, s, geomCache );
		}
	}

	// From TriStripSetShapeNodeElement

	/**
	* Given the LSG segment, retrieves and parses the geometry for the current node.
	* The geometry returned is as a tree of @{link {@link SceneGraphComponent} for the JReality system.
	* The geometry is stored in a separate segment identified in this node's attributes.
	* 
	* @param lsgSegment The Logical Scene Graph for this model.
	* @return 
	* @throws IOException
	*/

	private static SceneGraphComponent[] getGemoetryComponents( TriStripSetShapeNodeElement self, LSGSegment lsgSegment ) throws IOException
	{
		GUID segmentid = self.getLODSegmentId();
		//System.out.println(segmentid);
		LODSegment lod;
		TriStripSetShapeLODElement l;
		Object o = lsgSegment.getSegment( segmentid );
		if ( o instanceof LODSegment ) {
			lod = (LODSegment)o;
			lod.read();
			l = (TriStripSetShapeLODElement)lod.e;
		}
		else {
			System.err.println( "Unsupported segment type: " + o.getClass() );
			return null;
		}

		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setGenerateEdgesFromFaces( false );

		ifsf.setGenerateFaceNormals( TriStripSetShapeNodeElement.OGL_FIX );
		ifsf.setGenerateAABBTree( false );
		ifsf.setGenerateEdgeLabels( false );
		ifsf.setGenerateVertexLabels( false );
		ifsf.setGenerateVertexNormals( TriStripSetShapeNodeElement.OGL_FIX );
		int nbdVertices = 0;
		int nbdfaces = 0;

		for ( int i = 0; i < l.primitiveListIndices.length - 1; i++ ) {
			int start = l.primitiveListIndices[i];
			int end = l.primitiveListIndices[i + 1];
			nbdVertices += end - start;
			nbdfaces += end - start - 2;
		}

		double[] vertices = new double[nbdVertices * 3];
		double[] normals = new double[nbdVertices * 3];

		int[] faceIndices = new int[nbdfaces * 3];
		SceneGraphComponent[] ret = new SceneGraphComponent[1];//[l.primitiveListIndices.length - 1];

		if ( l.uncompressed ) {
			vertices = l.vertex;
			normals = l.normal;
			int k = 0;
			for ( int i = 0; i < l.primitiveListIndices.length - 1; i++ ) {
				int start = l.primitiveListIndices[i];
				int end = l.primitiveListIndices[i + 1];
				for ( int f = start; f < end - 2; f++ ) {
					if ( f % 2 == 0 ) {
						faceIndices[k + 0] = f;
						faceIndices[k + 1] = f + 1;
						faceIndices[k + 2] = f + 2;
					}
					else {
						faceIndices[k + 0] = f;
						faceIndices[k + 2] = f + 1;
						faceIndices[k + 1] = f + 2;
					}
					k += 3;
				}
			}
		}
		else {
			int k = 0;
			for ( int i = 0; i < l.primitiveListIndices.length - 1; i++ ) {
				int start = l.primitiveListIndices[i];
				int end = l.primitiveListIndices[i + 1];

				for ( int v = start; v < end; v++ ) {

					int j = v * 3;
					int idx_raw = l.vertexDataIndices[v];
					vertices[j + 0] = l.quantVertexCoord.getXValues()[idx_raw];
					vertices[j + 1] = l.quantVertexCoord.getYValues()[idx_raw];
					vertices[j + 2] = l.quantVertexCoord.getZValues()[idx_raw];

					normals[j + 0] = l.normals[idx_raw].getX();
					normals[j + 1] = l.normals[idx_raw].getY();
					normals[j + 2] = l.normals[idx_raw].getZ();
				}
				for ( int f = start; f < end - 2; f++ ) {
					if ( f % 2 == 0 ) {
						faceIndices[k + 0] = f;
						faceIndices[k + 1] = f + 1;
						faceIndices[k + 2] = f + 2;
					}
					else {
						faceIndices[k + 0] = f;
						faceIndices[k + 2] = f + 1;
						faceIndices[k + 1] = f + 2;
					}
					k += 3;
				}
			}
		}
		l = null;
		lod.e = null;

		ifsf.setFaceCount( faceIndices.length / 3 );
		ifsf.setFaceIndices( faceIndices );
		ifsf.setVertexCount( vertices.length / 3 );
		ifsf.setVertexCoordinates( vertices );
		//]
		if ( !TriStripSetShapeNodeElement.OGL_FIX )
			ifsf.setVertexNormals( normals );

		ifsf.update();

		SceneGraphComponent sgc = new SceneGraphComponent( "GEOMETRY" );
		sgc.setGeometry( ifsf.getIndexedFaceSet() );

		ifsf = null;
		ret[0] = sgc;
		return ret;
	}

	// From PartitionNodeElement

	public static SceneGraphComponent getGeometry( PartitionNodeElement self, LSGSegment lsgSegment ) throws IOException
	{
		System.out.println( "Loading " + self.filename );

		URI u = lsgSegment.file.uri.resolve( self.filename );

		if ( !new File( u ).exists() ) {
			SceneGraphComponent c = generateBBOX( self, "Missing  JT file " + self.filename + "- BBox only" );
			return c;

		}
		if ( self.vertexCountRange.max < PartitionNodeElement.MAX_VERTICES ) {
			try {
				JTFile jtFile = new JTFile( new File( u ), u );
				LSGSegment lsg = jtFile.read();
				// read file header and TOC, and returns the pointer to the LSG segment

				// root is the root element declared in the reader interface
				return Converter.generateSceneGraph( lsg );
			}
			catch ( FileNotFoundException e ) {
				e.printStackTrace();
				SceneGraphComponent c = generateBBOX( self, "External JT file " + self.filename + " not found!" );
				return c;
			}
		}
		else {
			SceneGraphComponent c = generateBBOX( self, "External JT file with " + self.vertexCountRange.max + " vertices - BBox only" );

			return c;

		}
	}

	private static SceneGraphComponent generateBBOX( PartitionNodeElement self, String s )
	{
		Appearance app = new Appearance( "BBox" );
		app.setAttribute( CommonAttributes.TRANSPARENCY_ENABLED, true );
		app.setAttribute( POLYGON_SHADER + "." + TRANSPARENCY, .8 );
		app.setAttribute( CommonAttributes.AMBIENT_COLOR, new java.awt.Color( (float)Math.random(), (float)Math.random(), (float)Math.random() ) );
		Rectangle3D r = BoundingBoxUtility.calculateBoundingBox( new double[][] { self.untransformedBox.maxCorner.getVectorDouble(), self.untransformedBox.minCorner.getVectorDouble() } );
		IndexedFaceSet cubo = IndexedFaceSetUtility.representAsSceneGraph( r );
		SceneGraphComponent c = SceneGraphUtility.createFullSceneGraphComponent( s );
		c.setAppearance( app );
		c.setGeometry( cubo );
		return c;
	}

	// From GeometricTransformAttributeElement

	/** 
	 * 
	 * @return the JReality Transformation object 
	 */
	public static Transformation getTransformation( GeometricTransformAttributeElement self )
	{
		// The original code looks very strange
//		Matrix m = new Matrix();
//		m.transpose();
//		return new Transformation( m.getArray() );

		// I think the it was supposed to do something like:
		Matrix m = new Matrix( self.getTransformationMatrix() );
		m.transpose();
		return new Transformation( m.getArray() );
	}

	/**
	 * Returns a JReality Appearance containing the specific material attributes.
	 * @return Appearance
	 */
	public static Appearance getAppereance( MaterialAttributeElement self )
	{
		Appearance app = new Appearance();
		app.setAttribute( CommonAttributes.VERTEX_DRAW, false );
		app.setAttribute( CommonAttributes.EDGE_DRAW, false );
		app.setAttribute( CommonAttributes.AMBIENT_COLOR, self.ambient.getColor() );
		app.setAttribute( CommonAttributes.SPECULAR_COLOR, self.specular.getColor() );
		app.setAttribute( CommonAttributes.DIFFUSE_COLOR, self.diffuse.getColor() );
		// app.setAttribute(CommonAttributes., ambient.getColor());
		return app;

	}

	/**
	 * displays a single LOD element; used for debugging purposes
	 */
	public static void display( TriStripSetShapeLODElement self )
	{
		de.jreality.scene.proxy.scene.SceneGraphComponent scene = new de.jreality.scene.proxy.scene.SceneGraphComponent();

		for ( int i = 0; i < self.primitiveListIndices.length - 1; i++ ) {
			int start = self.primitiveListIndices[i];
			int end = self.primitiveListIndices[i + 1];

			int nbVertices = end - start;

			double[][] vertices = new double[nbVertices][3];
			double[][] normals = new double[nbVertices][3];

			for ( int v = start; v < end; v++ ) {
				int idx_raw = self.vertexDataIndices[v];
				vertices[v - start][0] = self.quantVertexCoord.getXValues()[idx_raw];
				vertices[v - start][1] = self.quantVertexCoord.getYValues()[idx_raw];
				vertices[v - start][2] = self.quantVertexCoord.getZValues()[idx_raw];

				normals[v - start][0] = self.normals[idx_raw].getX();
				normals[v - start][1] = self.normals[idx_raw].getY();
				normals[v - start][2] = self.normals[idx_raw].getZ();
			}

			IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();

			int[][] faceIndices = new int[vertices.length - 2][3];

			for ( int f = 0; f < faceIndices.length; f++ )
				if ( f % 2 == 0 ) {
					faceIndices[f][0] = f;
					faceIndices[f][1] = f + 1;
					faceIndices[f][2] = f + 2;
				}
				else {
					faceIndices[f][0] = f;
					faceIndices[f][2] = f + 1;
					faceIndices[f][1] = f + 2;
				}

			ifsf.setVertexCount( vertices.length );
			ifsf.setVertexCoordinates( vertices );
			ifsf.setFaceCount( faceIndices.length );
			ifsf.setFaceIndices( faceIndices );

			ifsf.setGenerateEdgesFromFaces( true );

			ifsf.setVertexNormals( normals );
			// In case you don't have normals
			//ifsf.setGenerateFaceNormals(true);

			ifsf.update();

			SceneGraphComponent sgc = new SceneGraphComponent();
			sgc.setGeometry( ifsf.getIndexedFaceSet() );

			Appearance app = new Appearance();
			app.setAttribute( CommonAttributes.VERTEX_DRAW, false );
			app.setAttribute( CommonAttributes.EDGE_DRAW, false );
			sgc.setAppearance( app );

			scene.addChild( sgc );
			// Display Vertices Normals
			if ( TriStripSetShapeLODElement.testDisplayNormals )
				scene.addChild( PointSetUtility.displayVertexNormals( ifsf.getPointSet(), .1, Pn.EUCLIDEAN ) );
			// Display Faces Normals (in case they are generated by jReality)
			// scene.addChild(IndexedFaceSetUtility.displayFaceNormals(ifsf.getIndexedFaceSet(),
			// .1, Pn.EUCLIDEAN));

		}

		Viewer v = JRViewer.display( scene );
	}

	public static void displayVertices( TriStripSetShapeLODElement self )
	{
		PointSetFactory psf = new PointSetFactory();

		double[][] vertices = new double[self.quantVertexCoord.getXValues().length][3];

		for ( int v = 0; v < self.quantVertexCoord.getXValues().length; v++ ) {
			vertices[v][0] = self.quantVertexCoord.getXValues()[v];
			vertices[v][1] = self.quantVertexCoord.getYValues()[v];
			vertices[v][2] = self.quantVertexCoord.getZValues()[v];
		}
		psf.setVertexCount( vertices.length );
		psf.setVertexCoordinates( vertices );
		psf.update();

		Viewer v = JRViewer.display( psf.getPointSet() );
	}

}
