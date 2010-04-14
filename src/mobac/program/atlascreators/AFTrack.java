package mobac.program.atlascreators;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.utilities.Utilities;
import mobac.utilities.tar.TarIndex;

/**
 * AFTrack OSZ Atlas format
 */
public class AFTrack extends OSMTracker {

	private ArrayList<Integer> zoomLevel = new ArrayList<Integer>();

	private Point min;
	private Point max;

	@Override
	public void initLayerCreation(LayerInterface layer) throws IOException {
		super.initLayerCreation(layer);
		File oszFile = new File(atlasDir, layer.getName() + ".osz");
		mapTileWriter = new OszTileWriter(oszFile);
		zoomLevel.clear();
		min = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
		max = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	@Override
	public void finishLayerCreation() throws IOException {
		mapTileWriter.finalizeMap();
		mapTileWriter = null;

		super.finishLayerCreation();
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
		zoomLevel.add(new Integer(map.getZoom()));
		min.x = Math.min(min.x, map.getMinTileCoordinate().x / 256);
		min.y = Math.min(min.y, map.getMinTileCoordinate().y / 256);
		max.x = Math.max(max.x, map.getMaxTileCoordinate().x / 256);
		max.y = Math.max(max.y, map.getMaxTileCoordinate().y / 256);
	}

	private class OszTileWriter extends OSMTileWriter {

		ZipOutputStream zipStream;
		FileOutputStream out;

		public OszTileWriter(File oszFile) throws FileNotFoundException {
			super();
			out = new FileOutputStream(oszFile);
			zipStream = new ZipOutputStream(out);
		}

		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData)
				throws IOException {
			String entryName = String.format(tileFileNamePattern, zoom, tilex, tiley, tileType);
			ZipEntry entry = new ZipEntry(entryName);
			zipStream.putNextEntry(entry);
			zipStream.write(tileData);
			zipStream.closeEntry();
		}

		public void finalizeMap() throws IOException {
			ZipEntry entry = new ZipEntry("Manifest.txt");
			zipStream.putNextEntry(entry);
			OutputStreamWriter writer = new OutputStreamWriter(zipStream);

			Collections.sort(zoomLevel);
			for (Integer zoom : zoomLevel) {
				writer.append(String.format("zoom=%d\r\n",zoom.intValue()));
			}
			writer.append(String.format("minx=%d\r\n",min.x));
			writer.append(String.format("maxx=%d\r\n",max.x));
			writer.append(String.format("miny=%d\r\n",min.y));
			writer.append(String.format("maxy=%d\r\n",max.y));
			writer.flush();
			writer = null;
			zipStream.closeEntry();
			Utilities.closeStream(zipStream);
		}

	}

}
