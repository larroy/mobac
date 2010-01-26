package mobac.utilities.jdbc;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import mobac.program.DirectoryManager;

import org.apache.log4j.Logger;

import SQLite.Database;

/**
 * Dynamic loading of "SQLite Java Wrapper/JDBC Driver" (BSD-style license)
 * http://www.ch-werner.de/javasqlite/
 */
public class SQLiteLoader {

	private static final Logger log = Logger.getLogger(SQLiteLoader.class);

	private static boolean SQLITE_LOADED = false;

	public static final String MSG_SQLITE_MISSING = "Unable to find the SQLite libraries. "
			+ "These are required for BigPlanet output format.<br>Please read the README.HTM "
			+ "section \"Creating and using atlases with BigPlanet / RMaps\". ";

	public static boolean loadSQLiteOrShowError() {
		try {
			SQLiteLoader.loadSQLite();
			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "<html>" + MSG_SQLITE_MISSING + "</html>",
					"Error - SQLite not available", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public static synchronized void loadSQLite() throws SQLException {
		if (SQLITE_LOADED)
			return;
		try {
			// Load the native library
			System.loadLibrary("sqlite_jni");
			log.debug("SQLite library database version: " + Database.version());
			DriverManager.registerDriver(new SQLite.JDBCDriver());
			SQLITE_LOADED = true;
			return;
		} catch (Throwable t) {
			log.error("Loading of sqlite_jni from system library path failed: " + t.getMessage());
		}

		try {
			File pd = DirectoryManager.programDir;
			File cd = DirectoryManager.currentDir;
			File[] dirList = new File[] { pd, new File(pd, "lib"), cd, new File(cd, "lib") };

			File verifiedLibDir = null;
			String libName = System.mapLibraryName("sqlite_jni");
			for (File libDir : dirList) {
				File lib = new File(libDir, libName);
				if (lib.isFile()) {
					verifiedLibDir = libDir;
					log.debug("sqlite_jni found: " + lib.getAbsolutePath());
					System.load(lib.getAbsolutePath());
					break;
				}
			}
			if (verifiedLibDir == null)
				throw new SQLException("Native SQLite library not found: " + libName);

			log.debug("SQLite library database version: " + Database.version());
			log.info("Dynamic loading of SQLite library succeeded");
			DriverManager.registerDriver(new SQLite.JDBCDriver());
			SQLITE_LOADED = true;
			return;
		} catch (SQLException e) {
			throw e;
		} catch (Throwable t) {
			log.error("SQLite loading failed: ", t);
			throw new SQLException(t);
		}
	}

}
