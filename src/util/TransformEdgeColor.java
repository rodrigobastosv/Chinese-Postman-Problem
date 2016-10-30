package util;

import entities.Edge;
import java.awt.Color;
import java.awt.Paint;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author rodrigo
 */
public class TransformEdgeColor implements Transformer<Edge, Paint> {
    
    public Paint transform(Edge e) {
        if(e.isVisited()) {
            return Color.YELLOW;
        }
        return Color.RED;
    }
}