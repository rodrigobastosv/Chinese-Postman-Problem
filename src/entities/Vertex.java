package entities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Vertex implements Cloneable, Serializable {
    private Integer id;
    public Integer d; // For use in
    public Vertex prev;   // Bellman-Ford
    public boolean visited; // BFS

    public Vertex(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public boolean isVisited() {
        return visited;
    }

    public Integer getD() {
        return d;
    }

    public Vertex getPrev() {
        return prev;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setD(Integer d) {
        this.d = d;
    }

    public void setPrev(Vertex prev) {
        this.prev = prev;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
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

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}