package util;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.apache.commons.collections15.Transformer;

import entities.Vertex;

public class TransformVertexStroke implements Transformer<Vertex, Shape> {

	@Override
	public Shape transform(Vertex arg0) {
		//centerX - radius, centerY - radius, 2.0 * radius, 2.0 * radius
		int centerX = 0;
		int centerY = 0;
		int radius = 1;
		Ellipse2D circle = new Ellipse2D.Double(centerX - radius, centerY - radius, 2.0 * radius, 2.0 * radius);
        return circle;
	}

}
