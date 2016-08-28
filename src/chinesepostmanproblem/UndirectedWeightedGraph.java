package chinesepostmanproblem;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel;
import entities.ClosestPathSolution;
import entities.CplexResult;
import entities.CplexResultVariable;
import entities.Edge;
import entities.Vertex;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.apache.commons.collections15.Factory;
import util.TransformEdgeColor;
import util.TransformEdgeLabel;
import util.TransformVertexColor;
import util.TransformVertexLabel;
import util.TransformWeightDijkstra;
//</editor-fold>

public class UndirectedWeightedGraph implements Cloneable, Serializable {

    UndirectedGraph<Vertex, Edge> graph = new UndirectedSparseMultigraph();    
    List<ClosestPathSolution> closestPathSolutions = new ArrayList<>();
    List<Vertex> eulerianPath = new ArrayList<>();
    IloCplex cplex;
    CplexResult result;
    boolean useEdmondsAlgorithm;
    String rootDir = System.getProperty("user.dir");
    static long t0, t1;

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    public UndirectedWeightedGraph() {
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Read the graph from a file">
    public void readGraph(String path) throws FileNotFoundException, IOException {
        FileReader file = new FileReader(path);
        Scanner scanner = new Scanner(file);
        scanner.next(); //skip the first line of the file 
        scanner.next(); //(#vertices, #edges)

        while (scanner.hasNext()) {
            int vertex1 = Integer.parseInt(scanner.next());
            int vertex2 = Integer.parseInt(scanner.next());
            int weight = Integer.parseInt(scanner.next());
            if (!graph.containsVertex(getVertex(vertex1))) {
                graph.addVertex(new Vertex(vertex1));
            }
            if (!graph.containsVertex(getVertex(vertex2))) {
                graph.addVertex(new Vertex(vertex2));
            }
            graph.addEdge(new Edge(getVertex(vertex1), getVertex(vertex2), weight),
                    getVertex(vertex1), getVertex(vertex2), EdgeType.UNDIRECTED);
        }
        decideToUseEdmondsAlgorithm(graph, getNumberOddVertices());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Duplication of the edges via Edmonds">
    public void duplicateEdgesEdmonds(String path) throws FileNotFoundException {
        FileReader file = new FileReader(path);
        Scanner scanner = new Scanner(file);
        scanner.nextLine();
        while (scanner.hasNext()) {
            int vertex1 = Integer.parseInt(scanner.next());
            int vertex2 = Integer.parseInt(scanner.next());
            for (ClosestPathSolution cps : getClosestPathSolutions()) {
                if (vertex2 == cps.getBeginVertex().getId() && vertex1 == cps.getEndVertex().getId()) {
                    duplicateEdgeFromSolution(cps);
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Generation of the graph file for the edmonds algorithm">
    public void prepareEdmondsGraphFile(String path) throws FileNotFoundException, IOException {
        FileReader file = new FileReader(path);
        FileWriter fileW = new FileWriter("./edmondsStuff/GRAPH.txt");
        Scanner scanner = new Scanner(file);
        PrintWriter writer = new PrintWriter(fileW);

        writeGraphFileInit(writer);
        writeGraphNumVerticesAndEdges(writer);
        writeNumIncidentsArcs(writer);
        writeEdgesAndCosts(writer);
        writeStartVertex(writer);
        writeEndFile(writer);
    }

    private void writeGraphFileInit(PrintWriter pw) {
        pw.println("0");
    }

    private void writeGraphNumVerticesAndEdges(PrintWriter pw) {
        pw.println(graph.getVertexCount() + " " + graph.getEdgeCount());
    }

    private void writeNumIncidentsArcs(PrintWriter pw) {
        int[] numIncidentArcs = returnNumIncidentsArcs();
        for (int i = 1; i < numIncidentArcs.length; i++) {
            pw.print(numIncidentArcs[i] + " ");
        }
        pw.println();
    }

    private void writeEdgesAndCosts(PrintWriter pw) throws FileNotFoundException {
        FileReader file = new FileReader("./samples/Undirected/GRAPH.txt");
        Scanner scanner = new Scanner(file);
        scanner.next(); //skip the first line of the file 
        scanner.next(); //(#vertices, #edges)

        while (scanner.hasNext()) {
            int vertex1 = Integer.parseInt(scanner.next());
            int vertex2 = Integer.parseInt(scanner.next());
            int weight = Integer.parseInt(scanner.next());
            pw.println(String.format("%2s", vertex1) + " " + String.format("%2s", vertex2) + " " + String.format("%3s", weight));
        }
    }

    private void writeStartVertex(PrintWriter pw) {
        pw.println("1");
    }

    private void writeEndFile(PrintWriter pw) {
        pw.println("99");
        pw.flush();
        pw.close();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Decide if Edmonds Algorithm will be used">
    public void decideToUseEdmondsAlgorithm(UndirectedGraph<Vertex, Edge> graph, int numVerticesOddDegree) {
        if (numVerticesOddDegree > 33) { //CPLEX free allows 1000 variables (33*33)
            setUseEdmondsAlgorithm(true);
        } else {
            setUseEdmondsAlgorithm(false);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Return a vector with the num of incidente arcs os each vertex">
    private int[] returnNumIncidentsArcs() {
        int[] numIncidentsArcs = new int[graph.getVertexCount() + 1];
        for (Vertex v : graph.getVertices()) {
            numIncidentsArcs[v.getId()] = graph.getNeighborCount(v);
        }
        return numIncidentsArcs;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Return the number of odd vertices in the graph">
    public int getNumberOddVertices() {
        int n = 0;
        for (Vertex v : graph.getVertices()) {
            if (graph.degree(v) % 2 != 0) {
                n++;
            }
        }
        return n;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Display the graph using JUNG">
    public void displayGraph() throws InterruptedException {
        Layout layout = new ISOMLayout(graph);
        layout.setSize(new Dimension(700, 700)); // sets the initial size of the space        

        VisualizationViewer<Vertex, Edge> vv = new VisualizationViewer<>(layout);
        vv.setPreferredSize(new Dimension(750, 750)); //Sets the viewing area size
        vv.setBackground(Color.WHITE);
        HashMap<Key, Object> renderingHints = new HashMap<Key, Object>();
        renderingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        vv.setRenderingHints(renderingHints);

        DefaultModalGraphMouse<Vertex, Edge> graphMouse = new DefaultModalGraphMouse<Vertex, Edge>();
        vv.setGraphMouse(graphMouse);
        vv.addKeyListener(graphMouse.getModeKeyListener());
        GraphZoomScrollPane pane = new GraphZoomScrollPane(vv);

        RenderContext ctx = vv.getRenderContext();
        ctx.setVertexLabelTransformer(new TransformVertexLabel());
        ctx.setVertexDrawPaintTransformer(new TransformVertexColor());
        VertexLabel<Vertex, Edge> vl = vv.getRenderer().getVertexLabelRenderer();
        vl.setPosition(Renderer.VertexLabel.Position.CNTR);
        ctx.setEdgeDrawPaintTransformer(new TransformEdgeColor());
        ctx.setEdgeLabelTransformer(new TransformEdgeLabel());

        JFrame frame = new JFrame("Eulerian Graph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
        runTroughVertices(vv,layout);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Return if the graph is eulerian or not">
    public boolean isEulerian() {
        for (Vertex v : graph.getVertices()) {
            if ((graph.degree(v) % 2) != 0) {
                return false;
            }
        }
        return true;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Create a PDF with the report">
    public void createReport(long time) {
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream("report.pdf"));
            document.open();

            Font fonteCabecalho = new Font(Font.FontFamily.COURIER, 18, Font.BOLD);
            Font fonteHeader = new Font(Font.FontFamily.COURIER, 16, Font.BOLD);

            Paragraph title = new Paragraph("Problema do Carteiro Chinês\n\n", fonteCabecalho);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph informacoesGerais = new Paragraph("Informações Gerais\n\n", fonteHeader);
            informacoesGerais.setAlignment(Element.ALIGN_CENTER);
            document.add(informacoesGerais);
            PdfPTable table = new PdfPTable(2);
            table.addCell("Tipo de PCC");
            table.addCell("Não Dirigido");

            table.addCell("Método de Resolução");
            if (isUseEdmondsAlgorithm()) {
                table.addCell("Algoritmo de Edmonds");
            } else {
                table.addCell("Modelo de Programação Linear");
            }

            table.addCell("Número de Arestas Duplicadas");
            table.addCell(String.valueOf(getNumberOfDuplicatedEdges()));

            table.addCell("Arestas Duplicadas");
            table.addCell(getStringOfDuplicatedEdges());

            table.addCell("Soma das Arestas Duplicadas");            
            table.addCell(String.valueOf(getSumOfDuplicatedEdges()));            

            table.addCell("Número de Arestas do Grafo");
            table.addCell(String.valueOf(graph.getEdgeCount()));

            table.addCell("Soma das Arestas do Grafo");
            table.addCell(String.valueOf(getPathValue()));

            table.addCell("Tempo de Execução (s)");
            table.addCell(String.valueOf((float) time / (float) 1000));

            document.add(table);
            document.add(new Paragraph(" "));

            Paragraph circuitoCarteiro = new Paragraph("Circuito do Carteiro\n\n", fonteHeader);
            circuitoCarteiro.setAlignment(Element.ALIGN_CENTER);
            document.add(circuitoCarteiro);
            PdfPTable tableCircuit = new PdfPTable(4);
            tableCircuit.addCell("Vértice Origem");
            tableCircuit.addCell("Vértice Destino");
            tableCircuit.addCell("Custo");
            tableCircuit.addCell("Custo Acumulado");

            Vertex v, next;
            Edge e;
            int cost = 0;
            for (int i = 0; i < eulerianPath.size() - 1; i++) {
                v = eulerianPath.get(i);
                next = eulerianPath.get(i + 1);
                e = getEdge2(v.getId(), next.getId());
                cost += e.getWeight();
                tableCircuit.addCell(String.valueOf(v.getId()));
                tableCircuit.addCell(String.valueOf(next.getId()));
                tableCircuit.addCell(String.valueOf(e.getWeight()));
                tableCircuit.addCell(String.valueOf(cost));
            }
            document.add(tableCircuit);

        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        document.close();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Open the PDF report">
    public void openReport() throws IOException {
        File file = new File("./report.pdf");
        Desktop.getDesktop().open(file);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Gather information for the report">
    private int getNumberOfDuplicatedEdges() {
        int numberOfDuplicatedEdges = 0;
        for (Edge e : graph.getEdges()) {
            if (e.isDuplicated()) {
                numberOfDuplicatedEdges++;
            }
        }
        return numberOfDuplicatedEdges;
    }
    
    private int getSumOfDuplicatedEdges() {
        int sumOfDuplicatedEdges = 0;
        for (Edge e : graph.getEdges()) {
            if (e.isDuplicated()) {
                sumOfDuplicatedEdges += e.getWeight();
            }
        }
        return sumOfDuplicatedEdges;
    }

    private String getStringOfDuplicatedEdges() {
        String stringOfDuplicatedEdges = "";
        for (Edge e : graph.getEdges()) {
            if (e.isDuplicated()) {
                stringOfDuplicatedEdges += "(" + e.getNode1().getId() + "," + e.getNode2().getId() + ") ";
            }
        }
        return stringOfDuplicatedEdges;
    }

    private int getPathValue() {
        int pathValue = 0;
        for (Edge e : graph.getEdges()) {
            pathValue += e.getWeight();
        }
        return pathValue;
    }

    private String getStringOfEulerianPath() {
        String path = "";
        Vertex v;
        for (int i = 0; i < eulerianPath.size(); i++) {
            v = eulerianPath.get(i);
            path = path.concat(String.valueOf(v.getId()));
            if ((i + 1) % 10 == 0) {
                path = path.concat("\n");
            }
            if (i != eulerianPath.size() - 1) {
                path = path.concat("->");
            }
        }
        return path;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Run updating the edges of the graph with a timer">
    private void runTroughVertices(VisualizationViewer<Vertex, Edge> vv, Layout layout) {
        Vertex start, end;
        Edge e;
        Point2D vertexPoint;
        PickedState<Vertex> pickVertex = vv.getPickedVertexState();

        start = eulerianPath.get(0);
        pickVertex.pick(getVertex(start.getId()), true);
        try {
            for (int i = 1; i < eulerianPath.size(); i++) {
                start = eulerianPath.get(i - 1);
                end = eulerianPath.get(i);
                e = getEdgeNotVisited(start.getId(), end.getId());
                e.setVisited(true);
                pickVertex.pick(getVertex(end.getId()), true);
                vertexPoint = (Point2D) layout.transform(getVertex(end.getId()));
                //Uncoment to iterate
                //vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).setScale(2.0, 1.0, vertexPoint);
                vv.repaint();
                Thread.sleep(1000);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Remove the vertices that have even degree">
    public void removeVerticesWithEvenDegree() {
        List<Integer> listVerticesRemoved = new ArrayList();
        for (Vertex v : graph.getVertices()) {
            if ((graph.degree(v) % 2) == 0) {
                listVerticesRemoved.add(v.getId());
            }
        }
        for (Integer i : listVerticesRemoved) {
            graph.removeVertex(getVertex(i));
        }
    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="Create solutions for the vertices using Dijkstra's">
    public List<ClosestPathSolution> createListOfClosestPathSolutions() {
        List<ClosestPathSolution> solutions = new ArrayList<>();
        DijkstraShortestPath<Vertex, Edge> d = new DijkstraShortestPath<>(graph, new TransformWeightDijkstra());
        int weight;
        List<Vertex> path = new ArrayList<>();

        for (Vertex v : graph.getVertices()) {
            for (Vertex otherV : graph.getVertices()) {
                if (v.getId() < otherV.getId()) {
                    weight = (int) Math.round((double) d.getDistance(v, otherV));
                    path = getClosestPath(v, otherV);
                    solutions.add(new ClosestPathSolution(v, otherV, weight, path));
                }
            }
        }
        closestPathSolutions = solutions;
        return solutions;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Get closest path beetween vertices using Dijkstra's">
    public List<Vertex> getClosestPath(Vertex n1, Vertex n2) {
        List<Vertex> path = new ArrayList<>();
        DijkstraShortestPath<Vertex, Edge> d = new DijkstraShortestPath<>(graph, new TransformWeightDijkstra());
        for (Edge e : d.getPath(n1, n2)) {
            if (!path.contains(e.getNode1())) {
                path.add(e.getNode1());
            }
            if (!path.contains(e.getNode2())) {
                path.add(e.getNode2());
            }
        }
        return path;
    }
    //</editor-fold>              

    //<editor-fold defaultstate="collapsed" desc="Search for a Dijkstra solution in the solutions List">
    private ClosestPathSolution getClosestPathSolution(int n1, int n2) {
        ClosestPathSolution solution = null;
        for (ClosestPathSolution cps : closestPathSolutions) {
            if (cps.getBeginVertex().getId() == n1 && cps.getEndVertex().getId() == n2) {
                solution = cps;
            }
        }
        return solution;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Add edges to aquire a complete graph">
    public void addEdgesToCompleteGraph() { //weight of edges must be calculated by dijkstra's        
        ClosestPathSolution closestPathSolution = null;
        BellmanFordSolution bellmanFordSolution = null;
        for (Vertex v : graph.getVertices()) {
            for (Vertex otherV : graph.getVertices()) {
                if (v.getId() != otherV.getId() && v.getId() < otherV.getId()) {
                    if (!graph.containsEdge(getEdge(v.getId(), otherV.getId()))) {
                        closestPathSolution = getClosestPathSolution(v.getId(), otherV.getId());
                        graph.addEdge(new Edge(getVertex(v.getId()), getVertex(otherV.getId()),
                                closestPathSolution.getWeight()), getVertex(v.getId()),
                                getVertex(otherV.getId()), EdgeType.UNDIRECTED);
                    }
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Print the graph in a readable way">
    public void printGraph() {
        System.out.println("Number of Vertices: " + graph.getVertexCount());
        System.out.println("Number of Edges: " + graph.getEdgeCount());

        System.out.println("Vertices:");
        for (Vertex v : graph.getVertices()) {
            System.out.print(v.getId() + ":");
            for (Vertex vAdj : graph.getNeighbors(v)) {
                System.out.print(vAdj.getId() + " ");
            }
            System.out.println();
        }

        System.out.println("Edges:");
        for (Edge e : graph.getEdges()) {
            System.out.println(e.getNode1().getId() + "-" + e.getNode2().getId() + ":" + e.getWeight() + "(" + e.isDuplicated() + ")");
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Duplication of the edges">
    public void duplicateEdges() {
        int v1, v2;
        for (CplexResultVariable variable : result.getVariables()) {
            if (variable.getValue() == 1) {
                v1 = getFirstPart(variable.getName());
                v2 = getSecondPart(variable.getName());
                for (ClosestPathSolution cps : getClosestPathSolutions()) {
                    if (v1 == cps.getBeginVertex().getId() && v2 == cps.getEndVertex().getId()) {
                        duplicateEdgeFromSolution(cps);
                    }
                }
            }
        }
    }

    //private void duplicateEdgeFromSolution(BellmanFordSolution bfs) {
    private void duplicateEdgeFromSolution(ClosestPathSolution cps) {
        Edge e = new Edge();
        for (int i = 0; i < cps.getShortestPath().size() - 1; i++) {
            e = graph.findEdge(cps.getShortestPath().get(i), cps.getShortestPath().get(i + 1));
            graph.addEdge(new Edge(e.getNode1(), e.getNode2(), e.getWeight(), true),
                    e.getNode1(), e.getNode2(), EdgeType.UNDIRECTED);
        }
    }

    private int getFirstPart(String variable) {
        String s = variable.replace("X", "");
        return Integer.parseInt(s.substring(0, s.length() / 2));
    }

    private int getSecondPart(String variable) {
        String s = variable.replace("X", "");
        return Integer.parseInt(s.substring(s.length() / 2, s.length()));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Return a vertex from a given id">
    private Vertex getVertex(int id) {
        Vertex vertex = null;
        for (Vertex v : graph.getVertices()) {
            if (v.getId() == id) {
                vertex = v;
            }
        }
        return vertex;
    }
    //</editor-fold>          

    //<editor-fold defaultstate="collapsed" desc="Return an Eulerian path using Fleury's algorithm">
    public List<Vertex> findEulerianPathFrom(int s) {
        ArrayList<Vertex> eulerianPath = new ArrayList<>();
        ArrayList<Edge> edges;
        Vertex start = getVertex(s);
        eulerianPath.add(start);

        do {
            edges = new ArrayList<>(graph.getEdges()); //work with the edges that the graph has in the moment
            for (Edge e : edges) {
                if (graph.isIncident(start, e)) { //find the edge in the edge set
                    Vertex dest = graph.getOpposite(start, e);
                    graph.removeEdge(e); //remove the edge to check if it is a bridge
                    Vertex v = (graph.degree(e.getNode1()) == 0) ? e.getNode2() : e.getNode1();
                    if (graph.degree(dest) == 0 && graph.degree(start) > 0) { //first check if its a bridge
                        graph.addEdge(new Edge(e.getNode1(), e.getNode2(), e.getWeight()),
                                e.getNode1(), e.getNode2(), EdgeType.UNDIRECTED);
                    } else if (isGraphConected(v)) { //check if the graph still conected after the removal of the edge
                        start = dest;
                        break;
                    } else { //if enter here the edge was a bridge, so put it back
                        graph.addEdge(new Edge(e.getNode1(), e.getNode2(), e.getWeight()),
                                e.getNode1(), e.getNode2(), EdgeType.UNDIRECTED);
                    }
                }
            }
            eulerianPath.add(start);
        } while (graph.getEdgeCount() > 1);

        /* BAD SOLUTION TO PUT THE LAST VERTEX (FIX IT!)*/
        for (Edge e : graph.getEdges()) {
            Vertex dest = graph.getOpposite(start, e);
            eulerianPath.add(dest);
        }
        return eulerianPath;
    }

    private int BFS(Vertex start) {
        int visitedVertices = 0;
        Queue<Vertex> Q = new LinkedList<>();

        for (Vertex v : graph.getVertices()) {
            v.setVisited(false);
        }
        start.setVisited(true);
        visitedVertices++;

        Q.add(start);
        while (!Q.isEmpty()) {
            Vertex v = Q.poll();
            for (Vertex vAdj : graph.getNeighbors(v)) {
                if (!vAdj.isVisited()) {
                    Q.add(vAdj);
                    vAdj.setVisited(true);
                    visitedVertices++;
                }
            }
        }
        return visitedVertices;
    }

    private int numVerticesWithDegree() {
        int numVerticesWithDegree = 0;
        for (Vertex v : graph.getVertices()) {
            if (graph.degree(v) > 0) {
                numVerticesWithDegree++;
            }
        }
        return numVerticesWithDegree;
    }

    private boolean isGraphConected(Vertex start) {
        int numVerticesFound = BFS(start);
        if (numVerticesFound == numVerticesWithDegree()) {
            return true;
        }
        return false;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Return an edge given two vertices">
    private Edge getEdge(int n1, int n2) {
        Edge edge = null;
        for (Edge e : graph.getEdges()) {
            if (e.getNode1().getId() == n1 && e.getNode2().getId() == n2) {
                edge = e;
            }
        }
        return edge;
    }

    private Edge getEdge2(int n1, int n2) {
        Edge edge = null;
        for (Edge e : graph.getEdges()) {
            if (e.getNode1().getId() == n1 && e.getNode2().getId() == n2) {
                edge = e;
            }
            if (e.getNode1().getId() == n2 && e.getNode2().getId() == n1) {
                edge = e;
            }
        }
        return edge;
    }

    private Edge getEdgeNotVisited(int n1, int n2) {
        Edge edge = null;
        for (Edge e : graph.getEdges()) {
            if ((e.getNode1().getId() == n1 && e.getNode2().getId() == n2) || (e.getNode1().getId() == n2 && e.getNode2().getId() == n1)) {
                if (!e.isVisited()) {
                    edge = e;
                }
            }
        }
        return edge;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Generation of the String of the Mathematical Model">
    public void generateMathematicalModel() {
        String mathematicalModel = "";
        mathematicalModel += generateObjectiveFunction();
        mathematicalModel += generateST();
        mathematicalModel += generateRestrictions();
        mathematicalModel += generateEND();
        System.out.println(mathematicalModel);
    }

    private String generateObjectiveFunction() {
        boolean firstTime = true;
        String objectiveFunction = "MIN ";

        for (Edge e : graph.getEdges()) {
            if (firstTime) {
                objectiveFunction += addVariableToFunction(e);
                firstTime = false;
            } else {
                objectiveFunction += " + " + addVariableToFunction(e);
            }
        }
        objectiveFunction += "\n";
        return objectiveFunction;
    }

    private String generateRestrictions() {
        boolean firstTime = true;
        String restrictions = "";
        for (Vertex v : graph.getVertices()) {
            for (Vertex vAdj : graph.getNeighbors(v)) {
                if (firstTime) {
                    restrictions += addVariableToRestriction(v, vAdj);
                    firstTime = false;
                } else {
                    restrictions += " + " + addVariableToRestriction(v, vAdj);
                }
            }
            restrictions += " = 1\n";
            firstTime = true;
        }
        return restrictions;
    }

    private String generateST() {
        return "ST\n";
    }

    private String generateEND() {
        return "END\n";
    }

    private String addVariableToRestriction(Vertex v, Vertex v2) {
        String firstHalf, secondHalf;
        int slotsVertices = String.valueOf(graph.getVertexCount()).length();
        firstHalf = formatter(v.getId(), slotsVertices);
        secondHalf = formatter(v2.getId(), slotsVertices);
        if (Integer.parseInt(firstHalf) > Integer.parseInt(secondHalf)) {
            return "X" + secondHalf + firstHalf;
        }
        return "X" + firstHalf + secondHalf;
    }

    private String addVariableToFunction(Edge e) {
        String firstHalf, secondHalf;
        int slotsVertices = String.valueOf(graph.getVertexCount()).length();
        firstHalf = formatter(e.getNode1().getId(), slotsVertices);
        secondHalf = formatter(e.getNode2().getId(), slotsVertices);
        return e.getWeight() + "X" + firstHalf + secondHalf;
    }

    private String formatter(int id, int size) {
        String format = "%0" + size + "d";
        return String.format(format, id);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Generation the Mathematical Model using CPLEX">
    public void generateSolveMathematicalModelCplex() throws IloException {
        cplex = new IloCplex();
        IloNumVar[] variables = new IloNumVar[graph.getEdgeCount()];
        IloLinearNumExpr objective = cplex.linearNumExpr();
        List<IloRange> constraints = new ArrayList<IloRange>();

        addVariablesModelCplex(variables);
        addObjectiveFunctionModelCplex(objective, variables);
        addFunctionType(objective);
        addRestrictionsModelCplex(objective, constraints, variables);
        solveModelCplex(cplex, variables);
    }

    private void solveModelCplex(IloCplex cplex, IloNumVar[] variables) throws IloException {
        List<CplexResultVariable> listVariables = new ArrayList<>();

        cplex.solve();
        for (int i = 0; i < variables.length; i++) {
            listVariables.add(new CplexResultVariable(variables[i].getName(),
                    (int) cplex.getValue(variables[i])));
        }
        result = new CplexResult((int) cplex.getObjValue(), listVariables);
    }

    private void addFunctionType(IloLinearNumExpr objective) throws IloException {
        cplex.addMinimize(objective);
    }

    private void addVariablesModelCplex(IloNumVar[] variables) throws IloException {
        int i = 0;
        for (Edge e : graph.getEdges()) {
            variables[i] = cplex.boolVar(addVariableToFunctionCplex(e));
            i++;
        }
    }

    private String addVariableToFunctionCplex(Edge e) {
        String firstHalf, secondHalf;
        int slotsVertices = String.valueOf(graph.getVertexCount()).length();
        firstHalf = formatter(e.getNode1().getId(), slotsVertices);
        secondHalf = formatter(e.getNode2().getId(), slotsVertices);
        return "X" + firstHalf + secondHalf;
    }

    private void addObjectiveFunctionModelCplex(IloLinearNumExpr objective, IloNumVar[] variables) throws IloException {
        int i = 0;
        for (Edge e : graph.getEdges()) {
            objective.addTerm(e.getWeight(), variables[i]);
            i++;
        }
    }

    private void addRestrictionsModelCplex(IloLinearNumExpr objective, List<IloRange> constraints, IloNumVar[] variables) throws IloException {
        List<String> restrictionsNames;
        String restrictionName;
        IloLinearNumExpr ils;
        for (Vertex v : graph.getVertices()) {
            IloNumExpr expressao;
            IloNumVar[] vars = new IloNumVar[3];
            restrictionsNames = new ArrayList<>();
            ils = cplex.linearNumExpr();
            for (Vertex vAdj : graph.getNeighbors(v)) {
                for (IloNumVar var : variables) {
                    if (addVariableToRestriction(v, vAdj).equals(var.getName())) {
                        ils.addTerm(var, 1.0);
                    }
                }
            }
            constraints.add(cplex.addEq(ils, 1));
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Generation of the DOT file of the graph">
    public void generateDotFile(String filename) throws IOException {
        FileWriter dotGraphFile = new FileWriter(filename);
        dotGraphFile.write("graph G{\n");
        for (Edge e : graph.getEdges()) {
            if (e.isDuplicated()) {
                dotGraphFile.write(e.getNode1().getId() + "--" + e.getNode2().getId() + "[label=" + e.getWeight()
                        + ", color=blue]\n");
            } else {
                dotGraphFile.write(e.getNode1().getId() + "--" + e.getNode2().getId() + "[label=" + e.getWeight()
                        + "]\n");
            }
        }
        dotGraphFile.write("}");
        dotGraphFile.close();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Find Eulerian Graph using Edmonds">
    private void findEulerianGraphUsingEdmonds(UndirectedWeightedGraph graph) throws IOException {
        graph.prepareEdmondsGraphFile("./samples/Undirected/GRAPH.txt");
        String pathScript = graph.getRootDir() + "/bin/script.sh";
        Process p = Runtime.getRuntime().exec(pathScript);
        graph.duplicateEdgesEdmonds("./edmondsStuff/XIMBIL");
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Find Eulerian Graph using LP">
    private void findEulerianGraphUsingLinearProgramming(UndirectedWeightedGraph graph) throws CloneNotSupportedException, IloException {
        UndirectedWeightedGraph oddDegreeVerticesGraph = (UndirectedWeightedGraph) graph.clone();
        oddDegreeVerticesGraph.removeVerticesWithEvenDegree();
        oddDegreeVerticesGraph.setClosestPathSolutions(graph.getClosestPathSolutions());
        oddDegreeVerticesGraph.addEdgesToCompleteGraph();
        oddDegreeVerticesGraph.generateSolveMathematicalModelCplex();

        graph.setResult(oddDegreeVerticesGraph.getResult()); //put the result inside of the original graph                        
        graph.duplicateEdges(); //duplicate the edges to get an eulerian graph
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Visualization of the Graph">
    private UndirectedWeightedGraph visualizationOfGraph(UndirectedWeightedGraph graph) throws InterruptedException, IOException, CloneNotSupportedException {
        UndirectedWeightedGraph visualizationGraph = (UndirectedWeightedGraph) graph.clone();
        visualizationGraph.setEulerianPath(graph.findEulerianPathFrom(1));
        t1 = System.currentTimeMillis();
        visualizationGraph.displayGraph();
        return visualizationGraph;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Visualization of the Report">
    private void visualizationOfReport(UndirectedWeightedGraph graph, long initTimer, long endTimer) throws CloneNotSupportedException, IOException {
        UndirectedWeightedGraph reportGraph = (UndirectedWeightedGraph) graph.clone();
        reportGraph.createReport(endTimer - initTimer);
        reportGraph.openReport();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Gets & Sets">
    public boolean isUseEdmondsAlgorithm() {
        return useEdmondsAlgorithm;
    }

    public void setUseEdmondsAlgorithm(boolean useEdmondsAlgorithm) {
        this.useEdmondsAlgorithm = useEdmondsAlgorithm;
    }

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public UndirectedGraph<Vertex, Edge> getGraph() {
        return graph;
    }

    public void setGraph(UndirectedGraph<Vertex, Edge> graph) {
        this.graph = graph;
    }    

    public List<Vertex> getEulerianPath() {
        return eulerianPath;
    }

    public void setEulerianPath(List<Vertex> eulerianPath) {
        this.eulerianPath = eulerianPath;
    }    

    public List<ClosestPathSolution> getClosestPathSolutions() {
        return closestPathSolutions;
    }

    public void setClosestPathSolutions(List<ClosestPathSolution> closestPathSolutions) {
        this.closestPathSolutions = closestPathSolutions;
    }

    public CplexResult getResult() {
        return result;
    }

    public void setResult(CplexResult result) {
        this.result = result;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Overrides">
    @Override
    public Object clone() throws CloneNotSupportedException {
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
    //</editor-fold>
    
    public static void main(String args[]) throws IOException, CloneNotSupportedException, IloException, InterruptedException {
        UndirectedWeightedGraph graph = new UndirectedWeightedGraph();
        UndirectedWeightedGraph reportGraph = new UndirectedWeightedGraph();        
        t0 = System.currentTimeMillis();
        graph.readGraph("./samples/Undirected/GRAPH.txt");
        if (!graph.isEulerian()) {
            graph.setClosestPathSolutions(graph.createListOfClosestPathSolutions());
            if (graph.isUseEdmondsAlgorithm()) {
                graph.findEulerianGraphUsingEdmonds(graph);
            } else {
                graph.findEulerianGraphUsingLinearProgramming(graph);
            }
        }        
        reportGraph = graph.visualizationOfGraph(graph);
        graph.visualizationOfReport(reportGraph, t0, t1);
    }
}