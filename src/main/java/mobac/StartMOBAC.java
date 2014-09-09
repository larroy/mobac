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
package mobac;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import mobac.utilities.I18nUtils;

/**
 * Main class for starting Mobile Atlas Creator.
 * 
 * This class performs the Java Runtime version check and if the correct version is installed it creates a new instance
 * of the class specified by {@link #MAIN_CLASS}. The class to be instantiated is specified by it's name intentionally
 * as this allows to compile this class without any further class dependencies.
 * 
 */
public class StartMOBAC {

	public static final String MAIN_CLASS = "mobac.Main";

	public static String[] ARGS;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ARGS = args;
		setLookAndFeel();
		checkVersion();
		try {
			Class.forName(MAIN_CLASS).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, I18nUtils.localizedStringForKey("msg_environment_unable_to_start") + e.getMessage(), 
					I18nUtils.localizedStringForKey("Error"),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void setLookAndFeel() {
		try {
			if (System.getProperty("swing.defaultlaf") != null)
				return;
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			//MainGUI.setDefaultFontOfAllUIComponents(MainGUI.customFont());
		} catch (Exception e) {
		}
	}

	protected static void checkVersion() {
		String ver = System.getProperty("java.specification.version");
		if (ver == null)
			ver = "Unknown";
		String[] v = ver.split("\\.");
		int major = 0;
		int minor = 0;
		try {
			major = Integer.parseInt(v[0]);
			minor = Integer.parseInt(v[1]);
		} catch (Exception e) {
		}
		int version = (major * 1000) + minor;
		// 1.5 -> 1005; 1.6 -> 1006; 1.7 -> 1007
		if (version < 1007) {
			JOptionPane.showMessageDialog(null,
					String.format(I18nUtils.localizedStringForKey("msg_environment_jre_below"), ver), 
					I18nUtils.localizedStringForKey("msg_environment_jre_below_title"),
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

}
