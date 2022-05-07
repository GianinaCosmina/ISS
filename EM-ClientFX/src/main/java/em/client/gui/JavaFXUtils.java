package em.client.gui;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class JavaFXUtils {
    private static double xOffset = 0;
    private static double yOffset = 0;

    public static void makeWindowDraggable(Scene scene, Stage stage) {
        scene.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        scene.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    public static void setScenePosition(Scene scene, Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double x = (bounds.getWidth() - scene.getWidth()) / 2;
        double y = (bounds.getHeight() - scene.getHeight()) / 2;
        stage.setX(x);
        stage.setY(y);
    }
}
