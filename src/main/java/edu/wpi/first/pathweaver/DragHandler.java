package edu.wpi.first.pathweaver;

import javafx.geometry.Point2D;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;

public class DragHandler {

  private final PathDisplayController controller;
  private final Pane drawPane;

  private static Spline currentSpline = new NullSpline();

  private boolean isShiftDown = false;
  private boolean splineDragStarted = false;

  /**
   * Creates the DragHandler, which sets up and manages all drag interactions for the given PathDisplayController.
   *
   * @param parent   The PathDisplayController that this DragHandler manages
   * @param drawPane The PathDisplayController's Pane.
   */
  public DragHandler(PathDisplayController parent, Pane drawPane) {
    this.controller = parent;
    this.drawPane = drawPane;
    this.setupDrag();
  }

  private void finishDrag() {
    SaveManager.getInstance().addChange(Waypoint.currentWaypoint.getPath());
    splineDragStarted = false;
  }

  private void handleDrag(DragEvent event) {
    Dragboard dragboard = event.getDragboard();
    Waypoint wp = Waypoint.currentWaypoint;
    event.acceptTransferModes(TransferMode.MOVE);
    if (dragboard.hasContent(DataFormats.WAYPOINT)) {
      if (isShiftDown) {
        handlePathMoveDrag(event, wp);
      } else {
        handleWaypointDrag(event, wp);
      }
    } else if (dragboard.hasContent(DataFormats.CONTROL_VECTOR)) {
      handleVectorDrag(event, wp);
    } else if (dragboard.hasContent(DataFormats.SPLINE)) {
      handleSplineDrag(event, wp);
    }
    event.consume();
  }

  private void setupDrag() {
    drawPane.setOnDragDone(event -> finishDrag());
    drawPane.setOnDragOver(this::handleDrag);
    drawPane.setOnDragDetected(event -> {
      isShiftDown = event.isShiftDown();
    });
  }

  private void handleWaypointDrag(DragEvent event, Waypoint wp) {
    if (controller.checkBounds(event.getX(), event.getY())) {
      wp.setX(event.getX());
      wp.setY(event.getY());
      wp.getPath().getWaypoints().forEach(Waypoint::update);
    }
    controller.selectWaypoint(wp, false);
  }

  private void handleVectorDrag(DragEvent event, Waypoint wp) {
    Point2D pt = new Point2D(event.getX(), event.getY());
    wp.setTangent(pt.subtract(wp.getX(), wp.getY()));
    wp.lockTangentProperty().set(true);
    wp.getPath().getWaypoints().forEach(Waypoint::update);
  }

  private void handleSplineDrag(DragEvent event, Waypoint wp) {
    if (splineDragStarted) {
      handleWaypointDrag(event, wp);
    } else {
      Spline current = currentSpline;
      current.addPathWaypoint(controller);
      current = new NullSpline();
      splineDragStarted = true;
    }
  }

  private void handlePathMoveDrag(DragEvent event, Waypoint wp) {
    double offsetX = event.getX() - wp.getX();
    double offsetY = event.getY() - wp.getY();

    // Make sure all waypoints will be within the bounds
    for (Waypoint point : wp.getPath().getWaypoints()) {
      double wpNewX = point.getX() + offsetX;
      double wpNewY = point.getY() + offsetY;
      if (!controller.checkBounds(wpNewX, wpNewY)) {
        return;
      }
    }

    // Apply new positions
    for (Waypoint point : wp.getPath().getWaypoints()) {
      double wpNewX = point.getX() + offsetX;
      double wpNewY = point.getY() + offsetY;
      point.setX(wpNewX);
      point.setY(wpNewY);
    }
  }

  /**
   * Set the spline that is currently being dragged.
   * @param currentSpline Spline that is being dragged.
   */
  public static void setCurrentSpline(Spline currentSpline) {
    DragHandler.currentSpline = currentSpline;
  }
}
