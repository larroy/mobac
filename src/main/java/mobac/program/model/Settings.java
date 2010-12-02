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
package mobac.program.model;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import mobac.gui.actions.GpxLoad;
import mobac.gui.mapview.ScaleBar;
import mobac.gui.panels.JCoordinatesPanel;
import mobac.mapsources.MapSourcesManager;
import mobac.mapsources.custom.CustomMapSource;
import mobac.mapsources.mappacks.google.GoogleMapSource;
import mobac.program.DirectoryManager;
import mobac.program.ProgramInfo;
import mobac.program.download.UserAgent;
import mobac.utilities.Utilities;

import org.apache.log4j.Logger;

@XmlRootElement
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class Settings {

	private static Logger log = Logger.getLogger(Settings.class);
	private static Settings instance = new Settings();

	public static final File FILE = new File(DirectoryManager.currentDir, "settings.xml");

	private static long SETTINGS_LAST_MODIFIED = 0;

	private static final String SYSTEM_PROXY_HOST = System.getProperty("http.proxyHost");
	private static final String SYSTEM_PROXY_PORT = System.getProperty("http.proxyPort");

	@XmlElement(defaultValue = "")
	private String version;

	public int maxMapSize = 32767;

	public boolean tileStoreEnabled = true;

	/**
	 * Mapview related settings
	 */
	public int mapviewZoom = 3;
	public int mapviewGridZoom = -1;
	public EastNorthCoordinate mapviewCenterCoordinate = new EastNorthCoordinate(50, 9);

	public Point mapviewSelectionMax = null;
	public Point mapviewSelectionMin = null;

	@XmlElement(nillable = false)
	public String mapviewMapSource = MapSourcesManager.getInstance().getDefaultMapSource().getName();

	public String elementName = "Layer name";

	private String userAgent = UserAgent.FF3_XP;

	public int downloadThreadCount = 4;

	private boolean customTileProcessing = false;
	private Dimension tileSize = new Dimension(256, 256);
	private TileImageFormat tileImageFormat = TileImageFormat.PNG;
	private AtlasOutputFormat atlasOutputFormat = AtlasOutputFormat.TaredAtlas;

	public CoordinateStringFormat coordinateNumberFormat = CoordinateStringFormat.DEG_LOCAL;

	@XmlElement
	private String atlasOutputDirectory = null;
	public String tileStoreDirectory;

	/**
	 * Connection timeout in seconds (default 10 seconds)
	 */
	public int httpConnectionTimeout = 10;

	/**
	 * Read timeout in seconds (default 10 seconds)
	 */
	public int httpReadTimeout = 10;

	/**
	 * Maximum expiration (in milliseconds) acceptable. If a server sets an expiration time larger than this value it is
	 * truncated to this value on next download.
	 */
	public long tileMaxExpirationTime = TimeUnit.DAYS.toMillis(365);

	/**
	 * Minimum expiration (in milliseconds) acceptable. If a server sets an expiration time smaller than this value it
	 * is truncated to this value on next download.
	 */
	public long tileMinExpirationTime = TimeUnit.DAYS.toMillis(1);

	/**
	 * Expiration time (in milliseconds) of a tile if the server does not provide an expiration time
	 */
	public long tileDefaultExpirationTime = TimeUnit.DAYS.toMillis(7);

	private String googleLanguage = "en";

	/**
	 * Development mode enabled/disabled
	 * <p>
	 * In development mode one additional map source is available for using MOBAC Debug TileServer
	 * </p>
	 */
	@XmlElement
	private boolean devMode = false;

	/**
	 * Saves the last used directory of the GPX file chooser dialog. Used in {@link GpxLoad}.
	 */
	public String gpxFileChooserDir = "";

	public final MainWindowSettings mainWindow = new MainWindowSettings();

	public static class MainWindowSettings {
		public Dimension size = new Dimension();
		public Point position = new Point(-1, -1);
		public Boolean maximized = true;

		@XmlElementWrapper(name = "collapsedPanels")
		@XmlElement(name = "collapsedPanel")
		public Vector<String> collapsedPanels = new Vector<String>();
	}

	/**
	 * Network settings
	 */
	private ProxyType proxyType = ProxyType.CUSTOM;
	private String customProxyHost = "";
	private String customProxyPort = "";
	private String customProxyUserName = "";
	private String customProxyPassword = "";

	private Vector<String> disabledMapSources = new Vector<String>();

	@XmlElementWrapper(name = "customMapSources")
	@XmlElements( { @XmlElement(name = "customMapSource", type = CustomMapSource.class)
	/* @XmlElement(name = "customMultiLayerMapSource", type = CustomMultiLayerMapSource.class) */})
	public Vector<CustomMapSource> customMapSources = new Vector<CustomMapSource>();

	@XmlElement(name = "MapSourcesUpdate")
	public final MapSourcesUpdate mapSourcesUpdate = new MapSourcesUpdate();

	public static class MapSourcesUpdate {
		/**
		 * Last ETag value retrieved while online map source update.
		 * 
		 * @see MapSourcesManager#mapsourcesOnlineUpdate()
		 * @see http://en.wikipedia.org/wiki/HTTP_ETag
		 */
		public String etag;

		public Date lastUpdate;
	}

	private Settings() {
		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		mainWindow.size.width = (int) (0.9f * dScreen.width);
		mainWindow.size.height = (int) (0.9f * dScreen.height);
		mainWindow.collapsedPanels.add(JCoordinatesPanel.NAME);
		mainWindow.collapsedPanels.add("Gpx");
	}

	public static Settings getInstance() {
		return instance;
	}

	public static void load() throws JAXBException {
		try {
			JAXBContext context = JAXBContext.newInstance(Settings.class);
			Unmarshaller um = context.createUnmarshaller();
			instance = (Settings) um.unmarshal(FILE);
			SETTINGS_LAST_MODIFIED = FILE.lastModified();
		} finally {
			Settings s = getInstance();
			s.applyProxySettings();
		}
	}

	public static boolean checkSettingsFileModified() {
		if (SETTINGS_LAST_MODIFIED == 0)
			return false;
		// Check if the settings.xml has been modified
		// since it has been loaded
		long lastModified = FILE.lastModified();
		return (SETTINGS_LAST_MODIFIED != lastModified);
	}

	public static void save() throws JAXBException {
		getInstance().version = ProgramInfo.getVersion();
		JAXBContext context = JAXBContext.newInstance(Settings.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		ByteArrayOutputStream bo = null;
		FileOutputStream fo = null;
		try {
			// First we write to a buffer and if that works be write the buffer
			// to disk. Direct writing to file may result in an defect xml file
			// in case of an error
			bo = new ByteArrayOutputStream();
			m.marshal(getInstance(), bo);
			fo = new FileOutputStream(FILE);
			fo.write(bo.toByteArray());
			fo.close();
			SETTINGS_LAST_MODIFIED = FILE.lastModified();
		} catch (IOException e) {
			throw new JAXBException(e);
		} finally {
			Utilities.closeStream(fo);
		}
	}

	public static void loadOrQuit() {
		try {
			load();
		} catch (JAXBException e) {
			log.error(e);
			JOptionPane.showMessageDialog(null, "Could not read file settings.xml program will exit.", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getGoogleLanguage() {
		return googleLanguage;
	}

	public void setGoogleLanguage(String googleLanguage) {
		this.googleLanguage = googleLanguage;
		GoogleMapSource.LANG = googleLanguage;
	}

	public boolean isCustomTileSize() {
		return customTileProcessing;
	}

	public void setCustomTileSize(boolean customTileSize) {
		this.customTileProcessing = customTileSize;
	}

	public Dimension getTileSize() {
		return tileSize;
	}

	public void setTileSize(Dimension tileSize) {
		this.tileSize = tileSize;
	}

	public boolean isDevModeEnabled() {
		return devMode;
	}

	public TileImageFormat getTileImageFormat() {
		return tileImageFormat;
	}

	public void setTileImageFormat(TileImageFormat tileImageFormat) {
		this.tileImageFormat = tileImageFormat;
	}

	public AtlasOutputFormat getAtlasOutputFormat() {
		return atlasOutputFormat;
	}

	public void setAtlasOutputFormat(AtlasOutputFormat atlasOutputFormat) {
		this.atlasOutputFormat = atlasOutputFormat;
	}

	public ProxyType getProxyType() {
		return proxyType;
	}

	public void setProxyType(ProxyType proxyType) {
		this.proxyType = proxyType;
	}

	public String getCustomProxyHost() {
		return customProxyHost;
	}

	public String getCustomProxyPort() {
		return customProxyPort;
	}

	public void setCustomProxyHost(String proxyHost) {
		this.customProxyHost = proxyHost;
	}

	public void setCustomProxyPort(String proxyPort) {
		this.customProxyPort = proxyPort;
	}

	public String getCustomProxyUserName() {
		return customProxyUserName;
	}

	public void setCustomProxyUserName(String customProxyUserName) {
		this.customProxyUserName = customProxyUserName;
	}

	public String getCustomProxyPassword() {
		return customProxyPassword;
	}

	public void setCustomProxyPassword(String customProxyPassword) {
		this.customProxyPassword = customProxyPassword;
	}

	public Vector<String> getDisabledMapSources() {
		return disabledMapSources;
	}

	@XmlElementWrapper(name = "disabledMapSources")
	@XmlElement(name = "mapSource")
	public void setDisabledMapSources(Vector<String> disabledMapSources) {
		this.disabledMapSources = disabledMapSources;
	}

	public void applyProxySettings() {
		boolean useSystemProxies = false;
		String newProxyHost = null;
		String newProxyPort = null;
		Authenticator newAuthenticator = null;
		switch (proxyType) {
		case SYSTEM:
			log.info("Applying proxy configuration: system settings");
			useSystemProxies = true;
			break;
		case APP_SETTINGS:
			newProxyHost = SYSTEM_PROXY_HOST;
			newProxyPort = SYSTEM_PROXY_PORT;
			log.info("Applying proxy configuration: host=" + newProxyHost + " port=" + newProxyPort);
			break;
		case CUSTOM:
			newProxyHost = customProxyHost;
			newProxyPort = customProxyPort;
			log.info("Applying proxy configuration: host=" + newProxyHost + " port=" + newProxyPort);
			break;
		case CUSTOM_W_AUTH:
			newProxyHost = customProxyHost;
			newProxyPort = customProxyPort;
			newAuthenticator = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(customProxyUserName, customProxyPassword.toCharArray());
				}
			};
			log.info("Applying proxy configuration: host=" + newProxyHost + " port=" + newProxyPort + " user="
					+ customProxyUserName);
			break;
		}
		Utilities.setHttpProxyHost(newProxyHost);
		Utilities.setHttpProxyPort(newProxyPort);
		Authenticator.setDefault(newAuthenticator);
		System.setProperty("java.net.useSystemProxies", Boolean.toString(useSystemProxies));
	}

	@XmlElement
	public void setUnitSystem(UnitSystem unitSystem) {
		if (unitSystem == null)
			unitSystem = UnitSystem.Metric;
		ScaleBar.unitSystem = unitSystem;
	}

	public UnitSystem getUnitSystem() {
		return ScaleBar.unitSystem;
	}

	public File getAtlasOutputDirectory() {
		if (atlasOutputDirectory != null)
			return new File(atlasOutputDirectory);
		return new File(DirectoryManager.currentDir, "atlases");
	}

	public String getAtlasOutputDirectoryString() {
		if (atlasOutputDirectory == null)
			return "";
		return atlasOutputDirectory;
	}

	public String getVersion() {
		return version;
	}

	/**
	 * 
	 * @param dir
	 *            <code>null</code> or empty string resets to default directory otherwise set the new atlas output
	 *            directory.
	 */
	public void setAtlasOutputDirectory(String dir) {
		if (dir != null && dir.trim().length() == 0)
			dir = null;
		atlasOutputDirectory = dir;
	}
}
