package util;

import entities.Edge;
import java.awt.Color;
import java.awt.Paint;
import org.apache.commons.collections15.Transformer;

import config.Configurations;

/**
 *
 * @author rodrigo
 */
public class TransformEdgeColor implements Transformer<Edge, Paint> {
    
    public Paint transform(Edge e) {
    	javafx.scene.paint.Color color;
        if(e.isVisited()) {
        	color = Configurations.EDGES_ANIMATION_COLOR;
        } else {
        	color = Configurations.EDGES_COLOR;
        }
        float RED = (float) color.getRed();
    	float GREEN = (float) color.getGreen();
    	float BLUE = (float) color.getBlue();
    	return new Color(RED, GREEN, BLUE);
    }
}