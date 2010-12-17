package mobac.mapsources.loader;

import java.io.File;

import javax.swing.JOptionPane;

import mobac.mapsources.MapSourcesManager;
import mobac.mapsources.impl.BeanShellHttpMapSource;
import mobac.utilities.file.FileExtFilter;

import org.apache.log4j.Logger;

public class BeanShellMapSourceLoader {

	private final Logger log = Logger.getLogger(BeanShellMapSourceLoader.class);
	private final MapSourcesManager mapSourcesManager;
	private final File mapSourcesDir;

	public BeanShellMapSourceLoader(MapSourcesManager mapSourceManager, File mapSourcesDir) {
		this.mapSourcesManager = mapSourceManager;
		this.mapSourcesDir = mapSourcesDir;
	}

	public void loadBeanShellMapSources() {
		File[] customMapSourceFiles = mapSourcesDir.listFiles(new FileExtFilter(".bsh"));
		for (File f : customMapSourceFiles) {
			try {
				BeanShellHttpMapSource mapSource = BeanShellHttpMapSource.load(f);
				log.trace("BeanShell map source loaded: " + mapSource + " from file \"" + f.getName() + "\"");
				mapSourcesManager.addMapSource(mapSource);
			} catch (Exception e) {
				log.error("failed to load custom map source \"" + f.getName() + "\": " + e.getMessage(), e);
				JOptionPane.showMessageDialog(null, "msg", "title", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

}
