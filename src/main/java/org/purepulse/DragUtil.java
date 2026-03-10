package org.purepulse;

import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * @author aidan.liu
 * @version 1.0
 * @since 2026/3/10 14:46
 */
public class DragUtil {
  public static void enableDrag(Stage stage, Node node){

    final double[] offset = new double[2];

    node.setOnMousePressed(e -> {
      offset[0] = e.getSceneX();
      offset[1] = e.getSceneY();
    });

    node.setOnMouseDragged(e -> {
      stage.setX(e.getScreenX() - offset[0]);
      stage.setY(e.getScreenY() - offset[1]);
    });
  }
}