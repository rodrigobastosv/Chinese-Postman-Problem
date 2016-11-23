package gui;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import chinesepostmanproblem.DirectedWeightedGraph;
import chinesepostmanproblem.MixedWeightedGraph;
import chinesepostmanproblem.UndirectedWeightedGraph;
import config.Configurations;
import ilog.concert.IloException;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class StartWindow extends Application {
	File file;
	static long t0, t1;
	
	/** GRAPHS **/
	UndirectedWeightedGraph undirectedGraph = new UndirectedWeightedGraph();
	DirectedWeightedGraph directedGraph = new DirectedWeightedGraph();
	MixedWeightedGraph mixedGraph = new MixedWeightedGraph();
	UndirectedWeightedGraph reportUndirectedGraph = new UndirectedWeightedGraph();
	DirectedWeightedGraph reportDirectedGraph = new DirectedWeightedGraph();
	MixedWeightedGraph reportMixedGraph = new MixedWeightedGraph();

	/** BUTTONS **/
	Button btnStart = new Button("Começar");
	Button btnGraphFile = new Button("Arquivo");
	Button btnReport = new Button("Relatório");
	Button btnInitialGraph = new Button("Grafo Inicial");
	Button btnEulerianGraph = new Button("Grafo Euleriano");

	/** LABELS **/
	Label lbPCCType = new Label("Tipo de PCC: ");
	Label lbEdgeColor = new Label("Cor das Arestas: ");
	Label lbEdgeAnimationColor = new Label("Cor das Arestas (Animação): ");
	Label lbGraphFile = new Label("Arquivo do Grafo: ");
	Label lbVertexColor = new Label("Cor dos Vértices: ");
	Label lbEdgeThickness = new Label("Espessura das Arestas: ");
	Label lbInitialVertex = new Label("Vértice Inicial: ");
	Label lbInitialVertexValue = new Label();

	/** SCROLLS **/ 
	ScrollBar edgeThicknessScroll = new ScrollBar();
	
	/** COMBOS **/
	ComboBox cbPCCType = new ComboBox();
	ComboBox cbInitialVertex = new ComboBox();

	/** COLORPICKERS **/ 
	ColorPicker edgesColor = new ColorPicker();
	ColorPicker edgesAnimationColor = new ColorPicker();
	ColorPicker verticesColor = new ColorPicker();
	
	public static void main(String args[]) {
		launch(args);
	}
	
	private void populateInitialVertexCb(File file) {
		switch (cbPCCType.getValue().toString()) {
		case "Não Dirigido":
			try {
				UndirectedWeightedGraph uGraph = new UndirectedWeightedGraph();
				uGraph.readGraph(file.getAbsolutePath());
				Configurations.setVertexCount(uGraph.getGraph().getVertexCount());
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case "Dirigido":
			try {
				DirectedWeightedGraph dGraph = new DirectedWeightedGraph();
				dGraph.readGraph(file.getAbsolutePath());
				Configurations.setVertexCount(dGraph.getGraph().getVertexCount());
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case "Misto":
			try {
				MixedWeightedGraph mGraph = new MixedWeightedGraph();
				mGraph.readGraph(file.getAbsolutePath());
				Configurations.setVertexCount(mGraph.getGraph().getVertexCount());
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
	}
	
	private void configureButtons(Stage stage) {
		btnGraphFile.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Escolha o arquivo com o grafo");
				file = fileChooser.showOpenDialog(stage);
				btnGraphFile.setText(file.getName().length() > 30 ? file.getName().substring(0, 30) : file.getName());
				btnInitialGraph.setDisable(false);
				btnEulerianGraph.setDisable(false);
				configureComboInitialVertex(file);
			}
		});
		
		btnStart.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (file == null) {
					Alert alert = new Alert(AlertType.ERROR, "Escolha um arquivo com um grafo ", ButtonType.OK);
					alert.showAndWait();
					return;
				}
				Configurations.EDGES_COLOR = edgesColor.getValue();
				Configurations.EDGES_ANIMATION_COLOR = edgesAnimationColor.getValue();
				Configurations.VERTICES_COLOR = verticesColor.getValue();
				Configurations.EDGES_THICKNESS = (float) edgeThicknessScroll.getValue();
				Configurations.INITIAL_VERTEX = Integer.parseInt(cbInitialVertex.getValue().toString());
				switch (cbPCCType.getValue().toString()) {
				case "Não Dirigido":
					try {
						runUndirectedCPPSample(file);
					} catch (IOException | CloneNotSupportedException | IloException | InterruptedException e) {
						e.printStackTrace();
					}
					break;
				case "Dirigido":
					try {
						runDirectedCPPSample(file);
					} catch (IOException | CloneNotSupportedException | IloException | InterruptedException e) {
						e.printStackTrace();
					}
					break;
				case "Misto":
					try {
						runMixedCPPSample(file);
					} catch (IOException | CloneNotSupportedException | IloException | InterruptedException e) {
						e.printStackTrace();
					}
					break;
				default:
					Alert alert = new Alert(AlertType.ERROR, "Escolha um tipo de PCC válido ", ButtonType.OK);
					alert.showAndWait();
					return;
				}
				btnReport.setDisable(false);
			}
		});
		
		btnReport.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				switch (cbPCCType.getValue().toString()) {
				case "Não Dirigido":
					try {
						UndirectedWeightedGraph.visualizationOfReport(reportUndirectedGraph, t0, t1);
					} catch (CloneNotSupportedException | IOException e1) {
						e1.printStackTrace();
					}
					break;
				case "Dirigido":
					try {
						DirectedWeightedGraph.visualizationOfReport(reportDirectedGraph, t0, t1);
					} catch (CloneNotSupportedException | IOException e) {
						e.printStackTrace();
					}
					break;
				case "Misto":
					try {
						MixedWeightedGraph.visualizationOfReport(reportMixedGraph, t0, t1);
					} catch (CloneNotSupportedException | IOException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		});
		
		btnInitialGraph.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Configurations.EDGES_COLOR = edgesColor.getValue();
				Configurations.EDGES_ANIMATION_COLOR = edgesAnimationColor.getValue();
				Configurations.VERTICES_COLOR = verticesColor.getValue();
				Configurations.EDGES_THICKNESS = (float) edgeThicknessScroll.getValue();
				switch (cbPCCType.getValue().toString()) {
				case "Não Dirigido":
					try {
						undirectedGraph.readGraph(file.getAbsolutePath());
						Configurations.setVertexCount(undirectedGraph.getGraph().getVertexCount());
						undirectedGraph.imageOfGraph();
					} catch (IOException  | InterruptedException e) {
						e.printStackTrace();
					}
					break;
				case "Dirigido":
					try {
						directedGraph.readGraph(file.getAbsolutePath());
						Configurations.setVertexCount(directedGraph.getGraph().getVertexCount());
						directedGraph.imageOfGraph();
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
					break;
				case "Misto":
					try {
						mixedGraph.readGraph(file.getAbsolutePath());
						Configurations.setVertexCount(mixedGraph.getGraph().getVertexCount());
						mixedGraph.imageOfGraph();
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		});

		btnEulerianGraph.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Configurations.EDGES_COLOR = edgesColor.getValue();
				Configurations.EDGES_ANIMATION_COLOR = edgesAnimationColor.getValue();
				Configurations.VERTICES_COLOR = verticesColor.getValue();
				Configurations.EDGES_THICKNESS = (float) edgeThicknessScroll.getValue();
				switch (cbPCCType.getValue().toString()) {
				case "Não Dirigido":
					try {
						UndirectedWeightedGraph graph = new UndirectedWeightedGraph();
				        graph.readGraph(file.getAbsolutePath());
				        if (!graph.isEulerian()) {
				            graph.setClosestPathSolutions(graph.createListOfClosestPathSolutions());
				            if (graph.isUseEdmondsAlgorithm()) {
				                graph.findEulerianGraphUsingEdmonds(graph);
				            } else {
				                graph.findEulerianGraphUsingLinearProgramming(graph);
				            }
				        }
				        graph.imageOfGraph();
					} catch (IOException  | CloneNotSupportedException | IloException | InterruptedException e) {
						e.printStackTrace();
					}
					break;
				case "Dirigido":
					DirectedWeightedGraph graph = new DirectedWeightedGraph();
			        DirectedWeightedGraph differentDegreeVerticesGraph = new DirectedWeightedGraph();
			        try {
						graph.readGraph(file.getAbsolutePath());
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
				        graph.imageOfGraph();
					} catch (IOException | CloneNotSupportedException | IloException | InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break;
				case "Misto":
			        MixedWeightedGraph graph1 = new MixedWeightedGraph();
			        MixedWeightedGraph differentDegreeVerticesGraph1 = new MixedWeightedGraph();
			        try {
						graph1.readGraph(file.getAbsolutePath());
				        graph1.transformInOriented();
				        if (!graph1.isEulerian()) {
				            graph1.setClosestPathSolutions(graph1.createListOfClosestPathSolutions());
				            graph1.setVerticesDegree(graph1.createListOfVerticesDegree());

				            differentDegreeVerticesGraph1 = (MixedWeightedGraph) graph1.clone();
				            differentDegreeVerticesGraph1.removeVerticesWithSameDegree();
				            differentDegreeVerticesGraph1.setClosestPathSolutions(graph1.getClosestPathSolutions());
				            differentDegreeVerticesGraph1.addEdgesToCompleteGraph();
				            differentDegreeVerticesGraph1.setVerticesDegree(graph1.getVerticesDegree());            
				            differentDegreeVerticesGraph1.generateSolveMathematicalModelCplex();
				            
				            graph1.setResult(differentDegreeVerticesGraph1.getResult()); //put the result inside of the original graph            
				            graph1.duplicateEdges(); //duplicate the edges to get an eulerian graph            
				        }
				        graph1.imageOfGraph();
					} catch (IOException | CloneNotSupportedException | IloException | InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
		});

		
		btnReport.setDisable(true);
		btnInitialGraph.setDisable(true);
		btnEulerianGraph.setDisable(true);
		btnReport.setStyle(Configurations.BOLD_STYLE);
		btnStart.setStyle(Configurations.BOLD_STYLE);
		btnInitialGraph.setStyle(Configurations.BOLD_STYLE);
		btnEulerianGraph.setStyle(Configurations.BOLD_STYLE);
	}
	
	private void configureLabels() {
		lbPCCType.setStyle(Configurations.BOLD_STYLE);
		lbEdgeColor.setStyle(Configurations.BOLD_STYLE);
		lbEdgeAnimationColor.setStyle(Configurations.BOLD_STYLE);
		lbVertexColor.setStyle(Configurations.BOLD_STYLE);
		lbGraphFile.setStyle(Configurations.BOLD_STYLE);
		lbEdgeThickness.setStyle(Configurations.BOLD_STYLE);
		lbInitialVertex.setStyle(Configurations.BOLD_STYLE);
	}
	
	private GridPane configureGrid() {
		/** GRID **/
		GridPane gridPane = new GridPane();
		gridPane.setStyle(Configurations.WHITE_BACKGROUND);
		gridPane.setHgap(20);
		gridPane.setVgap(20);
		gridPane.setTranslateX(20);
		
		gridPane.add(lbPCCType, 0, 0);
		gridPane.add(cbPCCType, 1, 0);
		
		gridPane.add(lbGraphFile, 0, 1);
		gridPane.add(btnGraphFile, 1, 1);
		
		gridPane.add(lbEdgeColor, 0, 2);
		gridPane.add(edgesColor, 1, 2);
		
		gridPane.add(lbEdgeAnimationColor, 0, 3);
		gridPane.add(edgesAnimationColor, 1, 3);
		
		gridPane.add(lbEdgeThickness, 0, 4);
		gridPane.add(edgeThicknessScroll, 1, 4);
		
		gridPane.add(lbVertexColor, 0, 5);
		gridPane.add(verticesColor, 1, 5);
		
		gridPane.add(lbInitialVertex, 0, 6);
		gridPane.add(cbInitialVertex, 1, 6);
		
		gridPane.add(btnReport, 0, 8);
		gridPane.add(btnStart, 0, 7);
		gridPane.add(btnInitialGraph, 1, 7);
		gridPane.add(btnEulerianGraph, 1, 8);
		
		return gridPane;
	}
	
	private void configureScrolls() {
		edgeThicknessScroll.setMin(Configurations.MIN_THICKNESS);
		edgeThicknessScroll.setMax(Configurations.MAX_THICKNESS);
	}
	
	private void configureComboInitialVertex(File file) {
		populateInitialVertexCb(file);
		
		String[] itensArray = new String[Configurations.VERTEX_COUNT];
		Integer j;
		for (Integer i=0 ; i<Configurations.VERTEX_COUNT ; i++) {
			j = i+1;
			itensArray[i] = j.toString();
		}
		ObservableList<String> vertices = FXCollections.observableArrayList(itensArray);
		cbInitialVertex.setItems(vertices);
		cbInitialVertex.setValue(1);
	}
	
	private void configureCombos() {
		ObservableList<String> CPPtypes = FXCollections.observableArrayList(
		        "Não Dirigido",
		        "Dirigido",
		        "Misto");
		cbPCCType.setItems(CPPtypes);
		cbPCCType.setValue("Não Dirigido");
	}
	
	private void configureColors() {
		edgesColor.setValue(Color.RED);
		edgesAnimationColor.setValue(Color.GREEN);
		verticesColor.setValue(Color.RED);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Problema do Carteiro Chinês");
		
		configureLabels();
		configureButtons(primaryStage);
		configureCombos();
		configureScrolls();
		configureColors();
		
		Scene scene = new Scene(configureGrid(), Configurations.WINDOW_WIDHT, Configurations.WINDOW_HEIGHT);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public void runUndirectedCPPSample(File file) throws FileNotFoundException, IOException, CloneNotSupportedException, IloException, InterruptedException {
		UndirectedWeightedGraph graph = new UndirectedWeightedGraph();
		t0 = System.currentTimeMillis();
        graph.readGraph(file.getAbsolutePath());
        if (!graph.isEulerian()) {
            graph.setClosestPathSolutions(graph.createListOfClosestPathSolutions());
            if (graph.isUseEdmondsAlgorithm()) {
                graph.findEulerianGraphUsingEdmonds(graph);
            } else {
                graph.findEulerianGraphUsingLinearProgramming(graph);
            }
        }
        t1 = System.currentTimeMillis();
        reportUndirectedGraph = graph.visualizationOfGraph(graph);
	}
	
	public void runDirectedCPPSample(File file) throws FileNotFoundException, IOException, CloneNotSupportedException, IloException, InterruptedException {
		DirectedWeightedGraph graph = new DirectedWeightedGraph();
        DirectedWeightedGraph differentDegreeVerticesGraph = new DirectedWeightedGraph();
        t0 = System.currentTimeMillis();
        graph.readGraph(file.getAbsolutePath());
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
        t1 = System.currentTimeMillis();
        reportDirectedGraph = graph.visualizationOfGraph(graph);
	}

	public void runMixedCPPSample(File file) throws FileNotFoundException, IOException, CloneNotSupportedException, IloException, InterruptedException {
        MixedWeightedGraph graph = new MixedWeightedGraph();
        MixedWeightedGraph differentDegreeVerticesGraph = new MixedWeightedGraph();
        t0 = System.currentTimeMillis();
        graph.readGraph(file.getAbsolutePath());
        graph.transformInOriented();
        if (!graph.isEulerian()) {
            graph.setClosestPathSolutions(graph.createListOfClosestPathSolutions());
            graph.setVerticesDegree(graph.createListOfVerticesDegree());

            differentDegreeVerticesGraph = (MixedWeightedGraph) graph.clone();
            differentDegreeVerticesGraph.removeVerticesWithSameDegree();
            differentDegreeVerticesGraph.setClosestPathSolutions(graph.getClosestPathSolutions());
            differentDegreeVerticesGraph.addEdgesToCompleteGraph();
            differentDegreeVerticesGraph.setVerticesDegree(graph.getVerticesDegree());            
            differentDegreeVerticesGraph.generateSolveMathematicalModelCplex();
            
            graph.setResult(differentDegreeVerticesGraph.getResult()); //put the result inside of the original graph            
            graph.duplicateEdges(); //duplicate the edges to get an eulerian graph            
        }
        t1 = System.currentTimeMillis();
        reportMixedGraph = graph.visualizationOfGraph(graph);
	}
}
