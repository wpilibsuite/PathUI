package edu.wpi.first.pathweaver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import javax.measure.Unit;
import javax.measure.quantity.Length;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("PMD.SingleMethodSingleton")
public class ProjectPreferences {
	private static final String FILE_NAME = "pathweaver.json";

	private static ProjectPreferences instance;

	private final String directory;

	private Values values;

	private ProjectPreferences(String directory) {
		this.directory = directory;
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(directory, FILE_NAME))) {
			Gson gson = new Gson();
			values = gson.fromJson(reader, Values.class);
		} catch (JsonParseException e) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Preferences import error");
			alert.setContentText(
					"Preferences have been reset due to file corruption. You may have to reconfigure your project.");
			((Stage) alert.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);

			alert.show();
			setDefaults();
		} catch (IOException e) {
			setDefaults();
		}
	}

	private void setDefaults() {
		values = new Values("FOOT", 10.0, 60.0, 2.0, Game.INFINTE_RECHARGE_2020.getName(), null);
		updateValues();
	}

	private void updateValues() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			Files.createDirectories(Paths.get(directory));

			try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(directory, FILE_NAME))) {
				gson.toJson(values, writer);
			}
		} catch (IOException e) {
			Logger log = Logger.getLogger(getClass().getName());
			log.log(Level.WARNING, "Couldn't update Project Preferences", e);
		}
	}

	/**
	 * Sets the preferences for the current project.
	 *
	 * @param values
	 *            Values to set for preferences.
	 */
	public void setValues(Values values) {
		this.values = values;
		updateValues();
	}

	public String getDirectory() {
		return directory;
	}

	/**
	 * Return the singleton instance of ProjectPreferences for a given project
	 * directory.
	 *
	 * @param folder
	 *            Path to project folder.
	 * @return Singleton instance of ProjectPreferences.
	 */
	@SuppressWarnings("PMD.NonThreadSafeSingleton")
	public static ProjectPreferences getInstance(String folder) {
		if (instance == null || !instance.directory.equals(folder)) {
			instance = new ProjectPreferences(folder);
		}
		return instance;
	}

	/**
	 * Returns the singleton instance of ProjectPreferences for the previously
	 * requested directory or the default directory.
	 *
	 * @return Singleton instance of ProjectPreferences.
	 */
	public static ProjectPreferences getInstance() {
		return instance;
	}

	public static boolean projectExists(String folder) {
		return Files.exists(Paths.get(folder, FILE_NAME));
	}

	/**
	 * Returns a Field object for the current project's game year. Defaults to Power
	 * Up.
	 *
	 * @return Field for project's game year.
	 */
	public Field getField() {
		if (values.getGameName() == null) {
			values.gameName = Game.DEEP_SPACE_2019.getName();
			updateValues();
		}
		Game game = Game.fromPrettyName(values.gameName);
		if (game == null) {
			throw new UnsupportedOperationException("The referenced game is unknown: \"" + values.gameName + "\"");
		}
		if (values.getLengthUnit() == null) {
			values.lengthUnit = "FOOT";
			updateValues();
		}
		Field field = game.getField();
		field.convertUnit(values.getLengthUnit());
		return field;
	}

	/**
	 * Returns the folder to output the generated paths to.
	 *
	 * @return File object of Folder to output generated paths to.
	 */
	public File getOutputDir() {
		if (values.getOutputDir() == null) {
			File parentDirectory = new File(directory).getParentFile();
			return getOutputDir(parentDirectory);
		} else {
			File output = new File(directory, values.getOutputDir());
			return getOutputDir(output);
		}
	}

	/**
	 * Returns the output directory relative to a specified directory. If the
	 * directory is an FRC project, it returns the proper deploy directory.
	 * Otherwise it simply returns the output subdirectory.
	 *
	 * @param directory
	 *            Directory to return output directory for.
	 * @return A File that is the output directory.
	 */
	private File getOutputDir(File directory) {
		if (isFRCProject(directory)) {
			return getDeployDirectory(directory);
		} else {
			return new File(directory, "output");
		}
	}

	private File getDeployDirectory(File directory) {
		return new File(directory, "src/main/deploy/paths");
	}

	private boolean isFRCProject(File directory) {
		return new File(directory, "build.gradle").exists();
	}

	public Values getValues() {
		return values;
	}

	public static class Values {
		private String lengthUnit;
		private final double maxVelocity;
		private final double maxAcceleration;
		private final double wheelBase;
		private String gameName;
		private final String outputDir;

		/**
		 * Constructor for Values of ProjectPreferences.
		 *
		 * @param lengthUnit
		 *            The unit to use for distances
		 * @param maxVelocity
		 *            The maximum velocity the body is capable of travelling at
		 * @param maxAcceleration
		 *            The maximum acceleration to use
		 * @param wheelBase
		 *            The width between the individual sides of the drivebase
		 * @param gameName
		 *            The year/FRC game
		 * @param outputDir
		 *            The directory for the output files
		 */
		public Values(String lengthUnit, double maxVelocity, double maxAcceleration,
				double wheelBase, String gameName, String outputDir) {
			this.lengthUnit = lengthUnit;
			this.maxVelocity = maxVelocity;
			this.maxAcceleration = maxAcceleration;
			this.wheelBase = wheelBase;
			this.gameName = gameName;
			this.outputDir = outputDir;
		}

		public Unit<Length> getLengthUnit() {
			return PathUnits.getInstance().length(this.lengthUnit);
		}

		public double getMaxVelocity() {
			return maxVelocity;
		}

		public double getMaxAcceleration() {
			return maxAcceleration;
		}

		public double getWheelBase() {
			return wheelBase;
		}

		public String getGameName() {
			return gameName;
		}

		public String getOutputDir() {
			return outputDir;
		}
	}
}
