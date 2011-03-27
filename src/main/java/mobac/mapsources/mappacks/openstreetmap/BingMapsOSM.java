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

public class BingMapsOSM extends AbstractOsmMapSource {

	private static char[] SERVERS = { 'a', 'b', 'c' };
	private static int SERVER_NUM = 0;

	public BingMapsOSM() {
		super("BingMapsOSM");
		tileUpdate = TileUpdate.IfModifiedSince;
	}

	public String getTileUrl(int zoom, int x, int y) {
		char server = SERVERS[SERVER_NUM];
		SERVER_NUM = (SERVER_NUM + 1) % SERVERS.length;
		return "http://" + server + ".osm.virtualearth.net/" + zoom + "/" + x + "/" + y + ".png";
	}

	@Override
	public String toString() {
		return "OpenStreetMap Bing layer";
	}

}
