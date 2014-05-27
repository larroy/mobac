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
package mobac.mapsources;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import mobac.exceptions.UnrecoverableDownloadException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.interfaces.FileBasedMapSource;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.MapSourceLoaderInfo;
import mobac.program.model.TileImageType;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.model.Tile;
import org.mapsforge.map.awt.AwtGraphicFactory;
import org.mapsforge.map.awt.AwtTileBitmap;
import org.mapsforge.map.layer.renderer.DatabaseRenderer;
import org.mapsforge.map.layer.renderer.RendererJob;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.reader.MapDatabase;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;

public class MapsforgeMapSource implements MapSource, FileBasedMapSource {

	private static final String name = "MapsforgeWorld";

	private MapSourceLoaderInfo loaderInfo = null;

	private MapSpace mapSpace = MercatorPower2MapSpace.INSTANCE_256;

	protected File mapFile;
	protected DatabaseRenderer renderer;
	protected XmlRenderTheme xmlRenderTheme;
	protected DisplayModel displayModel;

	public MapsforgeMapSource() {
		this("world.map");
	}

	public MapsforgeMapSource(String mapFileName) {
		mapFile = new File(mapFileName);
		displayModel = new DisplayModel();
		xmlRenderTheme = InternalRenderTheme.OSMARENDER;
	}

	@Override
	public void initialize() {
		GraphicFactory graphicFactory = AwtGraphicFactory.INSTANCE;
		MapDatabase mapDatabase = new MapDatabase();
		mapDatabase.openFile(mapFile);
		renderer = new DatabaseRenderer(mapDatabase, graphicFactory);
	}

	@Override
	public void reinitialize() {
	}

	public Color getBackgroundColor() {
		return Color.WHITE;
	}

	public MapSpace getMapSpace() {
		return mapSpace;
	}

	public int getMaxZoom() {
		return 19;
	}

	public int getMinZoom() {
		return 0;
	}

	public String getName() {
		return name;
	}

	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			UnrecoverableDownloadException, InterruptedException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream(16000);
		try {
			BufferedImage image = getTileImage(zoom, x, y, loadMethod);
			ImageIO.write(image, "png", buf);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return buf.toByteArray();
	}

	public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			UnrecoverableDownloadException, InterruptedException {
		if (mapFile == null || xmlRenderTheme == null)
			return null;
		
		Tile tile = new Tile(x, y, (byte) zoom);
		RendererJob job = new RendererJob(tile, mapFile, xmlRenderTheme, displayModel, displayModel.getScaleFactor(),
				false);

		synchronized (this) {
			AwtTileBitmap tileBitmap = (AwtTileBitmap) renderer.executeJob(job);
			return tileBitmap.getBufferedImage();
		}
	}

	public TileImageType getTileImageType() {
		return TileImageType.PNG;
	}

	public MapSourceLoaderInfo getLoaderInfo() {
		return loaderInfo;
	}

	public void setLoaderInfo(MapSourceLoaderInfo loaderInfo) {
		this.loaderInfo = loaderInfo;
	}

	@Override
	public String toString() {
		return name;
	}

}
