/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.program;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

import mobac.utilities.Utilities;

/**
 * Provides the five common directories used within Mobile Atlas Creator:
 * <ul>
 * <li>current directory</li>
 * <li>program directory</li>
 * <li>user home directory</li>
 * <li>user settings directory</li>
 * <li>temporary directory</li>
 * </ul>
 * 
 */
public class DirectoryManager {

	public static final File currentDir;
	public static final File programDir;
	public static final File userHomeDir;
	public static final File tempDir;
	public static final File userAppDataDir;

	public static final File userSettingsDir;
	public static final File mapSourcesDir;
	public static final File atlasProfilesDir;
	public static final File tileStoreDir;

	private static Properties dirConfig;

	static {
		currentDir = new File(System.getProperty("user.dir"));
		userHomeDir = new File(System.getProperty("user.home"));
		tempDir = new File(System.getProperty("java.io.tmpdir"));
		programDir = getProgramDir();
		loadDirectoriesIni();

		userAppDataDir = getUserAppDataDir();
		mapSourcesDir = applyDirConfig("mobac.mapsourcesdir", new File(programDir, "mapsources"));
		userSettingsDir = applyDirConfig("mobac.usersettingsdir", programDir);
		atlasProfilesDir = applyDirConfig("mobac.atlasprofilesdir", currentDir);
		tileStoreDir = applyDirConfig("mobac.tilestoredir", new File(programDir, "tilestore"));
		
	}

	private static File applyDirConfig(String propertyName, File defaultDir) {
		try {
			final String dirCfg = dirConfig.getProperty(propertyName);
			if (dirCfg == null) {
				return defaultDir;
			} else {
				return getFileFromString(dirCfg);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "<html><p>Failed to load directory.ini - entry \"" + propertyName
					+ "\":<p><p>" + e.getMessage() + "</p></html>", "Faile do load directory.ini",
					JOptionPane.ERROR_MESSAGE);
			return defaultDir;
		}
	}

	private static File getFileFromString(String s) {
		s = s.trim();
		if (s.startsWith("${mobac-prog}")) {
			s = s.substring(Math.min(14, s.length()));
			if (s.length() > 0)
				return new File(programDir, s);
			else
				return programDir;
		} else if (s.startsWith("${home}")) {
			s = s.substring(Math.min(8, s.length()));
			if (s.length() > 0)
				return new File(userHomeDir, s);
			else
				return userHomeDir;
		} else
			return new File(s);
	}

	public static void initialize() {
		if (currentDir == null || userAppDataDir == null || tempDir == null || programDir == null)
			throw new RuntimeException("DirectoryManager failed");
	}

	private static void loadDirectoriesIni() {
		File dirIniFile = new File(programDir, "directories.ini");
		if (!dirIniFile.isFile())
			return;
		dirConfig = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream(dirIniFile);
			dirConfig.load(in);
		} catch (IOException e) {
			System.err.println("Failed to load " + dirIniFile.getName());
			e.printStackTrace();
		} finally {
			Utilities.closeStream(in);
		}
	}

	/**
	 * Returns the directory from which this java program is executed
	 * 
	 * @return
	 */
	private static File getProgramDir() {
		File f = null;
		try {
			f = Utilities.getClassLocation(DirectoryManager.class);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return currentDir;
		}
		if ("bin".equals(f.getName())) // remove the bin dir -> this usually
			// happens only in a development environment
			return f.getParentFile();
		else
			return f;
	}

	/**
	 * Returns the directory where Mobile Atlas Creator saves it's application settings.
	 * 
	 * Examples:
	 * <ul>
	 * <li>English Windows XP:<br>
	 * <tt>C:\Document and Settings\%username%\Application Data\Mobile Atlas Creator</tt>
	 * <li>Vista:<br>
	 * <tt>C:\Users\%username%\Application Data\Mobile Atlas Creator</tt>
	 * <li>Linux:<br>
	 * <tt>/home/$username$/.mobac</tt></li>
	 * </ul>
	 * 
	 * @return
	 */
	private static File getUserAppDataDir() {
		String appData = System.getenv("APPDATA");
		if (appData != null) {
			File appDataDir = new File(appData);
			if (appDataDir.isDirectory()) {
				File mobacDataDir = new File(appData, "Mobile Atlas Creator");
				if (mobacDataDir.isDirectory() || mobacDataDir.mkdir())
					return mobacDataDir;
				else
					throw new RuntimeException("Unable to create directory \"" + mobacDataDir.getAbsolutePath() + "\"");
			}
		}
		File userDir = new File(System.getProperty("user.home"));
		File mobacUserDir = new File(userDir, ".mobac");
		if (!mobacUserDir.exists() && !mobacUserDir.mkdir())
			throw new RuntimeException("Unable to create directory \"" + mobacUserDir.getAbsolutePath() + "\"");
		return mobacUserDir;
	}
}
