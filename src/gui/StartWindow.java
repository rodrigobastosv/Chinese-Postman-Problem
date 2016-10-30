package gui;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import chinesepostmanproblem.DirectedWeightedGraph;
import chinesepostmanproblem.MixedWeightedGraph;
import chinesepostmanproblem.UndirectedWeightedGraph;
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
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class StartWindow extends Application {
	File file;
	UndirectedWeightedGraph undirected = new UndirectedWeightedGraph();
	DirectedWeightedGraph directed = new DirectedWeightedGraph();
	MixedWeightedGraph mixed = new MixedWeightedGraph();
	static long t0, t1;

	public static void main(String args[]) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Problema do Carteiro Chinês");
		
		Button btnStart = new Button("Começar");
		
		Label lbFilePath = new Label();
		Button btnGraph = new Button("Grafo");
		btnGraph.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Escolha o arquivo com o grafo");
				file = fileChooser.showOpenDialog(primaryStage);
				int lenghtToFit = file.getName().length()<15 ? file.getName().length() : 15;
				lbFilePath.setText(file.getName().substring(0, lenghtToFit));
			}
		});
		
		Label lbPCCType = new Label("Tipo de PCC: ");
		Label lbEdgeColor = new Label("Cor das Arestas: ");
		
		ObservableList<String> types = FXCollections.observableArrayList(
			        "Não Dirigido",
			        "Dirigido",
			        "Misto");
		ComboBox cbPCCType = new ComboBox(types);
		cbPCCType.setValue("Não Dirigido");
		
		ColorPicker edgesColor = new ColorPicker();
		edgesColor.setValue(javafx.scene.paint.Color.GREEN);
		
		GridPane gridPane = new GridPane();
		gridPane.setStyle("-fx-background: #FFFFFF;");
		gridPane.setHgap(20);
		gridPane.setVgap(20);
		gridPane.setTranslateX(20);
		
		gridPane.add(cbPCCType, 1, 0);
		gridPane.add(lbPCCType, 0, 0);
		
		gridPane.add(lbFilePath, 1, 1);
		gridPane.add(btnGraph, 0, 1);
		
		gridPane.add(lbEdgeColor, 0, 2);
		gridPane.add(edgesColor, 1, 2);
		
		gridPane.add(btnStart, 3, 3);
		
		btnStart.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (file == null) {
					Alert alert = new Alert(AlertType.ERROR, "Escolha um arquivo com um grafo ", ButtonType.OK);
					alert.showAndWait();
					return;
				}
				primaryStage.hide();
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
				System.out.println(edgesColor.getValue());
			}
		});
		
		Scene scene = new Scene(gridPane, 400, 200);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public void runUndirectedCPPSample(File file) throws FileNotFoundException, IOException, CloneNotSupportedException, IloException, InterruptedException {
		UndirectedWeightedGraph graph = new UndirectedWeightedGraph();
        UndirectedWeightedGraph reportGraph = new UndirectedWeightedGraph();        
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
        reportGraph = graph.visualizationOfGraph(graph);
        graph.visualizationOfReport(reportGraph, t0, t1);
	}
	
	public void runDirectedCPPSample(File file) throws FileNotFoundException, IOException, CloneNotSupportedException, IloException, InterruptedException {
		DirectedWeightedGraph graph = new DirectedWeightedGraph();
        DirectedWeightedGraph differentDegreeVerticesGraph = new DirectedWeightedGraph();
        DirectedWeightedGraph visualizationGraph = new DirectedWeightedGraph();
        long init = System.currentTimeMillis();
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
        visualizationGraph = (DirectedWeightedGraph) graph.clone();
        visualizationGraph.setEulerianPath(graph.findEulerianPathFrom(1));
        long end = System.currentTimeMillis();
        visualizationGraph.displayGraph();
        visualizationGraph.createReport(end-init);
        visualizationGraph.openReport();
	}

	public void runMixedCPPSample(File file) throws FileNotFoundException, IOException, CloneNotSupportedException, IloException, InterruptedException {
        MixedWeightedGraph graph = new MixedWeightedGraph();
        MixedWeightedGraph differentDegreeVerticesGraph = new MixedWeightedGraph();
        MixedWeightedGraph visualizationGraph = new MixedWeightedGraph();
        long init = System.currentTimeMillis();
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
        visualizationGraph = (MixedWeightedGraph) graph.clone();
        visualizationGraph.setEulerianPath(graph.findEulerianPathFrom(1));
        long end = System.currentTimeMillis();
        visualizationGraph.displayGraph();
        visualizationGraph.createReport(end-init);
        visualizationGraph.openReport(); 
	}

}
