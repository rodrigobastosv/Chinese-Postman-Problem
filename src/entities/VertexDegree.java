package entities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class VertexDegree implements Cloneable, Serializable {
    Vertex vertex;
    int inDegree;
    int outDegree;

    public VertexDegree(Vertex vertex, int inDegree, int outDegree) {
        this.vertex = vertex;
        this.inDegree = inDegree;
        this.outDegree = outDegree;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public int getInDegree() {
        return inDegree;
    }

    public int getOutDegree() {
        return outDegree;
    }

    public void setVertex(Vertex vertex) {
        this.vertex = vertex;
    }

    public void setInDegree(int inDegree) {
        this.inDegree = inDegree;
    }

    public void setOutDegree(int outDegree) {
        this.outDegree = outDegree;
    }            
    
    public Object clone() {
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bout);
            out.writeObject(this);
            out.close();
            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            in = new ObjectInputStream(bin);
            Object copy = in.readObject();
            in.close();
            return copy;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ignore) {
            }
        }
        return null;
    }    
}
