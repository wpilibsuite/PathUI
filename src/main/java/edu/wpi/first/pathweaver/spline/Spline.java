package edu.wpi.first.pathweaver.spline;

import edu.wpi.first.pathweaver.path.Path;

import edu.wpi.first.pathweaver.global.CurrentSelections;
import javafx.scene.Group;

/**
 * This interface represents a Spline - the function that describes the path
 * the robot will take when travelling across the field. This class is designed for use with
 * SplineSegment but can be used without or with an alternative implementation so long
 * as the behavior obeys the "as-if' rule.
 * <p>
 * Note: This interface places drag and drop constraints on implementors.
 * Implementors of this class should see {@link AbstractSpline} for
 * an example of setting up drag and drop for the spline and {@link SplineSegment}
 * for an example of setting up drag and drop for segments between splines.
 * <p>
 * Drag and drop is fundamental to Pathweaver, and implmentors of this interface
 * must abide by the following policies. Between {@link Spline} and {@link edu.wpi.first.pathweaver.path.Path}, the
 * following variables must get set in {@link CurrentSelections}:
 *
 * <ul>
 * <li>{@link CurrentSelections#curPathProperty()}: To be set whenever a Path is clicked or dragged.
 * see {@link edu.wpi.first.pathweaver.path.wpilib.WpilibPath} and {@link SplineSegment} for examples.
 * <li>{@link CurrentSelections#curWaypointProperty()}: To be set whenever a Waypoint is clicked or dragged,
 * see {@link edu.wpi.first.pathweaver.path.wpilib.WpilibPath} for an example.
 * <li>{@link CurrentSelections#curSegmentStartProperty()} and {@link CurrentSelections#curSegmentEndProperty()}: To be set
 * whenever a SplineSegment (or functional equivalent) is clicked on or dragged.
 * </ul>
 *
 * @see CurrentSelections
 * @see Path
 * @see edu.wpi.first.pathweaver.path.wpilib.WpilibPath
 * @see SplineSegment
 */

public interface Spline {
  /**
   * Updates the current spline
   */
  void update();

  /**
   * Adds this spline to a {@link Group}, while scaling all drawn lines/shapes by the scalefactor.
   * @param splineGroup the group to add this spline to
   * @param scaleFactor the scalefactor to scale all drawing by
   */
  void addToGroup(Group splineGroup, double scaleFactor);

  //IDK how this works just copy the impl
  void enableSubchildSelector(int i);

  /**
   * Removes this spline from a group
   * @param splineGroup the group to remove this spline from
   */
  void removeFromGroup(Group splineGroup);

  /**
   * Serialize this path to a file for use in the robot.
   * @param path the path of the file to write to
   * @return whether the write succeeded
   */
  boolean writeToFile(java.nio.file.Path path);

  /**
   * Export this path to a file for use in the robot.
   * @param path the path of the file to write to
   * @return whether the write succeeded
   */
  boolean writePathToFile(Path path);
}
