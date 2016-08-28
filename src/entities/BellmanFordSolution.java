package entities;

//<editor-fold defaultstate="collapsed" desc="Imports">
import java.io.Serializable;
import java.util.List;
//</editor-fold>

@Deprecated
public class BellmanFordSolution implements Serializable {

    Vertex beginVertex, endVertex;
    int weight;
    List<Vertex> shortestPath;

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    public BellmanFordSolution(Vertex beginVertex, Vertex endVertex, int weight, List<Vertex> shortestPath) {
        this.beginVertex = beginVertex;
        this.endVertex = endVertex;
        this.weight = weight;
        this.shortestPath = shortestPath;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Gets & Sets">
    public Vertex getBeginVertex() {
        return beginVertex;
    }

    public Vertex getEndVertex() {
        return endVertex;
    }

    public int getWeight() {
        return weight;
    }

    public List<Vertex> getShortestPath() {
        return shortestPath;
    }

    public void setBeginVertex(Vertex beginVertex) {
        this.beginVertex = beginVertex;
    }

    public void setEndVertex(Vertex endVertex) {
        this.endVertex = endVertex;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setShortestPath(List<Vertex> shortestPath) {
        this.shortestPath = shortestPath;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Overrides">
    @Override
    public String toString() {
        String s = "";
        s += getBeginVertex().getId() + " para " + getEndVertex().getId();
        s += " tem custo " + getWeight();
        s += " e o caminho é: " + getShortestPath();
        return s;
    }
    //</editor-fold>
}
