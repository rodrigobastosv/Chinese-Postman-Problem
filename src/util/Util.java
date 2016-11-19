package util;

import java.awt.Color;

public class Util {

	public static Color getNormalizedColor(javafx.scene.paint.Color color) {
    	float RED = (float) color.getRed();
    	float GREEN = (float) color.getGreen();
    	float BLUE = (float) color.getBlue();

    	return new Color(RED, GREEN, BLUE);
	}
}
