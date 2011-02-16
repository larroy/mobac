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
package mobac.mapsources.mappacks.openstreetmap;

import java.awt.Color;
import java.net.HttpURLConnection;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.ProgramInfo;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class OsmHikingLayers {

	public static final String MAP_HIKING_RELIEF = "http://www.wanderreitkarte.de/hills/";
	public static final String MAP_HIKING_BASE = "http://www.wanderreitkarte.de/base/";
	public static final String MAP_HIKING_TRAILS = "http://www.wanderreitkarte.de/topo/";

	public static class OsmHikingLayerBase extends AbstractHttpMapSource {

		public OsmHikingLayerBase() {
			super("OSM Hiking Base", 4, 18, TileImageType.PNG, HttpMapSource.TileUpdate.IfNoneMatch);
		}

		@Override
		public String toString() {
			return "OpenStreetMap Hiking Base only (Europe only)";
		}

		@Override
		protected void prepareTileUrlConnection(HttpURLConnection conn) {
			super.prepareTileUrlConnection(conn);
			conn.setRequestProperty("User-agent", ProgramInfo.getUserAgent());
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_HIKING_BASE + zoom + "/" + tilex + "/" + tiley + ".png";
		}
	}

	public static class OsmHikingLayerMap extends AbstractHttpMapSource {

		public OsmHikingLayerMap() {
			super("OSM Hiking", 4, 18, TileImageType.PNG, HttpMapSource.TileUpdate.IfNoneMatch);
		}

		@Override
		public String toString() {
			return "OpenStreetMap Hiking (Europe only)";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_HIKING_TRAILS + zoom + "/" + tilex + "/" + tiley + ".png";
		}

		@Override
		protected void prepareTileUrlConnection(HttpURLConnection conn) {
			super.prepareTileUrlConnection(conn);
			conn.setRequestProperty("User-agent", ProgramInfo.getUserAgent());
		}

	}

	public static class OsmHikingLayerRelief extends AbstractHttpMapSource {

		public OsmHikingLayerRelief() {
			super("OSM Hiking Relief", 4, 15, TileImageType.PNG, HttpMapSource.TileUpdate.IfNoneMatch);
		}

		@Override
		public String toString() {
			return "OpenStreetMap Hiking Relief only (Europe only)";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_HIKING_RELIEF + zoom + "/" + tilex + "/" + tiley + ".png";
		}

		@Override
		public Color getBackgroundColor() {
			return Color.WHITE;
		}

		@Override
		protected void prepareTileUrlConnection(HttpURLConnection conn) {
			super.prepareTileUrlConnection(conn);
			conn.setRequestProperty("User-agent", ProgramInfo.getUserAgent());
		}

	}
}
