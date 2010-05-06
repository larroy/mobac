/**
 * Package level definition of adapters for JAXB 
 */
@XmlJavaTypeAdapters( {
		@XmlJavaTypeAdapter(value = PointAdapter.class, type = java.awt.Point.class),
		@XmlJavaTypeAdapter(value = DimensionAdapter.class, type = java.awt.Dimension.class),
		@XmlJavaTypeAdapter(value = PolygonAdapter.class, type = java.awt.Polygon.class) })
package mobac.program.model;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import mobac.program.jaxb.DimensionAdapter;
import mobac.program.jaxb.PointAdapter;
import mobac.program.jaxb.PolygonAdapter;

