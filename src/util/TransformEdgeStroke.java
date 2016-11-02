package util;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.apache.commons.collections15.Transformer;

import config.Configurations;
import entities.Edge;

public class TransformEdgeStroke implements Transformer<Edge, Stroke> {

	@Override
	public Stroke transform(Edge arg0) {
		return new BasicStroke(Configurations.EDGES_THICKNESS);
	}

}
