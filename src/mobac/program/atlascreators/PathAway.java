package mobac.program.atlascreators;

import java.io.File;
import java.io.IOException;

import mobac.exceptions.AtlasTestException;
import mobac.mapsources.impl.Google.GoogleEarth;
import mobac.mapsources.impl.Google.GoogleMaps;
import mobac.mapsources.impl.Google.GoogleTerrain;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.utilities.tar.TarIndex;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

/**
 * Creates a tile cache structure as used by <a
 * href="http://www.pathaway.com/">PathAway</a> (for WindowsMobile, Symbian,
 * Palm)
 */
public class PathAway extends OSMTracker {

	public PathAway() {
		super();
		tileFileNamePattern = "%02X/%04X/%04X.%s";
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);

		MapSource mapSource = map.getMapSource();
		String shortMapDir = null;
		if (mapSource.getClass().equals(GoogleMaps.class))
			shortMapDir = "G1";
		else if (mapSource.getClass().equals(GoogleEarth.class))
			shortMapDir = "G2";
		else if (mapSource.getClass().equals(GoogleTerrain.class))
			shortMapDir = "G3";
		if (shortMapDir != null)
			mapDir = new File(atlasDir, shortMapDir);
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas) throws IOException, InterruptedException,
			AtlasTestException {
		super.startAtlasCreation(atlas);
		int mapCount = 0;
		for (LayerInterface layer : atlas) {
			for (MapInterface map : layer) {
				mapCount++;
				if (map.getZoom() > 17)
					throw new AtlasTestException("resolution too high - "
							+ "highest possible zoom level is 17");
			}
		}
	}

	@Override
	protected File getTileFile(int x, int y, int zoom) {
		return new File(mapDir, String.format(tileFileNamePattern, 17 - zoom, x, y, tileType));
	}

}
