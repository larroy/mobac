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
/**
 * 
 */
package mobac.mapsources.mappacks.openstreetmap;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.model.TileImageType;

public class OpenPisteMap extends AbstractHttpMapSource {

	private static final String MAP_PISTE = "http://tiles.openpistemap.org/contours/";

	public OpenPisteMap() {
		super("OpenPisteMap", 0, 17, TileImageType.PNG, HttpMapSource.TileUpdate.LastModified);
	}

	@Override
	public String toString() {
		return "Open Piste Map";
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		return MAP_PISTE + zoom + "/" + tilex + "/" + tiley + ".png";
	}

}