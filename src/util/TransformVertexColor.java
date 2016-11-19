package util;

import entities.Vertex;
import java.awt.Paint;
import org.apache.commons.collections15.Transformer;

import config.Configurations;

/**
 *
 * @author rodrigo
 */
public class TransformVertexColor implements Transformer<Vertex, Paint> {
    
    public Paint transform(Vertex v) {
    	return Util.getNormalizedColor(Configurations.VERTICES_COLOR);
    }    
}