package chinesepostmanproblem;

//<editor-fold defaultstate="collapsed" desc="Imports">
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
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import entities.ClosestPathSolution;
import entities.CplexResult;
import entities.CplexResultVariable;
import entities.Edge;
import entities.Vertex;
import entities.VertexDegree;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import util.TransformEdgeColor;
import util.TransformEdgeLabel;
import util.TransformVertexColor;
import util.TransformVertexLabel;
import util.TransformWeightDijkstra;
//</editor-fold>

public class DirectedWeightedGraph implements Cloneable, Serializable {

    DirectedGraph<Vertex, Edge> graph = new DirectedSparseMultigraph<>();
    List<ClosestPathSolution> closestPathSolutions = new ArrayList<>();
    List<Vertex> eulerianPath = new ArrayList<>();
    List<VertexDegree> verticesDegree = new ArrayList<>();
    IloCplex cplex;
    CplexResult result;

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
                    getVertex(vertex1), getVertex(vertex2), EdgeType.DIRECTED);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Display the graph using JUNG">
    public void displayGraph() throws InterruptedException {
        Layout layout = new ISOMLayout(graph);
        layout.setSize(new Dimension(700, 700)); // sets the initial size of the space        

        VisualizationViewer<Vertex, Edge> vv = new VisualizationViewer<>(layout);
        vv.setPreferredSize(new Dimension(750, 750)); //Sets the viewing area size
        vv.setBackground(Color.WHITE);
        HashMap<RenderingHints.Key, Object> renderingHints = new HashMap<RenderingHints.Key, Object>();
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
        Renderer.VertexLabel<Vertex, Edge> vl = vv.getRenderer().getVertexLabelRenderer();
        vl.setPosition(Renderer.VertexLabel.Position.CNTR);
        ctx.setEdgeDrawPaintTransformer(new TransformEdgeColor());
        ctx.setEdgeLabelTransformer(new TransformEdgeLabel());

        JFrame frame = new JFrame("Eulerian Graph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
        runTroughVertices(vv);
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
            
            Paragraph title = new Paragraph("Problema do Carteiro Chin�s\n\n", fonteCabecalho);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            Paragraph informacoesGerais = new Paragraph("Informa��es Gerais\n\n", fonteHeader);
            informacoesGerais.setAlignment(Element.ALIGN_CENTER);
            document.add(informacoesGerais);
            PdfPTable table = new PdfPTable(2);                                                            
            table.addCell("Tipo de PCC");
            table.addCell("Dirigido");
            
            table.addCell("N�mero de Arestas Duplicadas");
            table.addCell(String.valueOf(getNumberOfDuplicatedEdges()));
            
            table.addCell("Arestas Duplicadas");
            table.addCell(getStringOfDuplicatedEdges());
            
            table.addCell("Soma das Arestas Duplicadas");
            table.addCell(String.valueOf(result.getObjectiveFunctionValue()));                        
            
            table.addCell("N�mero de Arestas do Grafo");            
            table.addCell(String.valueOf(graph.getEdgeCount()));
            
            table.addCell("Soma das Arestas do Grafo");
            table.addCell(String.valueOf(getPathValue()));
            
            table.addCell("Tempo de Execu��o (s)");
            table.addCell(String.valueOf((float)time/(float)1000));
            
            document.add(table);
            document.add(new Paragraph(" "));
            
            Paragraph circuitoCarteiro = new Paragraph("Circuito do Carteiro\n\n", fonteHeader);
            circuitoCarteiro.setAlignment(Element.ALIGN_CENTER);
            document.add(circuitoCarteiro);                        
            PdfPTable tableCircuit = new PdfPTable(4);                        
            tableCircuit.addCell("V�rtice Origem");
            tableCircuit.addCell("V�rtice Destino");
            tableCircuit.addCell("Custo");
            tableCircuit.addCell("Custo Acumulado");
            
            Vertex v, next;
            Edge e;
            int cost = 0;            
            for(int i=0 ; i<eulerianPath.size()-1 ; i++) {
                v = eulerianPath.get(i);
                next = eulerianPath.get(i+1);
                e = getEdge(v.getId(), next.getId());                
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
        for(Edge e : graph.getEdges()) {
            if(e.isDuplicated()) {
                numberOfDuplicatedEdges++;
            }
        }
        return numberOfDuplicatedEdges;
    }
    
    private String getStringOfDuplicatedEdges() {
        String stringOfDuplicatedEdges = "";
        for(Edge e : graph.getEdges()) {
            if(e.isDuplicated()) {
                stringOfDuplicatedEdges += "(" + e.getNode1().getId() + "," + e.getNode2().getId() + ") ";
            }
        }
        return stringOfDuplicatedEdges;
    }
    
    private int getPathValue() {
        int pathValue = 0;
        for(Edge e : graph.getEdges()) {
            pathValue += e.getWeight();
        }
        return pathValue;
    }
    
    private String getStringOfEulerianPath() {
        String path = "";
        Vertex v;
        for(int i=0 ; i<eulerianPath.size() ; i++) {
            v = eulerianPath.get(i);
            path = path.concat(String.valueOf(v.getId()));
            if((i+1) % 10 == 0) {
                path = path.concat("\n");
            }
            if(i != eulerianPath.size()-1) {
                path = path.concat("->");
            }
        }
        return path;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Run updating the edges of the graph with a timer">
    private void runTroughVertices(VisualizationViewer<Vertex, Edge> vv) {
        Vertex start, end;
        Edge e;
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
                vv.repaint();
                Thread.sleep(1000);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Create a list of the vertices and theirs degrees">
    public List<VertexDegree> createListOfVerticesDegree() {
        List<VertexDegree> listVerticesDegree = new ArrayList<>();
        int inDegree, outDegree;
        for (Vertex v : graph.getVertices()) {            
            inDegree = graph.getInEdges(v).size();
            outDegree = graph.getOutEdges(v).size();
            VertexDegree vd = new VertexDegree(v, inDegree, outDegree);
            listVerticesDegree.add(vd);
        }
        return listVerticesDegree;
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
                if (v.getId() != otherV.getId()) {
                    if (d.getDistance(v, otherV) != null) {
                        weight = (int) Math.round((double) d.getDistance(v, otherV));
                        path = getClosestPath(v, otherV);
                        solutions.add(new ClosestPathSolution(v, otherV, weight, path));
                    }
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

    //<editor-fold defaultstate="collapsed" desc="Remove the vertices that have in degree equal than out degree">
    public void removeVerticesWithSameDegree() {
        int inDegree, outDegree;
        List<Integer> listVerticesRemoved = new ArrayList();
        for (Vertex v : graph.getVertices()) {            
            inDegree = graph.getInEdges(v).size();
            outDegree = graph.getOutEdges(v).size();            
            if (inDegree == outDegree) {
                listVerticesRemoved.add(v.getId());
            }
        }
        for (Integer i : listVerticesRemoved) {
            graph.removeVertex(getVertex(i));
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Return if the graph is eulerian or not">
    public boolean isEulerian() {
        int inDegree, outDegree;
        for (Vertex v : graph.getVertices()) {            
            inDegree = graph.getInEdges(v).size();
            outDegree = graph.getOutEdges(v).size();
            if (inDegree != outDegree) {
                return false;
            }
        }
        return true;
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
                if (graph.isSource(start, e)) { //find the edge in the edge set
                    Vertex dest = graph.getOpposite(start, e);
                    graph.removeEdge(e); //remove the edge to check if it is a bridge
                    
                    if (graph.getOutEdges(dest).size() == 0 && graph.getInEdges(start).size() > 0) { //first check if its a bridge
                        graph.addEdge(new Edge(e.getNode1(), e.getNode2(), e.getWeight()),
                                e.getNode1(), e.getNode2(), EdgeType.DIRECTED);
                    } else if (isGraphConected(e.getNode1(), e.getNode2())) { //check if the graph still conected after the removal of the edge
                        start = dest;
                        break;
                    } else { //if enter here the edge was a bridge, so put it back
                        graph.addEdge(new Edge(e.getNode1(), e.getNode2(), e.getWeight()),
                                e.getNode1(), e.getNode2(), EdgeType.DIRECTED);
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
            for (Vertex vAdj : graph.getSuccessors(v)) {
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
            if (graph.getInEdges(v).size() > 0) {
                numVerticesWithDegree++;
            } else if(graph.getOutEdges(v).size() > 0) {
                numVerticesWithDegree++;
            }
        }
        return numVerticesWithDegree;
    }

    private boolean isGraphConected(Vertex firstOption, Vertex secondOption) {        
        if (BFS(firstOption) == numVerticesWithDegree()) {
            return true;
        } else if(BFS(secondOption) == numVerticesWithDegree()) {
            return true;
        }
        return false;
    }
    //</editor-fold>        
    
    //<editor-fold defaultstate="collapsed" desc="Add edges to aquire a complete graph">
    public void addEdgesToCompleteGraph() { //weight of edges must be calculated by dijkstra's        
        ClosestPathSolution closestPathSolution = null;
        for (Vertex v : graph.getVertices()) {
            for (Vertex otherV : graph.getVertices()) {
                if (v.getId() != otherV.getId()) {
                    if (!graph.containsEdge(getEdge(v.getId(), otherV.getId()))) {
                        closestPathSolution = getClosestPathSolution(v.getId(), otherV.getId());
                        graph.addEdge(new Edge(getVertex(v.getId()), getVertex(otherV.getId()),
                                closestPathSolution.getWeight()), getVertex(v.getId()),
                                getVertex(otherV.getId()), EdgeType.DIRECTED);
                    }
                }
            }
        }
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
    
    private Edge getEdgeNotVisited(int n1, int n2) {
        Edge edge = null;
        for (Edge e : graph.getEdges()) {
            if (e.getNode1().getId() == n1 && e.getNode2().getId() == n2) {
                if (!e.isVisited()) {
                    edge = e;
                }
            }
        }        
        return edge;
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
    
    private void duplicateEdgeFromSolution(ClosestPathSolution cps) {
        Edge e = new Edge();
        for (int i = 0; i < cps.getShortestPath().size() - 1; i++) {
            e = graph.findEdge(cps.getShortestPath().get(i), cps.getShortestPath().get(i + 1));                                             
            graph.addEdge(new Edge(e.getNode1(), e.getNode2(), e.getWeight(), true),
                    e.getNode1(), e.getNode2(), EdgeType.DIRECTED);
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

    //<editor-fold defaultstate="collapsed" desc="Print the difference of in and out degree of the vertices">
    public void printDiffDegreeInOut() {
        for(Vertex v : graph.getVertices()) {
            System.out.print(v + ": ");
            System.out.println(graph.getInEdges(v).size() - graph.getOutEdges(v).size());
        }
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
        VertexDegree vertexDegree;
        String restrictions = "";
        for (Vertex v : graph.getVertices()) {
            vertexDegree = findVertexDegree(v);
            if (vertexDegree.getOutDegree() > vertexDegree.getInDegree()) {
                for (Vertex vInc : graph.getPredecessors(v)) {
                    if (firstTime) {
                        restrictions += addVariableToRestriction(vInc, v);
                        firstTime = false;
                    } else {
                        restrictions += " + " + addVariableToRestriction(vInc, v);
                    }
                }
                restrictions += " = " + (vertexDegree.getOutDegree() - vertexDegree.getInDegree()) + "\n";
                firstTime = true;
            } else {
                for (Vertex vSuc : graph.getSuccessors(v)) {
                    if (firstTime) {
                        restrictions += addVariableToRestriction(v, vSuc);
                        firstTime = false;
                    } else {
                        restrictions += " + " + addVariableToRestriction(v, vSuc);
                    }
                }
                restrictions += " = " + (vertexDegree.getInDegree() - vertexDegree.getOutDegree()) + "\n";
                firstTime = true;
            }
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
        IloLinearNumExpr ils;
        VertexDegree vertexDegree;
        for (Vertex v : graph.getVertices()) {
            ils = cplex.linearNumExpr();
            vertexDegree = findVertexDegree(v);
            if (vertexDegree.getOutDegree() > vertexDegree.getInDegree()) {
                for (Vertex vInc : graph.getPredecessors(v)) {
                    if (findVertexDegree(vInc).getInDegree() > findVertexDegree(vInc).getOutDegree()) {
                        for (IloNumVar var : variables) {
                            if (addVariableToRestriction(vInc, v).equals(var.getName())) {
                                ils.addTerm(var, 1.0);
                            }
                        }
                    }
                }                
                constraints.add(cplex.addEq(ils, (vertexDegree.getOutDegree() - vertexDegree.getInDegree())));
            } else {
                for (Vertex vSuc : graph.getSuccessors(v)) {
                    if (findVertexDegree(vSuc).getOutDegree() > findVertexDegree(vSuc).getInDegree()) {
                        for (IloNumVar var : variables) {
                            if (addVariableToRestriction(v, vSuc).equals(var.getName())) {
                                ils.addTerm(var, 1.0);
                            }
                        }
                    }
                    
                }                
                constraints.add(cplex.addEq(ils, (vertexDegree.getInDegree() - vertexDegree.getOutDegree())));
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Find a vertex in the list of vertices degree">
    private VertexDegree findVertexDegree(Vertex v) {
        VertexDegree vertexDegree = null;
        for (VertexDegree vd : verticesDegree) {
            if (vd.getVertex().getId().equals(v.getId())) {
                vertexDegree = vd;
            }
        }
        return vertexDegree;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    public DirectedWeightedGraph() {
    }
    //</editor-fold>

    public static void main(String args[]) throws IOException, CloneNotSupportedException, IloException, InterruptedException {
        DirectedWeightedGraph graph = new DirectedWeightedGraph();
        DirectedWeightedGraph differentDegreeVerticesGraph = new DirectedWeightedGraph();
        DirectedWeightedGraph visualizationGraph = new DirectedWeightedGraph();
        long init = System.currentTimeMillis();
        graph.readGraph("./samples/Directed/GRAPH.txt");
        if (!graph.isEulerian()) {
            graph.setClosestPathSolutions(graph.createListOfClosestPathSolutions());
            graph.setVerticesDegree(graph.createListOfVerticesDegree());

            differentDegreeVerticesGraph = (DirectedWeightedGraph) graph.clone();
            differentDegreeVerticesGraph.removeVerticesWithSameDegree();
            differentDegreeVerticesGraph.setClosestPathSolutions(graph.getClosestPathSolutions());
            differentDegreeVerticesGraph.addEdgesToCompleteGraph();
            differentDegreeVerticesGraph.setVerticesDegree(graph.getVerticesDegree());            
            differentDegreeVerticesGraph.generateSolveMathematicalModelCplex();
            
            graph.setResult(differentDegreeVerticesGraph.getResult()); //put the result inside of the original graph            
            graph.duplicateEdges(); //duplicate the edges to get an eulerian graph            
        }
        visualizationGraph = (DirectedWeightedGraph) graph.clone();
        visualizationGraph.setEulerianPath(graph.findEulerianPathFrom(1));
        long end = System.currentTimeMillis();
        visualizationGraph.displayGraph();
        visualizationGraph.createReport(end-init);
        visualizationGraph.openReport();
    }
    
    //<editor-fold defaultstate="collapsed" desc="Gets & Sets">
    public List<ClosestPathSolution> getClosestPathSolutions() {
        return closestPathSolutions;
    }

    public List<Vertex> getEulerianPath() {
        return eulerianPath;
    }

    public CplexResult getResult() {
        return result;
    }

    public void setResult(CplexResult result) {
        this.result = result;
    }

    public void setClosestPathSolutions(List<ClosestPathSolution> closestPathSolutions) {
        this.closestPathSolutions = closestPathSolutions;
    }

    public List<VertexDegree> getVerticesDegree() {
        return verticesDegree;
    }

    public void setEulerianPath(List<Vertex> eulerianPath) {
        this.eulerianPath = eulerianPath;
    }

    public void setVerticesDegree(List<VertexDegree> verticesDegree) {
        this.verticesDegree = verticesDegree;
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
}
