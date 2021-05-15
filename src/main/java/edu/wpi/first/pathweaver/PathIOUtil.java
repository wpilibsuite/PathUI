package edu.wpi.first.pathweaver;

import edu.wpi.first.pathweaver.path.Path;
import edu.wpi.first.pathweaver.path.wpilib.WpilibPath;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.geometry.Point2D;

public final class PathIOUtil {
  private static final Logger LOGGER = Logger.getLogger(PathIOUtil.class.getName());

  private PathIOUtil() {
  }

  /**
   * Exports path object to csv file.
   *
   * @param fileLocation the directory and filename to write to
   * @param path         Path object to save
   *
   * @return true if successful file write was preformed
   */
  public static boolean export(String fileLocation, Path path, double yoffset) {
    try (
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileLocation + path.getPathName()));

        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
            .withHeader("X", "Y", "Tangent X", "Tangent Y", "Fixed Theta", "Reversed", "Name"))
    ) {
      for (Waypoint wp : path.getWaypoints()) {
        double xPos = wp.getX();
        double yPos = wp.getY() + yoffset;
        double tangentX = wp.getTangentX();
        double tangentY = wp.getTangentY();
        String name = wp.getName();
        csvPrinter.printRecord(xPos, yPos, tangentX, tangentY, wp.isLockTangent(), wp.isReversed(), name);
      }
      csvPrinter.flush();
    } catch (IOException except) {
      LOGGER.log(Level.WARNING, "Could not save Path file", except);
      return false;
    }
    return true;
  }

  /**
   * Imports Path object from disk.
   *
   * @param fileLocation Folder with path file
   * @param fileName     Name of path file
   *
   * @return Path object saved in Path file
   */
  public static Path importPath(String fileLocation, String fileName) {
    try(Reader reader = Files.newBufferedReader(java.nio.file.Path.of(fileLocation, fileName));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim())) {
      ArrayList<Waypoint> waypoints = new ArrayList<>();
      for (CSVRecord csvRecord : csvParser) {
        Point2D position = new Point2D(
                Double.parseDouble(csvRecord.get("X")),
                Double.parseDouble(csvRecord.get("Y"))
        );
        Point2D tangent = new Point2D(
                Double.parseDouble(csvRecord.get("Tangent X")),
                Double.parseDouble(csvRecord.get("Tangent Y"))
        );
        boolean locked = Boolean.parseBoolean(csvRecord.get("Fixed Theta"));
        boolean reversed = Boolean.parseBoolean(csvRecord.get("Reversed"));
        Waypoint point = new Waypoint(position, tangent, locked, reversed);
        if (csvRecord.isMapped("Name")) {
          String name = csvRecord.get("Name");
          point.setName(name);
        }
        waypoints.add(point);
      }
      return new WpilibPath(waypoints, fileName);
    } catch (IOException except) {
      LOGGER.log(Level.WARNING, "Could not read Path file", except);
      return null;
    }
  }
}
