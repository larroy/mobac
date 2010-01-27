package org.openstreetmap.gui.jmapviewer;

import java.awt.Graphics;

import org.openstreetmap.gui.jmapviewer.JobDispatcher.JobThread;
import org.openstreetmap.gui.jmapviewer.Tile.TileState;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapTileLayer;

public class DefaultMapTileLayer implements MapTileLayer {

	protected JMapViewer mapViewer;

	protected MapSource mapSource;

	protected boolean usePlaceHolders;

	public DefaultMapTileLayer(JMapViewer mapViewer, MapSource mapSource) {
		this.mapViewer = mapViewer;
		this.mapSource = mapSource;
	}

	public void startPainting(MapSource mapSource) {
		usePlaceHolders = mapViewer.usePlaceHolderTiles;
	}

	public void paintTile(Graphics g, int gx, int gy, int tilex, int tiley, int zoom) {
		Tile tile = getTile(tilex, tiley, zoom);
		if (tile == null)
			return;
		tile.paint(g, gx, gy);
	}

	/**
	 * retrieves a tile from the cache. If the tile is not present in the cache
	 * a load job is added to the working queue of {@link JobThread}.
	 * 
	 * @param tilex
	 * @param tiley
	 * @param zoom
	 * @return specified tile from the cache or <code>null</code> if the tile
	 *         was not found in the cache.
	 */
	protected Tile getTile(int tilex, int tiley, int zoom) {
		int max = (1 << zoom);
		if (tilex < 0 || tilex >= max || tiley < 0 || tiley >= max)
			return null;
		Tile tile = mapViewer.tileCache.getTile(mapSource, tilex, tiley, zoom);
		if (tile == null) {
			tile = new Tile(mapSource, tilex, tiley, zoom);
			mapViewer.tileCache.addTile(tile);
			if (usePlaceHolders)
				tile.loadPlaceholderFromCache(mapViewer.tileCache);
		}
		if (tile.getTileState() == TileState.TS_NEW) {
			mapViewer.jobDispatcher.addJob(mapViewer.tileLoader.createTileLoaderJob(mapSource,
					tilex, tiley, zoom));
		}
		return tile;
	}

}
