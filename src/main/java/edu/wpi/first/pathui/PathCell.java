package edu.wpi.first.pathui;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

/** TreeItem with support for dragging.
 *
 */
public class PathCell extends TreeCell<String> {
  private final TreeCell cell;

  //having a single tempItem you use for all dragging
  //means that any dragover call is able to remove the temporary
  //item from past locations
  //each cell might have its own dragover call as you move around
  private static final TreeItem<String> tempItem = new TreeItem<>("");

  /**
   * Creates PathCell, a TreeCell object that can be dragged and used as a drag target.
   *
   * @param validDropTarget If this item should allow drag over and drag drop.
   */
  public PathCell(boolean validDropTarget) {
    super();
    cell = this;
    setupDragStart();

    if (validDropTarget) {
      setupDragOver();
      setupDragDrop();
    }
  }

  @Override
  protected void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);
    setText(item);
  }

  private void setupDragDrop() {
    this.setOnDragDropped(event -> {
      TreeItem<String> newItem= new TreeItem<>(tempItem.getValue()); //make new item

      int index = tempItem.getParent().getChildren().indexOf(tempItem); //get location of old item
      tempItem.getParent().getChildren().add(index,newItem); // add new item to temp item location

      tempItem.getParent().getChildren().remove(tempItem); //reset tempItem
      tempItem.setValue("");
    });
  }

  private void setupDragStart() {
    this.setOnDragDetected(event -> {
      TreeItem<String> item = cell.getTreeItem();
      if (item != null && item.isLeaf()) {
        Dragboard db = cell.startDragAndDrop(TransferMode.COPY);
        ClipboardContent content = new ClipboardContent();
        content.putString(item.getValue());
        db.setContent(content);
        event.consume();
      }
    });
  }


  private void setupDragOver() {

    cell.setOnDragOver(event -> {
      TreeItem<String> item = cell.getTreeItem();
      if (item != null) {
        TreeItem<String> parent = item.getParent();

        if (parent != null
            && event.getGestureSource() != cell
            && event.getDragboard().hasString()) {
          Dragboard db = event.getDragboard();
          String source = db.getString();

          //TODO check if duplicate
          if (item.isLeaf() && item.getParent().getParent() != null) {
            int currentIndex = parent.getChildren().indexOf(item);
            event.acceptTransferModes(TransferMode.COPY);
            //place it
            parent.setExpanded(true);
            setTemp(source, parent, currentIndex);
          } else {
            item.setExpanded(true);
            event.acceptTransferModes(TransferMode.COPY);
            setTemp(source, item, 0);
          }
          event.consume();

        }
      }
    });
  }

  private void setTemp(String source, TreeItem<String> parent, int currentIndex) {
    removeTemp();
    if (currentIndex >= 0) {
      parent.getChildren().add(currentIndex, tempItem);
      tempItem.setValue(source);
    }
  }

  private void removeTemp() {
    TreeItem<String> tempParent = tempItem.getParent();
    if (tempParent != null) {
      tempParent.getChildren().remove(tempItem);
    }
  }

}



