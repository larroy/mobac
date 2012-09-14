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
package mobac.gui.actions;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.xml.bind.JAXBException;

import mobac.data.gpx.GPXUtils;
import mobac.data.gpx.gpx11.Gpx;
import mobac.gui.MainGUI;
import mobac.gui.mapview.layer.GpxLayer;
import mobac.gui.panels.JGpxPanel;
import mobac.program.model.Settings;
import mobac.utilities.file.GpxFileFilter;

import org.apache.log4j.Logger;

public class GpxLoad implements ActionListener {

	private Logger log = Logger.getLogger(GpxLoad.class);

	JGpxPanel panel;

	public GpxLoad(JGpxPanel panel) {
		super();
		this.panel = panel;
	}

	public void actionPerformed(ActionEvent event) {
		if (!GPXUtils.checkJAXBVersion())
			return;
		JFileChooser fc = new JFileChooser();
		try {
			File dir = new File(Settings.getInstance().gpxFileChooserDir);
			fc.setCurrentDirectory(dir); // restore the saved directory
		} catch (Exception e) {
		}
		fc.setMultiSelectionEnabled(true);
		fc.addChoosableFileFilter(new GpxFileFilter(false));
		final MainGUI mainGUI = MainGUI.getMainGUI();
		int returnVal = fc.showOpenDialog(mainGUI);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;
		Settings.getInstance().gpxFileChooserDir = fc.getCurrentDirectory().getAbsolutePath();

		File[] f = fc.getSelectedFiles();
		if (f.length > 1) {
			doMultiLoad(f, mainGUI);
		} else if (f.length == 1) {
			doLoad(f[0], mainGUI);
		}
	}

	/**
	 * @param f
	 */
	private void doLoad(File f, Component parent) {
		if (panel.isFileOpen(f.getAbsolutePath())) {
			int answer = JOptionPane.showConfirmDialog(parent, "The file <" + f.getName() + "> was already opened.\n"
					+ "Do you want to open a new instance of this file?", "Warning", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (answer != JOptionPane.YES_OPTION)
				return;
		}
		loadGpxFile(f, parent);
	}

	private void loadGpxFile(File f, Component parent) {
		try {
			Gpx gpx = GPXUtils.loadGpxFile(f);
			GpxLayer gpxLayer = new GpxLayer(gpx);
			gpxLayer.setFile(f);
			panel.addGpxLayer(gpxLayer);
		} catch (JAXBException e) {
			JOptionPane.showMessageDialog(parent, "<html>Unable to load the GPX file <br><i>" + f.getAbsolutePath()
					+ "</i><br><br><b>Please make sure the file is a valid GPX v1.1 file.</b><br>"
					+ "<br>Internal error message:<br>" + e.getMessage() + "</html>", "GPX loading failed",
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(e);
		}
	}

	private void doMultiLoad(final File[] files, final MainGUI mainGUI) {
		final JDialog progressDialog = new JDialog(mainGUI);
		// prepare progress dialog
		progressDialog.setSize(400, 50);
		progressDialog.setResizable(false);
		progressDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		progressDialog.setLocation(
				Math.max(0, (int) (mainGUI.getLocation().getX() + mainGUI.getSize().getWidth() / 2 - 200)),
				Math.max(0, (int) (mainGUI.getLocation().getY() + mainGUI.getSize().getHeight() / 2 - 25)));
		final JProgressBar progressBar = new JProgressBar(0, files.length);
		progressDialog.add(progressBar);

		mainGUI.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		mainGUI.setEnabled(false);
		progressDialog.setVisible(true);

		for (final File file : files) {
			LinkedList<File> openFiles = new LinkedList<File>();
			if (panel.isFileOpen(file.getAbsolutePath())) {
				openFiles.add(file);
			}
			if (openFiles.size() > 0) {
				int answer = JOptionPane.showConfirmDialog(mainGUI, "One or more files are already opened.\n"
						+ "Do you want to open a new instance of those files?", "Warning", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (answer != JOptionPane.YES_OPTION)
					return;
			}
		}

		Thread job = new Thread() {

			private int counter = 0;

			public void run() {
				try {
					// iterate over files to load
					for (final File file : files) {
						counter++;
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								progressBar.setValue(counter);
								progressDialog.setTitle("Processing " + counter + " of " + files.length + " <"
										+ file.getName() + ">");
							}
						});
						loadGpxFile(file, mainGUI);
					}
				} catch (RuntimeException e) {
					log.error(e.getMessage(), e);
				} finally {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							// close progress dialog
							mainGUI.previewMap.repaint();
							mainGUI.setCursor(Cursor.getDefaultCursor());
							if (progressDialog != null) {
								progressDialog.setVisible(false);
								progressDialog.dispose();
							}
							mainGUI.setEnabled(true);
							mainGUI.toFront();
						}
					});
				}
			};
		};

		job.start();
	}
}
