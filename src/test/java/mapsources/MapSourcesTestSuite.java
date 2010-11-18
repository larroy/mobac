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
package mapsources;

import java.net.HttpURLConnection;
import java.util.HashSet;

import javax.xml.bind.JAXBException;

import junit.framework.TestSuite;
import mobac.mapsources.DefaultMapSourcesManager;
import mobac.mapsources.MapSourcesManager;
import mobac.mapsources.MapSourcesUpdater;
import mobac.program.Logging;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MultiLayerMapSource;
import mobac.program.model.EastNorthCoordinate;
import mobac.program.model.Settings;
import mobac.tools.Cities;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * {@link TestSuite} that tests every available map source for operability. The operability test consists of the
 * download of one map tile at the highest available zoom level of the map source. By default the map tile to be
 * downloaded is located in the middle of Berlin (at the coordinate of {@link #BERLIN}). As some map providers do not
 * cover Berlin for each {@link MapSource} a different test coordinate can be specified using {@link #testCoordinates}.
 * 
 */
public class MapSourcesTestSuite extends TestSuite {

	protected final Logger log;

	public static final EastNorthCoordinate C_DEFAULT = Cities.BERLIN;

	private HashSet<String> testedMapSources;

	public MapSourcesTestSuite() throws JAXBException {
		super();
		HttpURLConnection.setFollowRedirects(false);
		Logging.configureConsoleLogging();
		Logger.getRootLogger().setLevel(Level.ERROR);
		log = Logger.getLogger(MapSourcesTestSuite.class);
		testedMapSources = new HashSet<String>();
		MapSourcesUpdater.loadMapSourceProperties();
		DefaultMapSourcesManager.initialize();
		Settings.load();
		for (MapSource mapSource : MapSourcesManager.getInstance().getAllMapSources())
			if (mapSource instanceof HttpMapSource)
				addMapSourcesTestCase((HttpMapSource) mapSource);
	}

	private void addMapSourcesTestCase(HttpMapSource mapSource) {
		if (testedMapSources.contains(mapSource.getStoreName()))
			return;
		EastNorthCoordinate coordinate = Cities.getTestCoordinate(mapSource, C_DEFAULT);
		addTest(new MapSourceTestCase(mapSource, coordinate));
		testedMapSources.add(mapSource.getStoreName());
		if (mapSource instanceof MultiLayerMapSource)
			addMapSourcesTestCase((HttpMapSource)((MultiLayerMapSource) mapSource).getBackgroundMapSource());
	}

	public static TestSuite suite() throws JAXBException {
		MapSourcesTestSuite testSuite = new MapSourcesTestSuite();
		return testSuite;
	}

}
