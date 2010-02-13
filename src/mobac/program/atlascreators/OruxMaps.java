package mobac.program.atlascreators;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Locale;

import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.atlascreators.impl.MapTileBuilder;
import mobac.program.atlascreators.impl.MapTileWriter;
import mobac.program.atlascreators.tileprovider.CacheTileProvider;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.model.TileImageFormat;
import mobac.program.model.TileImageParameters;
import mobac.utilities.Utilities;
import mobac.utilities.tar.TarIndex;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

/**
 * Creates maps using the OruxMaps (Android) atlas format.
 * 
 * @author orux
 */
public class OruxMaps extends AtlasCreator {

	// Calibration file extension
	protected static final String ORUXMAPS_EXT = ".otrk2.xml";

	// OruxMaps tile size
	protected static final int TILE_SIZE = 512;

	// OruxMaps background color
	protected static final Color BG_COLOR = new Color(0xcb, 0xd3, 0xf3);

	// Each layer is a Main map for OruxMaps
	protected File oruxMapsMainDir;

	// Each map is a Layer map for OruxMaps
	protected File oruxMapsLayerDir;

	// Images directory for each map
	protected File oruxMapsImagesDir;

	@Override
	public boolean testMapSource(MapSource mapSource) {

		return (mapSource.getMapSpace() instanceof MercatorPower2MapSpace);
	}

	/*
	 * @see
	 * mobac.program.atlascreators.AtlasCreator#initLayerCreation(mobac.program
	 * .interfaces.LayerInterface)
	 */
	@Override
	public void initLayerCreation(LayerInterface layer) throws IOException {

		super.initLayerCreation(layer);
		oruxMapsMainDir = new File(atlasDir, layer.getName());
		Utilities.mkDir(oruxMapsMainDir);
		writeMainOtrk2File(layer.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mobac.program.atlascreators.AtlasCreator#initializeMap(mobac.program.
	 * interfaces.MapInterface, mobac.utilities.tar.TarIndex)
	 */
	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {

		super.initializeMap(map, tarTileIndex);
		oruxMapsLayerDir = new File(oruxMapsMainDir, map.getName());
		oruxMapsImagesDir = new File(oruxMapsLayerDir, "set");
		// OruxMaps default image format, jpeg90; always TILE_SIZE=512;
		if (parameters == null)
			parameters = new TileImageParameters(TILE_SIZE, TILE_SIZE, TileImageFormat.JPEG90);
		else
			parameters = new TileImageParameters(TILE_SIZE, TILE_SIZE, parameters.getFormat());
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {

		try {
			Utilities.mkDir(oruxMapsLayerDir);
			Utilities.mkDir(oruxMapsImagesDir);
			writeOtrk2File();
			createTiles();
		} catch (InterruptedException e) {
			// User has aborted process
			return;
		} catch (Exception e) {
			throw new MapCreationException(e);
		}
	}

	protected void createTiles() throws InterruptedException, MapCreationException {

		CacheTileProvider ctp = new CacheTileProvider(mapDlTileProvider);
		try {
			mapDlTileProvider = ctp;

			OruxMapTileBuilder mapTileBuilder = new OruxMapTileBuilder(this);
			atlasProgress.initMapCreation(mapTileBuilder.getCustomTileCount());
			mapTileBuilder.createTiles();
		} finally {
			ctp.cleanup();
		}
	}

	/**
	 * Main calibration file
	 * 
	 * @param name
	 */
	private void writeMainOtrk2File(String name) {

		OutputStreamWriter writer;
		FileOutputStream otrk2FileStream = null;
		File otrk2 = new File(oruxMapsMainDir, name + ORUXMAPS_EXT);
		try {
			writer = new OutputStreamWriter(new FileOutputStream(otrk2), "UTF8");
			writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			writer.append("<OruxTracker "
					+ "xmlns:orux=\"http://oruxtracker.com/app/res/calibration\"\n"
					+ " versionCode=\"2.1\">\n");
			writer.append("<MapCalibration layers=\"true\" layerLevel=\"0\">\n");
			writer.append("<MapName><![CDATA[" + name + "]]></MapName>\n");
			writer.append("</MapCalibration>\n");
			writer.append("</OruxTracker>\n");
			writer.flush();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			Utilities.closeStream(otrk2FileStream);
		}
	}

	/**
	 * Main calibration file per layer
	 * 
	 */
	protected void writeOtrk2File() {

		FileOutputStream stream = null;
		OutputStreamWriter mapWriter;
		File otrk2File = new File(oruxMapsLayerDir, map.getName() + ORUXMAPS_EXT);
		try {
			stream = new FileOutputStream(otrk2File);
			mapWriter = new OutputStreamWriter(stream, "UTF8");
			MapSpace mapSpace = mapSource.getMapSpace();
			double longitudeMin = mapSpace.cXToLon(xMin * tileSize, zoom);
			double longitudeMax = mapSpace.cXToLon((xMax + 1) * tileSize, zoom);
			double latitudeMin = mapSpace.cYToLat((yMax + 1) * tileSize, zoom);
			double latitudeMax = mapSpace.cYToLat(yMin * tileSize, zoom);
			mapWriter.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			mapWriter.append("<OruxTracker "
					+ "xmlns:orux=\"http://oruxtracker.com/app/res/calibration\"\n"
					+ " versionCode=\"2.1\">\n");
			mapWriter.append("<MapCalibration layers=\"false\" layerLevel=\"" + map.getZoom()
					+ "\">\n");
			mapWriter.append("<MapName><![CDATA[" + map.getName() + "]]></MapName>\n");

			// convert ampersands and others
			String mapFileName = map.getName();
			mapFileName = mapFileName.replaceAll("&", "&amp;");
			mapFileName = mapFileName.replaceAll("<", "&lt;");
			mapFileName = mapFileName.replaceAll(">", "&gt;");
			mapFileName = mapFileName.replaceAll("\"", "&quot;");
			mapFileName = mapFileName.replaceAll("'", "&apos;");

			int mapWidth = (xMax - xMin + 1) * tileSize;
			int mapHeight = (yMax - yMin + 1) * tileSize;
			int numXimg = (mapWidth + TILE_SIZE - 1) / TILE_SIZE;
			int numYimg = (mapHeight + TILE_SIZE - 1) / TILE_SIZE;
			mapWriter.append("<MapChunks xMax=\"" + numXimg + "\" yMax=\"" + numYimg
					+ "\" datum=\"" + "WGS84" + "\" projection=\"" + "Mercator"
					+ "\" img_height=\"" + TILE_SIZE + "\" img_width=\"" + TILE_SIZE
					+ "\" file_name=\"" + mapFileName + "\" />\n");
			mapWriter.append("<MapDimensions height=\"" + mapHeight + "\" width=\"" + mapWidth
					+ "\" />\n");
			mapWriter.append("<MapBounds minLat=\"" + latitudeMin + "\" maxLat=\"" + latitudeMax
					+ "\" minLon=\"" + longitudeMin + "\" maxLon=\"" + longitudeMax + "\" />\n");
			mapWriter.append("<CalibrationPoints>\n");
			String cb = "<CalibrationPoint corner=\"%s\" lon=\"%2.6f\" lat=\"%2.6f\" />\n";
			mapWriter.append(String.format(Locale.ENGLISH, cb, "TL", longitudeMin, latitudeMax));
			mapWriter.append(String.format(Locale.ENGLISH, cb, "BR", longitudeMax, latitudeMin));
			mapWriter.append(String.format(Locale.ENGLISH, cb, "TR", longitudeMax, latitudeMax));
			mapWriter.append(String.format(Locale.ENGLISH, cb, "BL", longitudeMin, latitudeMin));
			mapWriter.append("</CalibrationPoints>\n");
			mapWriter.append("</MapCalibration>\n");
			mapWriter.append("</OruxTracker>\n");
			mapWriter.flush();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			Utilities.closeStream(stream);
		}
	}

	private class OruxMapTileBuilder extends MapTileBuilder {

		public OruxMapTileBuilder(AtlasCreator atlasCreator) {
			super(atlasCreator, new OruxMapTileWriter(), false);
		}

		@Override
		protected void prepareTile(Graphics2D graphics) {
			graphics.setColor(BG_COLOR);
			graphics.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
		}
		
	}

	private class OruxMapTileWriter implements MapTileWriter {

		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData)
				throws IOException {
			String tileFileName = String.format("%s_%d_%d.omc2", map.getName(), tilex, tiley);
			FileOutputStream out = new FileOutputStream(new File(oruxMapsImagesDir, tileFileName));
			try {
				out.write(tileData);
			} finally {
				Utilities.closeStream(out);
			}
		}

		public void finalizeMap() {
			// Nothing to do
		}

	}
}
