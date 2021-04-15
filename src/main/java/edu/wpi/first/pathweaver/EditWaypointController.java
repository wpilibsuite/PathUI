package edu.wpi.first.pathweaver;

import java.util.List;

import edu.wpi.first.pathweaver.global.CurrentSelections;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.util.converter.NumberStringConverter;

@SuppressWarnings("PMD.UnusedPrivateMethod")
public class EditWaypointController {
  @FXML
  private ListView listView;
  @FXML
  private TextField xPosition;
  @FXML
  private TextField yPosition;
  @FXML
  private TextField tangentX;
  @FXML
  private TextField tangentY;
  @FXML
  private CheckBox lockedTangent;
  @FXML
  private CheckBox reverseSpline;
  @FXML
  private TextField pointName;
  @FXML
  private Button javaExport;
  @FXML
  private Button cExport;

  private List<Control> controls;
  private ChangeListener<String> nameListener;

  final Clipboard clipboard = Clipboard.getSystemClipboard();
  final ClipboardContent content = new ClipboardContent();

  @FXML
  private void initialize() {
    controls = List.of(listView, xPosition, yPosition, tangentX, tangentY, lockedTangent, pointName, reverseSpline, javaExport, cExport);
    controls.forEach(control -> control.setDisable(true));
    List<TextField> textFields = List.of(xPosition, yPosition, tangentX, tangentY);
    textFields.forEach(textField -> textField.setTextFormatter(FxUtils.onlyDoubleText()));
  }

  /**
   * Binds the edit fields to the given wp. Allows for the unbinding and rebinding of properties as wp changes.
   * @param wp The ObservableValue for the selected waypoint.
   * @param controller The PathDisplayController to check the bounds of new waypoint values.
   */
  public void bindToWaypoint(ObservableValue<Waypoint> wp, FieldDisplayController controller) {
    double height = ProjectPreferences.getInstance().getField().getRealLength().getValue().doubleValue();
    // When changing X and Y values, verify points are within bounds
    xPosition.textProperty().addListener((observable, oldValue, newValue) -> {
      boolean validText = !("").equals(newValue) && !("").equals(yPosition.getText());
      if (validText && !controller.checkBounds(Double.parseDouble(newValue),
              Double.parseDouble(yPosition.getText()) - height)) {
        xPosition.setText(oldValue);
      }
    });
    yPosition.textProperty().addListener((observable, oldValue, newValue) -> {
      boolean validText = !("").equals(newValue) && !("").equals(xPosition.getText());
      if (validText && !controller.checkBounds(Double.parseDouble(xPosition.getText()),
              Double.parseDouble(newValue) - height)) {
        yPosition.setText(oldValue);
      }
    });
    wp.addListener((observable, oldValue, newValue) -> {
      if (oldValue != null) {
        unbind(oldValue);
      }
      if (newValue != null) {
        bind(newValue);
      }
    });
    enableSaving(wp);
    lockTangentOnEdit();
  }

  private void enableDoubleBinding(TextField field, DoubleProperty doubleProperty) {
    NumberStringConverter converter = new NumberStringConverter() {
      @Override
      public Number fromString(String value) {
        // Don't parse the beginning of a negative number
        if ("-".equals(value)) {
          return null;
        } else {
          return super.fromString(value);
        }
      }
    };
    field.textProperty().bindBidirectional(doubleProperty, converter);
  }

  private void yDoubleBinding(TextField field, DoubleProperty doubleProperty) {
    NumberStringConverter converter = new NumberStringConverter() {
      @Override
      public Double fromString(String value) {
        double height = ProjectPreferences.getInstance().getField().getRealLength().getValue().doubleValue();
        return Double.parseDouble(value) - height;
      }

      @Override
      public String toString(Number object){
        double height = ProjectPreferences.getInstance().getField().getRealLength().getValue().doubleValue();
        return String.format("%.3f", height + object.doubleValue());
      }
    };
    field.textProperty().bindBidirectional(doubleProperty, converter);
  }

  private void disableDoubleBinding(TextField field, DoubleProperty doubleProperty) {
    field.textProperty().unbindBidirectional(doubleProperty);
    field.setText("");
  }

  private void unbind(Waypoint oldValue) {
    controls.forEach(control -> control.setDisable(true));
    disableDoubleBinding(xPosition, oldValue.xProperty());
    disableDoubleBinding(yPosition, oldValue.yProperty());
    disableDoubleBinding(tangentX, oldValue.tangentXProperty());
    disableDoubleBinding(tangentY, oldValue.tangentYProperty());
    lockedTangent.selectedProperty().unbindBidirectional(oldValue.lockTangentProperty());
    reverseSpline.selectedProperty().unbindBidirectional(oldValue.reversedProperty());
    lockedTangent.setSelected(false);
    reverseSpline.setSelected(false);
    pointName.textProperty().removeListener(nameListener);
    pointName.setText("");
  }

  private void bind(Waypoint newValue) {
    controls.forEach(control -> control.setDisable(false));

    if (CurrentSelections.getCurPath().getStart() == newValue || CurrentSelections.getCurPath().getEnd() == newValue) {
      lockedTangent.setDisable(true);
      lockedTangent.setSelected(true);
    } else {
      lockedTangent.selectedProperty().bindBidirectional(newValue.lockTangentProperty());
    }
    reverseSpline.selectedProperty().bindBidirectional(newValue.reversedProperty());
    enableDoubleBinding(xPosition, newValue.xProperty());
    yDoubleBinding(yPosition, newValue.yProperty());
    enableDoubleBinding(tangentX, newValue.tangentXProperty());
    enableDoubleBinding(tangentY, newValue.tangentYProperty());
    pointName.setText(newValue.getName());
    nameListener = (observable, oldText, newText) -> newValue.setName(newText);
    pointName.textProperty().addListener(nameListener);
  }

  private void enableSaving(ObservableValue<Waypoint> wp) {
    // Save values when out of focus
    List.of(xPosition, yPosition, tangentX, tangentY, pointName)
        .forEach(textField -> {
          textField.setOnKeyReleased(event -> {
            if (!textField.getText().equals("") && wp.getValue() != null) {
              SaveManager.getInstance().addChange(CurrentSelections.getCurPath());
              CurrentSelections.getCurPath().update();
            }
            event.consume();
          });

          textField.setOnMouseClicked(event -> {
            if (!textField.getText().equals("") && wp.getValue() != null) {
              SaveManager.getInstance().addChange(CurrentSelections.getCurPath());
              CurrentSelections.getCurPath().update();
            }
          });
        });

    lockedTangent.selectedProperty()
            .addListener((listener, oldValue, newValue) -> {
              if (wp.getValue().isLockTangent() != newValue) {
                SaveManager.getInstance().addChange(CurrentSelections.getCurPath());
              }
            });
    reverseSpline.selectedProperty()
            .addListener((listener, oldValue, newValue) -> {
              if (wp.getValue().isReversed() != newValue) {
                SaveManager.getInstance().addChange(CurrentSelections.getCurPath());
              }
            });
  }

  private void lockTangentOnEdit() {
    tangentY.setOnKeyTyped((KeyEvent event) -> lockedTangent.setSelected(true));
    tangentX.setOnKeyTyped((KeyEvent event) -> lockedTangent.setSelected(true));
  }

  @FXML
  private void handleJavaExport() {
    String baseString = "var %s = new ControlVector(new double[] {%f, %f}, new double[] {%f, %f});";
    var wp = CurrentSelections.getCurWaypoint();
    String clipStr = String.format(baseString, wp.getName(), wp.getX(), wp.getTangentX(), wp.getY(), wp.getTangentY());
    content.putString(clipStr);
    clipboard.setContent(content);
  }

  @FXML
  private void handleCExport() {
    String baseString = "ControlVector %s{{%f, %f}, {%f, %f}};";
    var wp = CurrentSelections.getCurWaypoint();
    String clipStr = String.format(baseString, wp.getName(), wp.getX(), wp.getTangentX(), wp.getY(), wp.getTangentY());
    content.putString(clipStr);
    clipboard.setContent(content);
  }
}
