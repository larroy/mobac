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
package mobac.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import mobac.program.model.Settings;

public class I18nUtils {

	// MP: return application's resource strings
	private static ResourceBundle STRING_RESOURCE = null;

	public static String localizedStringForKey(String key) {
		if (STRING_RESOURCE == null)
			I18nUtils.updateLocalizedStringFormSettings();
		String str = null;
		try {
			str = STRING_RESOURCE.getString(key);
		} catch (Exception e) {
			str = key;
		}
		if (str == null) {
			// always return a valid string
			str = "";
		}
		return str;
	}

	public static synchronized void updateLocalizedStringFormSettings() {
		Settings settings = Settings.getInstance();
		// force to use Simplify-Chinese Locale， will update later
		Locale locale = null;
		if (settings != null) {
			locale = new Locale(settings.localeLanguage, settings.localeCountry);
		} else {
			locale = Locale.getDefault();
		}

		STRING_RESOURCE = ResourceBundle.getBundle("mobac.resources.text.localize", locale, new UTF8Control());
	}

	/**
	 * http://stackoverflow.com/questions/4659929/how-to-use-utf-8-in-resource-properties-with-resourcebundle
	 */
	public static class UTF8Control extends Control {
		public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
				boolean reload) throws IllegalAccessException, InstantiationException, IOException {
			// The below is a copy of the default implementation.
			String bundleName = toBundleName(baseName, locale);
			String resourceName = toResourceName(bundleName, "properties");
			ResourceBundle bundle = null;
			InputStream stream = null;
			if (reload) {
				URL url = loader.getResource(resourceName);
				if (url != null) {
					URLConnection connection = url.openConnection();
					if (connection != null) {
						connection.setUseCaches(false);
						stream = connection.getInputStream();
					}
				}
			} else {
				stream = loader.getResourceAsStream(resourceName);
			}
			if (stream != null) {
				try {
					// Only this line is changed to make it to read properties files as UTF-8.
					bundle = new PropertyResourceBundle(new InputStreamReader(stream, Charsets.UTF_8));
				} finally {
					stream.close();
				}
			}
			return bundle;
		}
	}
}
