package util;

import entities.Vertex;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author rodrigo
 */
public class TransformVertexLabel implements Transformer<Vertex, String> {
    
    public String transform(Vertex v) {
        return String.valueOf(v.getId());
    }    
}