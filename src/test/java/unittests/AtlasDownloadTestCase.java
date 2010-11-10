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
package unittests;

import java.io.File;

import mobac.mapsources.DefaultMapSourcesManager;
import mobac.program.atlascreators.TwoNavRmap;
import mobac.program.interfaces.AtlasInterface;

public class AtlasDownloadTestCase extends AbstractAtlasCreatorTestCase {

	public AtlasDownloadTestCase() {
		super();
	}

	public void testGoogleEarchOverlay() throws Exception {
		AtlasInterface atlas;
		// atlas = loadAtlas("Germany10-12");
		// atlas = loadAtlas("HamburgPark");
		DefaultMapSourcesManager.initialize();
		atlas = loadAtlas("Munich6-16");
		File dir = createAtlas(atlas, new TwoNavRmap());
		assertNotNull(dir);
	}

	// public void testGarminCustom() throws Exception {
	// AtlasInterface atlas = loadAtlas("HamburgPark");
	// File dir = createAtlas(atlas, new GarminCustom());
	// assertNotNull(dir);
	// }

}
