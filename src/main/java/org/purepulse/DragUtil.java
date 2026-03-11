package org.purepulse;

import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Utility class that enables dragging functionality for an undecorated
 * JavaFX window.
 *
 * <p>This helper is commonly used when a {@link javafx.stage.Stage} is created
 * without the default system window decorations (for example using
 * {@code StageStyle.UNDECORATED}). In such cases the user cannot move the
 * window using the system title bar, so this utility provides a custom
 * drag behavior.</p>
 *
 * <p>The implementation works by:</p>
 * <ul>
 *     <li>Capturing the mouse position when the user presses the mouse button</li>
 *     <li>Tracking the mouse movement while dragging</li>
 *     <li>Updating the window position based on the drag offset</li>
 * </ul>
 *
 * <p>This method is typically applied to the root container of a JavaFX UI,
 * allowing the entire floating panel to be draggable.</p>
 * <p>
 * Example usage:
 *
 * <pre>
 * DragUtil.enableDrag(stage, rootNode);
 * </pre>
 *
 * @author aidan.liu
 * @since 2026-03-10
 */
public class DragUtil {

  /**
   * Enables drag-to-move behavior for the given JavaFX window.
   *
   * <p>When the user presses and drags the mouse over the specified node,
   * the associated {@link Stage} will move accordingly on the screen.</p>
   *
   * @param stage the JavaFX window to be moved
   * @param node  the UI node that listens for mouse drag events
   */
  public static void enableDrag(Stage stage, Node node) {

    final double[] offset = new double[2];

    // Record mouse position when pressed
    node.setOnMousePressed(e -> {
      offset[0] = e.getSceneX();
      offset[1] = e.getSceneY();
    });

    // Move window when mouse is dragged
    node.setOnMouseDragged(e -> {
      stage.setX(e.getScreenX() - offset[0]);
      stage.setY(e.getScreenY() - offset[1]);
    });
  }
}