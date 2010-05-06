package mobac.program;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

import javax.swing.JOptionPane;

import mobac.mapsources.impl.Google;
import mobac.program.model.Atlas;
import mobac.program.model.EastNorthCoordinate;
import mobac.program.model.Layer;
import mobac.program.model.Profile;
import mobac.program.model.Settings;
import mobac.utilities.GUIExceptionHandler;
import mobac.utilities.file.NamePatternFileFilter;

import org.apache.log4j.Logger;


/**
 * Creates the necessary files on first time Mobile Atlas Creator is started or
 * tries to update the environment if the version has changed.
 */
public class EnvironmentSetup {

	public static Logger log = Logger.getLogger(EnvironmentSetup.class);

	public static void checkMemory() {
		Runtime r = Runtime.getRuntime();
		long maxHeap = r.maxMemory();
		String heapMBFormatted = String.format(Locale.ENGLISH, "%3.2f MiB", maxHeap / 1048576d);
		log.info("Total avialable memory to MOBAC: " + heapMBFormatted);
		if (maxHeap < 200000000) {
			String msg = "<html><b>WARNING:</b> Mobile Atlas Creator has been started "
					+ "with a very small amount of memory assigned.<br>"
					+ "The current maximum usable amount of memory to Mobile Atlas Creator is <b>"
					+ heapMBFormatted
					+ "</b>.<br><br>Please make sure to start Mobile Atlas Creator in "
					+ "the future via the provided start scripts <i>Mobile Atlas Creator.exe</i><br>"
					+ "on Windows or <i>start.sh</i> on Linux/Unix/OSX or add the "
					+ "parameter <b>-Xmx 512M</b> to your startup command.<br><br>"
					+ "Example: <i>java -Xmx512M -jar Mobile_Atlas_Creator.jar</i><br>"
					+ "<br><center>Press OK to continue and start Mobile Atlas Creator</center></html>";
			JOptionPane.showMessageDialog(null, msg, "Warning: low memory",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	public static void upgrade() {
		FileFilter ff = new NamePatternFileFilter("tac-profile-.*.xml");
		File profilesDir = DirectoryManager.currentDir;
		File[] files = profilesDir.listFiles(ff);
		for (File f : files) {
			File dest = new File(profilesDir, f.getName().replaceFirst("tac-", "mobac-"));
			f.renameTo(dest);
		}
	}

	public static void checkFileSetup() {
		if (Settings.FILE.exists())
			return;

		try {
			Settings.save();
		} catch (Exception e) {
			log.error("Error while creating settings.xml: " + e.getMessage(), e);
			String[] options = { "Exit", "Show error report" };
			int a = JOptionPane.showOptionDialog(null,
					"Could not create file settings.xml program will exit.", "Error", 0,
					JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			if (a == 1)
				GUIExceptionHandler.showExceptionDialog(e);
			System.exit(1);
		}
		Profile p = new Profile("Google Maps New York");
		Atlas atlas = Atlas.newInstance();
		try {
			EastNorthCoordinate max = new EastNorthCoordinate(40.97264, -74.142609);
			EastNorthCoordinate min = new EastNorthCoordinate(40.541982, -73.699036);
			Layer layer = new Layer(atlas, "GM New York");
			layer.addMapsAutocut("GM New York 16", new Google.GoogleMaps(), max, min, 16, null,
					32000);
			layer.addMapsAutocut("GM New York 14", new Google.GoogleMaps(), max, min, 14, null,
					32000);
			atlas.addLayer(layer);
			p.save(atlas);
		} catch (Exception e) {
			log.error("Creation for example profiles failed", e);
			GUIExceptionHandler.showExceptionDialog(e);
		}
	}
}
