package edu.wpi.first.pathweaver;

import edu.wpi.first.pathweaver.global.CurrentSelections;
import edu.wpi.first.pathweaver.global.DragHandler;
import edu.wpi.first.pathweaver.path.Path;
import edu.wpi.first.pathweaver.path.wpilib.WpilibPath;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.TreeItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

@SuppressWarnings("PMD.UnusedPrivateMethod")
public class FieldDisplayController {
    private static final PseudoClass SELECTED_CLASS = PseudoClass.getPseudoClass("selected");

    @FXML
    public Group group;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private Pane drawPane;
    @FXML
    private Pane topPane;
    @FXML
    private Group pathGroup;

    private Field field;

    private final ObservableList<Path> pathList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        field = ProjectPreferences.getInstance().getField();
        Image image = field.getImage();
        backgroundImage.setImage(image);
        Tooltip tooltip = new Tooltip();
        Tooltip.install(topPane, tooltip);
        tooltip.setShowDelay(Duration.seconds(0.0));
        tooltip.setHideDelay(Duration.seconds(999999.0));
        drawPane.setOnMouseMoved(e -> {
                tooltip.setText("X: " + roundToString(e.getX()) + " / Y: " + roundToString(e.getY()));
                tooltip.show(topPane, e.getSceneX(), e.getSceneY());
                });
        Scale scale = new Scale();
        scale.xProperty().bind(Bindings.createDoubleBinding(() ->
                        Math.min(topPane.getWidth() / image.getWidth(), topPane.getHeight() / image.getHeight()),
                topPane.widthProperty(), topPane.heightProperty()));
        scale.yProperty().bind(Bindings.createDoubleBinding(() ->
                        Math.min(topPane.getWidth() / image.getWidth(), topPane.getHeight() / image.getHeight()),
                topPane.widthProperty(), topPane.heightProperty()));

        group.getTransforms().add(scale);

        setupDrawPaneSizing();
        this.drawPane.setOnMouseClicked(e -> {
            if (CurrentSelections.getCurWaypoint() != null) {
                CurrentSelections.getCurWaypoint().getIcon().pseudoClassStateChanged(SELECTED_CLASS, false);
                CurrentSelections.setCurWaypoint(null);
            }

            if(CurrentSelections.getCurPath() != null) {
                CurrentSelections.getCurPath().update();
            }
        });

        new DragHandler(this, drawPane); // Handler doesn't need to be kept around by this, so just do setup

        setupPathListener();
    }

    private void setupPathListener() {
        pathList.addListener((ListChangeListener<Path>) change -> {
            while (change.next()) {
                for (Path path : change.getAddedSubList()) {
                    pathGroup.getChildren().add(path.getMainGroup());
                }
                for (Path path : change.getRemoved()) {
                    pathGroup.getChildren().remove(path.getMainGroup());
                }
            }
        });
    }

    @FXML
    private void keyPressed(KeyEvent event) {
        Path curPath = CurrentSelections.getCurPath();
        KeyCombination save = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
            curPath.removeWaypoint(CurrentSelections.getCurWaypoint());
        } else if (save.match(event)) {
            SaveManager.getInstance().saveChange(curPath);
        }
    }

    /**
     * Adds a path to the controller.
     *
     * @param fileLocations The folder containing the path file
     * @param newValue      The TreeItem holding the name of this path
     * @return The new path, or if duplicate, the old path matching the file name
     */
    public Path addPath(String fileLocations, TreeItem<String> newValue) {
        String fileName = newValue.getValue();
        for (Path path : pathList) {
            if (fileName.equals(path.getPathName())) {
                return path;
            }
        }
        Path newPath = PathIOUtil.importPath(fileLocations, fileName);
        if (newPath == null) {
            newPath = new WpilibPath(fileName);
            SaveManager.getInstance().saveChange(newPath);
        }
        CurrentSelections.curPathProperty().set(newPath);

        pathList.add(newPath);
        return newPath;
    }

    /**
     * Remove all paths from Controller.
     */
    public void removeAllPath() {
        pathList.clear();
    }

    private void setupDrawPaneSizing() {
        drawPane.setPrefHeight(field.getRealLength().getValue().doubleValue());
        drawPane.setPrefWidth(field.getRealWidth().getValue().doubleValue());
        drawPane.setLayoutX(field.getCoord().getX());
        drawPane.setLayoutY(field.getCoord().getY());
        drawPane.setScaleX(field.getScale());
        drawPane.setScaleY(field.getScale());
    }

    /**
     * Flip the current path.
     *
     * @param horizontal True if horizontal flip, false if vertical
     */
    public void flip(boolean horizontal) {
        Path curPath = CurrentSelections.getCurPath();
        curPath.flip(horizontal, drawPane);
        SaveManager.getInstance().addChange(curPath);
    }

    /**
     * Duplicates the selected path.
     *
     * @param pathDirectory The directory to save the new path to.
     * @return The new path.
     */
    public Path duplicate(String pathDirectory) {
        Path path = CurrentSelections.getCurPath();
        String fileName = MainIOUtil.getValidFileName(pathDirectory, path.getPathNameNoExtension(), ".path");
        return path.duplicate(fileName);
    }

    /**
     * Checks if the given x, y coordinates are within the valid area of the drawpane.
     *
     * @param x X coordinate, following {@link Waypoint} convention
     * @param y Y coordinate following {@link Waypoint} convention
     * @return True if X, Y is within the bounds of the drawpane.
     */
    public boolean checkBounds(double x, double y) {
        //Convert waypoint convention to JavaFX
        return drawPane.getLayoutBounds().contains(x, -y);
    }

    private String roundToString(Double number) {
        number = Math.round(number * 100.0) / 100.0;
        return String.valueOf(number);
    }
}
