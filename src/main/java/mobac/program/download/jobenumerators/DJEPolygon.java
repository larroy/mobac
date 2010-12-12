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
package mobac.program.download.jobenumerators;

import java.awt.Polygon;

import mobac.program.JobDispatcher.Job;
import mobac.program.download.DownloadJob;
import mobac.program.interfaces.DownloadJobListener;
import mobac.program.interfaces.MapSource;
import mobac.program.model.MapPolygon;
import mobac.utilities.tar.TarIndexedArchive;

/**
 * Enumerates / creates the download jobs for a single layer map with a polygonal selection.
 */
public class DJEPolygon extends DJERectangle {

	protected final int tileSize;
	protected Polygon polygon;

	public DJEPolygon(MapPolygon map, MapSource mapSource, TarIndexedArchive tileArchive,
			DownloadJobListener listener) {
		super(map, mapSource, tileArchive, listener);
		this.polygon = map.getPolygon();
		tileSize = mapSource.getMapSpace().getTileSize();
		x--;
		nextElement();
	}

	public Polygon getPolygon() {
		return polygon;
	}

	@Override
	public Job nextElement() {
		Job job = nextJob;
		boolean intersects = false;
		do {
			x++;
			if (x > xMax) {
				y++;
				x = xMin;
				if (y > yMax) {
					nextJob = null;
					return job;
				}
			}
			int tileCoordinateX = x * tileSize;
			int tileCoordinateY = y * tileSize;
			intersects = polygon.intersects(tileCoordinateX, tileCoordinateY, tileSize, tileSize);
			// System.out.println(String.format("x=%5d y=%5d %s",
			// tileCoordinateX, tileCoordinateY,
			// Boolean.toString(intersects)));
		} while (!intersects);
		nextJob = new DownloadJob(mapSource, x, y, zoom, tileArchive, listener);
		return job;
	}
}
