package util;

import java.awt.Color;
import java.awt.Paint;

import org.apache.commons.collections15.Transformer;

import config.Configurations;
import entities.Edge;

public class TransformImageEdgeColor implements Transformer<Edge, Paint> {

	@Override
	public Paint transform(Edge e) {
		javafx.scene.paint.Color color = Configurations.EDGES_COLOR;
    	float RED = (float) color.getRed();
    	float GREEN = (float) color.getGreen();
    	float BLUE = (float) color.getBlue();

    	return new Color(RED, GREEN, BLUE);
	}

}
