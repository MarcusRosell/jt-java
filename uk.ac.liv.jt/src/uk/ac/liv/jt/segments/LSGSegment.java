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
package uk.ac.liv.jt.segments;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.liv.jt.format.JTElement;
import uk.ac.liv.jt.format.elements.BaseAttributeElement;
import uk.ac.liv.jt.format.elements.BaseNodeElement;
import uk.ac.liv.jt.format.elements.BasePropertyAtomData;
import uk.ac.liv.jt.format.elements.GroupNodeElement;
import uk.ac.liv.jt.format.elements.InstanceNodeElement;
import uk.ac.liv.jt.format.elements.PartitionNodeElement;
import uk.ac.liv.jt.internal.BundleAccessor;
import uk.ac.liv.jt.types.GUID;

/**
 * The Logical Scene Graph segment contains the main structural information for a JT file.
 * The Scene graph contain references to the metadata, attributes (material, transformation) 
 * and properties, plus all the references to the actual geometric data.
 * 
 * Use the standard {@link #read()} method to generate the internal representation of the graph.
 * Use the {@link #generateSceneGraph()} method to generate the JReality scene graph and load 
 * all the geometry from the JT file.  
 * 
 * @author fabio
 *
 */
public class LSGSegment extends JTSegment
{

	/** the Graph structure holding the LSG representation */
	Map<BaseNodeElement, List<BaseNodeElement>> graph;

	/** the root of the JT Graph representation used for visiting the graph */
	GroupNodeElement start;

	public GroupNodeElement getStartElement()
	{
		return this.start;
	}

	/** specifies if rendering the scene graph is requested */
	public static boolean doRender = false;

	/** 
	 * Method to get the internal graph representation.
	 * This method does not read all the late loaded segments.
	 * @return the internal graph representation.
	 * @throws IOException
	 */
	public Map<BaseNodeElement, List<BaseNodeElement>> getGraph() throws IOException
	{
		if ( this.graph == null ) {
			read();
		}
		return this.graph;
	}

	@Override
	/** reads the segment LSG and generates the internal representation */
	public void read() throws IOException
	{

		super.read();

		this.graph = new HashMap<>();
		Map<Integer, BaseNodeElement> nodes = new HashMap<>();
		Map<Integer, BaseAttributeElement> attributes = new HashMap<>();
		Map<Integer, BasePropertyAtomData> properties = new HashMap<>();

		// read all the nodes and attributes
		while ( true ) {

			JTElement element2 = JTElement.createJTElement( this.reader );

			// *** SEE FIGURE 9: the ZLIB header is applied ONLY to the first
			// element in the Segment.
			element2.read();
			// End of elements
			if ( element2.id.equals( GUID.END_OF_ELEMENTS ) ) {
				break;
			}
			if ( element2 instanceof PartitionNodeElement pne && this.start == null ) {
				this.start = pne;
			}
			// we have an actual node
			if ( element2 instanceof BaseNodeElement node ) {
				nodes.put( node.getObjectID(), node );
				// we have an attribute
			}
			else if ( element2 instanceof BaseAttributeElement attr ) {
				attributes.put( attr.getObjectID(), attr );
			}
			else {
				BundleAccessor.getLogger().error( "Should not happen : not node nor attribute" ); //$NON-NLS-1$
			}
		}

		//read all the Property Atom Elements

		while ( true ) {

			JTElement element2 = JTElement.createJTElement( this.reader );

			// *** SEE FIGURE 9: the ZLIB header is applied ONLY to the first
			// element in the Segment.

			element2.read();

			if ( element2.id.equals( GUID.END_OF_ELEMENTS ) ) {
				break;
			}
			if ( element2 instanceof BasePropertyAtomData node ) {
				properties.put( node.getObjectID(), node );
			}
			else {
				BundleAccessor.getLogger().error( "Non property element found in the Property atoms" ); //$NON-NLS-1$
			}
		}

		// for each Node add attributes, children and instances
		int edgeN = 0;
		for ( BaseNodeElement b : nodes.values() ) {
			// add all the attributes
			for ( int i = 0; i < b.attObjectId.length; i++ ) {
				BaseAttributeElement baseAttributeElement = attributes.get( b.attObjectId[i] );
				b.attributes[i] = baseAttributeElement;
				if ( baseAttributeElement == null ) {
					BundleAccessor.getLogger().error( "null attribute!" ); //$NON-NLS-1$
				}
			}
			// add the vertex to the graph
			List<BaseNodeElement> l = new LinkedList<>();
			// graph.addVertex(b);
			// if it has child, add them to the graph
			if ( b instanceof GroupNodeElement g ) {
				for ( int child : g.childNodeObjectId ) {
					BaseNodeElement to = nodes.get( child );
					if ( to == null ) {
						BundleAccessor.getLogger().error( "child is a null node!" ); //$NON-NLS-1$
					}
					else {
						l.add( to );
					}
				}
			}
			// if it is an instance node, add the referenced node to the instanced node
			if ( b instanceof InstanceNodeElement ine ) {
				BaseNodeElement to = nodes.get( ine.instancedNodeObjectId );
				if ( to == null ) {
					BundleAccessor.getLogger().error( "child is a null node!" ); //$NON-NLS-1$
				}
				else {
					l.add( to );
				}
			}
			this.graph.put( b, l );
		}

		// Finally read the Property table, adding the properties to the nodes

		short versionNumber = this.reader.readI16();
		BasePropertyAtomData[][] dummy = new BasePropertyAtomData[0][0];
		int count = this.reader.readI32();
		for ( int i = 0; i < count; i++ ) {
			int refId = this.reader.readI32();
			int key;
			int value;

			List<BasePropertyAtomData[]> pro = new LinkedList<>();
			BundleAccessor.getLogger().trace( "Node object id: {} , {} of {}", refId, i + 1, count ); //$NON-NLS-1$
			while ( (key = this.reader.readI32()) != 0 ) {
				value = this.reader.readI32();
				BundleAccessor.getLogger().trace( "{} = {};", properties.get( key ), properties.get( value ) ); //$NON-NLS-1$
				BasePropertyAtomData basePropertyAtomData = properties.get( key );
				BasePropertyAtomData basePropertyAtomData2 = properties.get( value );
				pro.add( new BasePropertyAtomData[] { basePropertyAtomData, basePropertyAtomData2 } );
			}
			nodes.get( refId ).properties = pro.toArray( dummy );
		}

		// Graph rendering: represents the graph to screen
		if ( doRender ) {
			renderGraphRepresentation();
		}

	}

	/** returns the JTSegment for  GUID */
	public JTSegment getSegment( GUID segmentid )
	{
		return this.file.getSegment( segmentid );
	}

	/** Method to render the graph to a graph diagram, for debugging*/

	public void renderGraphRepresentation()
	{
//
//        
//        Graph<BaseNodeElement, Integer> gg = new DirectedSparseGraph<BaseNodeElement, Integer>();
//        
//        final VisualizationViewer<BaseNodeElement, Integer> vv = new VisualizationViewer<BaseNodeElement, Integer>(
//                new FRLayout<BaseNodeElement, Integer>( graph, new Dimension(1600, 1000)));
//        final JFrame frame = new JFrame();
//        Container content = frame.getContentPane();
//        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
//        content.add(panel);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//        final ModalGraphMouse gm = new DefaultModalGraphMouse<Integer, Number>();
//        vv.setGraphMouse(gm);
//        vv.addGraphMouseListener(new GraphMouseListener<BaseNodeElement>() {
//
//            public void graphReleased(BaseNodeElement arg0, MouseEvent arg1) {
//                // TODO Auto-generated method stub
//
//            }
//
//            public void graphPressed(BaseNodeElement arg0, MouseEvent arg1) {
//                // TODO Auto-generated method stub
//
//            }
//
//            public void graphClicked(BaseNodeElement arg0, MouseEvent arg1) {
//                for (BaseAttributeElement a : arg0.attributes)
//                    System.out.print(" " + a);
//
//                String s = "";
//                if (arg0.properties != null)
//                    for (BasePropertyAtomData[] a : arg0.properties)
//                        s += "\n" + a[0] + " = " + a[1];
//
//                System.out.println(s);
//
//            }
//        });
//        final ScalingControl scaler = new CrossoverScalingControl();
//
//        JButton plus = new JButton("+");
//        plus.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                scaler.scale(vv, 1.1f, vv.getCenter());
//            }
//        });
//        JButton minus = new JButton("-");
//        minus.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                scaler.scale(vv, 1 / 1.1f, vv.getCenter());
//            }
//        });
//
//        JCheckBox lo = new JCheckBox("Show Labels");
//        lo.addItemListener(new ItemListener() {
//            public void itemStateChanged(ItemEvent e) {
//                vv.repaint();
//            }
//        });
//        lo.setSelected(true);
//        vv.getRenderContext().setVertexLabelTransformer(
//                new Transformer<BaseNodeElement, String>() {
//
//                    public String transform(BaseNodeElement arg0) {
//                        return arg0.toString();
//                    }
//                });
//        vv.setVertexToolTipTransformer(new Transformer<BaseNodeElement, String>() {
//
//            public String transform(BaseNodeElement arg0) {
//                String s = "<html> <body>";
//                if (arg0.properties != null)
//                    for (BasePropertyAtomData[] a : arg0.properties)
//                        s += "<br>\n" + a[0] + " = " + a[1];
//                if (arg0.attributes != null)
//                    for (BaseAttributeElement a : arg0.attributes)
//                        s += "<br>\n" + a;
//
//                return s;
//            }
//        });
//        JPanel controls = new JPanel();
//        controls.add(plus);
//        controls.add(minus);
//        controls.add(lo);
//        controls.add(((DefaultModalGraphMouse<Integer, Number>) gm)
//                .getModeComboBox());
//        content.add(controls, BorderLayout.SOUTH);
//
//        frame.pack();
//        frame.setVisible(true);
	}

}
