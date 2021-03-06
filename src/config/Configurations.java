package config;

import javafx.scene.paint.Color;

public class Configurations {

	public static int VERTEX_COUNT;
	public static int INITIAL_VERTEX;
	public static int WINDOW_HEIGHT = 600; 
	public static int WINDOW_WIDHT = 400;
	public static Color EDGES_COLOR;
	public static Color EDGES_ANIMATION_COLOR;
	public static Color VERTICES_COLOR;
	public static float EDGES_THICKNESS;
	public static String BOLD_STYLE = "-fx-font-weight: bold;";
	public static String WHITE_BACKGROUND = "-fx-background: #FFFFFF;";
	public static float MIN_THICKNESS = 1F;
	public static float MAX_THICKNESS = 3F;
	public static String LAYOUT;

	public static void setVertexCount(int vertexCount) {
		VERTEX_COUNT = vertexCount;
	}
}
