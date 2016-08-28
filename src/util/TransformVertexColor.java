package util;

import entities.Vertex;
import java.awt.Color;
import java.awt.Paint;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author rodrigo
 */
public class TransformVertexColor implements Transformer<Vertex, Paint> {
    
    public Paint transform(Vertex v) {
        if(v.isVisited()) {
            return Color.YELLOW;
        }
        return Color.RED;
    }    
}