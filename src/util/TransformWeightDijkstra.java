package util;

import entities.Edge;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author rodrigo
 */
public class TransformWeightDijkstra implements Transformer<Edge, Integer>{
    
    public Integer transform(Edge e) {
        return e.getWeight();
    }
    
}
