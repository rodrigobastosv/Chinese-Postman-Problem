package entities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import java.util.Random;

public class Edge implements Cloneable, Serializable {    
    private Vertex node1, node2;
    private int weight;
    private boolean duplicated; //for DOT
    private boolean visited;
    private int hash;

    public Edge() {
        this.hash = hashCode() + new Random().nextInt();
    }
    
    public Edge(int weight) {
        this.weight = weight;
        this.duplicated = false;
        this.visited = false;
        this.hash = hashCode() + new Random().nextInt();
    }
    
    public Edge(Vertex node1, Vertex node2, int weight) {
        this.node1 = node1;
        this.node2 = node2;
        this.weight = weight;
        this.duplicated = false;
        this.hash = hashCode() + new Random().nextInt();
    }
    
    public Edge(Vertex node1, Vertex node2) {
        this.node1 = node1;
        this.node2 = node2;        
        this.duplicated = false;
        this.hash = hashCode() + new Random().nextInt();
    }
    
    public Edge(Vertex node1, Vertex node2, int weight, boolean duplicated) {
        this.node1 = node1;
        this.node2 = node2;
        this.weight = weight;
        this.duplicated = duplicated;
        this.hash = hashCode() + new Random().nextInt();
    }

    public Vertex getNode1() {
        return node1;
    }

    public int getHash() {
        return hash;
    }

    public boolean isVisited() {
        return visited;
    }

    public Vertex getNode2() {
        return node2;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isDuplicated() {
        return duplicated;
    }

    public void setNode1(Vertex node1) {
        this.node1 = node1;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

    public void setNode2(Vertex node2) {
        this.node2 = node2;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setDuplicated(boolean duplicated) {
        this.duplicated = duplicated;
    }    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.node1);
        hash = 67 * hash + Objects.hashCode(this.node2);
        hash = (int) (67 * hash + this.weight);
        hash = 67 * hash + (this.duplicated ? 1 : 0);        
        hash = 67 * hash + this.hash;
        return hash;
    }    

    @Override
    public String toString() {
        return node1.getId() + "-" + node2.getId() + ":" + weight;
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
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Edge other = (Edge) obj;
        if(other.duplicated != this.duplicated) {
            return false;
        }
        if (!Objects.equals(this.node1, other.node1)) {
            return false;
        }
        if (!Objects.equals(this.node2, other.node2)) {
            return false;
        }
        if (Objects.equals(this.node1, other.node2) && Objects.equals(this.node2, other.node1)) {
            return true;
        }
        return true;
    }
}