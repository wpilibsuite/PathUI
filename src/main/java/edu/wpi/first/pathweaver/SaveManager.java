package edu.wpi.first.pathweaver;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import edu.wpi.first.pathweaver.path.Path;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class SaveManager {
  private static final SaveManager INSTANCE = new SaveManager();

  private final Set<Path> paths = new HashSet<>();

  /**
   * Return the singleton instance of SaveManager. Tracks which files have been edited so the user can be prompted to
   * save them upon exit.
   * @return Singleton instance of SaveManager.
   */
  public static SaveManager getInstance() {
    return INSTANCE;
  }

  public void addChange(Path path) {
    paths.add(path);
  }

  public boolean hasChanges(Path path) {
    return paths.contains(path);
  }

  /**
   * Saves all changed Paths without prompts.
   */
  public void saveAll() {
    for (Path path : paths) {
      saveChange(path, false);
    }
    paths.clear();
  }

  public boolean promptSaveAll() {
    return promptSaveAll(true);
  }
  
  /**
   * Saves all Paths the user confirms are valid changes. Prompts the user for feedback.
   * @param allowCancel Whether to allow the user to cancel the save.
   * @return True if application should close, false otherwise.
   */
  public boolean promptSaveAll(boolean allowCancel) {
    for (Path path : paths) {
      Alert alert = new Alert(Alert.AlertType.NONE);
      FxUtils.applyDarkMode(alert);
      alert.setTitle(path.getPathName() + " has been modified");
      alert.setContentText("Save " + path.getPathName() + "?");
      alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
      if (allowCancel) {
        alert.getButtonTypes().add(ButtonType.CANCEL);
      }
      Optional<ButtonType> buttonType = alert.showAndWait();
      if (buttonType.isPresent()) {
        if (buttonType.get() == ButtonType.YES) {
          saveChange(path, false);
        } else if (buttonType.get() == ButtonType.CANCEL) {
          return false;
        }
      }
    }
    paths.clear(); // User has taken action on all paths
    return true;
  }

  /**
   * Saves the given path to the Project's Path directory. Removes the path from the set of modified paths.
   * @param path Path to save.
   */
  public void saveChange(Path path) {
    saveChange(path, true);
  }

  /**
   * Saves the given path to the Project's Path directory.
   * @param path Path to save.
   * @param remove Whether to remove Path from set of modified paths.
   */
  private void saveChange(Path path, boolean remove) {
    String pathDirectory = ProjectPreferences.getInstance().getDirectory() + "/Paths/";
    PathIOUtil.export(pathDirectory, path, 0d);
    if (remove) {
      paths.remove(path);
    }
  }

  /**
   * Removes a saved path from the list of saved paths.
   * @param path The Path to no longer save.
   */
  public void removeChange(Path path) {
    paths.remove(path);
  }

}
