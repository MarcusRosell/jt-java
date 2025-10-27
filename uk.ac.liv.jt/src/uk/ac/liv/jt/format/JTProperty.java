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
package uk.ac.liv.jt.format;

public enum JTProperty
{
	unit( "JT_PROP_MEASUREMENT_UNITS" ), //$NON-NLS-1$
	name( "JT_PROP_NAME" ), //$NON-NLS-1$
	partition( "PartitionType" ), //$NON-NLS-1$
	tristrip_layout( "JT_PROP_TRISTRIP_DATA_LAYOUT" ), //$NON-NLS-1$
	simp_params( "JT_PROP_SIMP_PARAMS" ), //$NON-NLS-1$
	simp_name( "JT_PROP_SIMP_NAME" ), //$NON-NLS-1$
	shape_type( "JT_PROP_SHAPE_DATA_TYPE" ), //$NON-NLS-1$
	shape_lloaded_reference( "JT_LLPROP_SHAPEIMPL" ), //$NON-NLS-1$
	shape_lloaded_pmi( "JT_LLPROP_PMI" ), //$NON-NLS-1$
	shape_lloaded_metadata( "JT_LLPROP_METADATA" ), //$NON-NLS-1$
	BSphereCoverageFractionMax( "BSphereCoverageFractionMax" ), //$NON-NLS-1$
	EXTERNAL_REF( "__EXTERNAL_REF" ), //$NON-NLS-1$
	nTrisLODs( "_nTrisLODs" ), //$NON-NLS-1$
	Angular( "Angular" ), //$NON-NLS-1$
	Chordal( "Chordal" ), //$NON-NLS-1$

	SEOccurrenceDisplayAsReference( "SEOccurrenceDisplayAsReference::" ), //$NON-NLS-1$
	SEOccurrenceExcludeFromBOM( "SEOccurrenceExcludeFromBOM:" ), //$NON-NLS-1$
	SEOccurrenceExcludeFromInterference( "SEOccurrenceExcludeFromInterference::" ), //$NON-NLS-1$
	SEOccurrenceExcludeFromPhysicalProps( "SEOccurrenceExcludeFromPhysicalProps::" ), //$NON-NLS-1$
	SEOccurrenceHideInDrawing( "SEOccurrenceHideInDrawing:" ), //$NON-NLS-1$
	SEOccurrenceHideInSubAssembly( "SEOccurrenceHideInSubAssembly::" ), //$NON-NLS-1$
	SEOccurrenceID( "SEOccurrenceID::" ), //$NON-NLS-1$
	SEOccurrenceName( "SEOccurrenceName:" ), //$NON-NLS-1$
	SEOccurrenceQuantity( "SEOccurrenceQuantity::" ), //$NON-NLS-1$
	TOOLKIT_CUSTOMER( "TOOLKIT_CUSTOMER" ), //$NON-NLS-1$
	Translation_Date( "Translation Date::" ), //$NON-NLS-1$
	Translation_Version( "Translator Version::" ), //$NON-NLS-1$

	AdvCompressLevel( "AdvCompressLevel::" ), //$NON-NLS-1$
	AdvCompressLODLevel( "AdvCompressLODLevel::" ), //$NON-NLS-1$
	Angular2( "Angular::" ), //$NON-NLS-1$
	CAD_Source( "CAD Source::" ), //$NON-NLS-1$
	Chordal2( "Chordal::" ), //$NON-NLS-1$
	Name( "Name::" ); //$NON-NLS-1$

	private String key;

	private JTProperty( String key )
	{
		this.key = key;
	}
}
