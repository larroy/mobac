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
package mobac.program.atlascreators;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.imageio.ImageIO;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.RequiresSQLite;
import mobac.utilities.jdbc.SQLiteLoader;

/**
 * http://sourceforge.net/tracker/?func=detail&aid=3154177&group_id=238075&atid=1105496
 */
public class OsmdroidSQLite extends AtlasCreator implements RequiresSQLite {

	protected Connection conn = null;

	public OsmdroidSQLite() {
		super();
		SQLiteLoader.loadSQLiteOrShowError();
	}

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return true;
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas, File customAtlasDir) throws IOException, AtlasTestException,
			InterruptedException {
		super.startAtlasCreation(atlas, customAtlasDir);
		String databaseFile = new File(atlasDir, atlas.getName() + ".sqlite").getAbsolutePath();
		try {
			SQLiteLoader.loadSQLite();
		} catch (SQLException e) {
			throw new AtlasTestException(SQLiteLoader.MSG_SQLITE_MISSING);
		}
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
			Statement stat = conn.createStatement();
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS tiles (key INTEGER PRIMARY KEY, provider TEXT, tile BLOB)");
			stat.close();
		} catch (SQLException e) {
			throw new AtlasTestException("Error creating SQL database \"" + databaseFile + "\": " + e.getMessage());
		}
		log.debug("SQLite Database file: " + databaseFile);
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			String provider = map.getMapSource().getName();
			atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
			conn.setAutoCommit(false);
			ImageIO.setUseCache(false);
			PreparedStatement prep = conn.prepareStatement("insert into tiles values (?, ?, ?);");

			for (int x = xMin; x <= xMax; x++)
				for (int y = yMin; y <= yMax; y++) {
					checkUserAbort();
					atlasProgress.incMapCreationProgress();
					byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
					if (sourceTileData != null) {
						int index = (((zoom << zoom) + x) << zoom) + y;
						prep.setInt(1, index);
						prep.setString(2, provider);
						prep.setBytes(3, sourceTileData);
						prep.addBatch();
					}
				}
			prep.executeBatch();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			throw new MapCreationException("Error writing tile image: " + e.getMessage(), map, e);
		} catch (IOException e) {
			throw new MapCreationException("Error writing tile image: " + e.getMessage(), map, e);
		}
	}

	@Override
	public void abortAtlasCreation() throws IOException {
		SQLiteLoader.closeConnection(conn);
		conn = null;
		super.abortAtlasCreation();
	}

	@Override
	public void finishAtlasCreation() throws IOException, InterruptedException {
		SQLiteLoader.closeConnection(conn);
		conn = null;
		super.finishAtlasCreation();
	}

}
