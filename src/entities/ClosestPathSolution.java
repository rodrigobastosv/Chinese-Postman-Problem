package entities;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author rodrigo
 */
public class ClosestPathSolution implements Serializable {

    Vertex beginVertex, endVertex;
    int weight;
    List<Vertex> shortestPath;

    public ClosestPathSolution(Vertex beginVertex, Vertex endVertex, int weight, List<Vertex> shortestPath) {
        this.beginVertex = beginVertex;
        this.endVertex = endVertex;
        this.weight = weight;
        this.shortestPath = shortestPath;
    }

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

    @Override
    public String toString() {
        String s = "";
        s += getBeginVertex().getId() + " para " + getEndVertex().getId();
        s += " tem custo " + getWeight();
        s += " e o caminho é: " + getShortestPath();
        return s;
    }
}
