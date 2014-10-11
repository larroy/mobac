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

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import mobac.exceptions.MapCreationException;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.interfaces.MapSource;
import mobac.utilities.Utilities;

/**
 * https://sourceforge.net/p/mobac/feature-requests/263/
 * 
 * http://www.geopackage.org/spec/
 */
@AtlasCreatorName(value = "GeoPackage SQLite", type = "GeoPackage")
public class GeoPackage extends AbstractSQLite {

	private static final String CREATE_TABLE_TILEMATRIX = "CREATE TABLE gpkg_tile_matrix "
			+ "(table_name TEXT NOT NULL, zoom_level INTEGER NOT NULL, matrix_width INTEGER NOT NULL, "
			+ "matrix_height INTEGER NOT NULL, tile_width INTEGER NOT NULL, tile_height INTEGER NOT NULL, "
			+ "pixel_x_size DOUBLE NOT NULL, pixel_y_size DOUBLE NOT NULL, "
			+ "CONSTRAINT pk_ttm PRIMARY KEY (table_name, zoom_level), "
			+ "CONSTRAINT fk_tmm_table_name FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name));";

	private static final String CREATE_TILE_TABLE = "CREATE TABLE ? (id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "zoom_level INTEGER NOT NULL, tile_column INTEGER NOT NULL, tile_row INTEGER NOT NULL, tile_data BLOB NOT NULL), "
			+ "UNIQUE (zoom_level, tile_column, tile_row))";

	private static final String INSERT_TILE = "INSERT INTO TABLE ? (id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "zoom_level INTEGER NOT NULL, tile_column INTEGER NOT NULL, tile_row INTEGER NOT NULL, tile_data BLOB NOT NULL) "
			+ "VALUES (?, ?, ?, ?)";

	protected String tableName;
	
	public GeoPackage() {
		super();
	}

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return true;
	}

	@Override
	protected String getDatabaseFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void updateTileMetaInfo() throws SQLException {
		// TODO Auto-generated method stub

	}

	
	
	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			Utilities.mkDir(atlasDir);
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		}
		try {
			openConnection();
			initializeDB();
			createTiles();
		} catch (SQLException e) {
			throw new MapCreationException("Error creating SQL database \"" + databaseFile + "\": " + e.getMessage(),
					map, e);
		}
	}

	private void initializeDB() throws SQLException {
		Statement stat = conn.createStatement();
		stat.executeUpdate(CREATE_TABLE_TILEMATRIX);
		stat.close();
		
		
	}

	@Override
	protected String getTileInsertSQL() {
		return INSERT_TILE;
	}

	@Override
	protected void writeTile(int x, int y, int z, byte[] tileData) throws SQLException, IOException {
		prepStmt.setString(1, tableName);
		prepStmt.setInt(2, z);
		prepStmt.setInt(3, x);
		prepStmt.setInt(4, y);
		prepStmt.setBytes(5, tileData);
		prepStmt.addBatch();
	}

}
