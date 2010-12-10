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
package mobac.tools.urlupdater;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import mapsources.MapSourceTestCase;
import mobac.mapsources.mappacks.google.GoogleEarth;
import mobac.mapsources.mappacks.google.GoogleEarthMapsOverlay;
import mobac.mapsources.mappacks.google.GoogleMapMaker;
import mobac.mapsources.mappacks.google.GoogleMaps;
import mobac.mapsources.mappacks.google.GoogleMapsChina;
import mobac.mapsources.mappacks.google.GoogleMapsKorea;
import mobac.mapsources.mappacks.google.GoogleTerrain;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.interfaces.MapSource;
import mobac.utilities.Utilities;
import mobac.utilities.writer.NullPrintWriter;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

/**
 * Stand alone tool for updating several Google map sources. Google changes the tile url of each map source very often -
 * sometimes daily. This tool checks the map html pages and extracts the relevant URLs.
 */
public class GoogleUrlUpdater implements Runnable {

	/**
	 * <p>
	 * Recalculates the tile urls for Google Maps, Earth and Terrain and prints it to std out. Other map sources can not
	 * be updated this way.
	 * </p>
	 * 
	 * Requires <a href="http://jtidy.sourceforge.net/">JTidy</a> library.
	 */
	public static void main(String[] args) {

		// Logging.configureConsoleLogging(Level.TRACE);

		// just for initializing the MapSourcesManager
		UrlUpdater.getInstance();

		GoogleUrlUpdater g = new GoogleUrlUpdater();
		g.run();

		UrlUpdater.getInstance().writeUpdatedMapsourcesPropertiesFile();

		System.out.println("Updated map sources: " + UrlUpdater.getInstance().getUpdatedUrlsCount());
	}

	public void run() {
		testMapSource(new UpdateableMapSource("http://maps.google.com/?ie=UTF8&ll=0,0&spn=0,0&z=2", GoogleMaps.class));
		testMapSource(new UpdateableMapSource("http://maps.google.com/?ie=UTF8&t=k&ll=0,0&spn=0,0&z=2",
				GoogleEarth.class));
		testMapSource(new UpdateableMapSource("http://maps.google.com/?ie=UTF8&t=p&ll=0,0&spn=0,0&z=2",
				GoogleTerrain.class));
		testMapSource(new UpdateableMapSource("http://maps.google.com/?ie=UTF8&ll=0,0&spn=0,0&t=h&z=4",
				GoogleEarthMapsOverlay.class));

		testMapSource(new UpdateableMapSource(
				"http://maps.google.com/?ie=UTF8&ll=36.27,128.20&spn=3.126164,4.932861&z=8", GoogleMapsKorea.class,
				false) {

			@Override
			protected String processFoundUrl(String url) {
				if (url.endsWith("&") && url.indexOf("gmaptiles.co.kr") > 0)
					return url + "x=0&y=0&z=0";
				else
					return url;
			}

		});

		testMapSource(new UpdateableMapSource("", GoogleMapMaker.class) {

			@Override
			public String getUpdatedUrl(GoogleUrlUpdater g) {
				return g.getUppdatedGoogleMapMakerUrl();
			}

		});
		testMapSource(new UpdateableMapSource("", GoogleMapsChina.class) {

			@Override
			public String getUpdatedUrl(GoogleUrlUpdater g) {
				return g.getUpdateGoogleMapsChinaUrl();
			}

		});
	}

	public void testMapSource(UpdateableMapSource ums) {
		try {
			String key = ums.key;
			// KEYS.add(key);
			String oldUrlTemplate = UrlUpdater.getInstance().getMapSourceUrl(key);
			if (oldUrlTemplate == null)
				throw new RuntimeException("Url for key not found: " + key);
			String newUrlTemplate = ums.getUpdatedUrl(this);
			if (newUrlTemplate == null) {
				System.out.println(ums.mapSourceClass.getSimpleName());
				System.out.println(" failed to extract url");
			} else if (!oldUrlTemplate.equals(newUrlTemplate)) {
				try {
					System.setProperty(key, newUrlTemplate);
					MapSourceTestCase testCase = new MapSourceTestCase(ums.mapSourceClass);
					System.out.println(ums.mapSourceClass.getSimpleName());
					UrlUpdater.getInstance().updateMapSopurceUrl(key, newUrlTemplate);
					// testCase.runMapSourceTest();
					// } catch (MapSourceTestFailedException e) {
					// System.err.print("Test of new url failed: ");
					// System.err.println(key + "=" + newUrlTemplate);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			System.err.println("Failed to update " + ums);
			e.printStackTrace();
		}
	}

	public List<String> extractImgSrcList(String url) throws IOException, XPathExpressionException {
		LinkedList<String> list = new LinkedList<String>();
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();

		Tidy tidy = new Tidy();
		tidy.setErrout(new NullPrintWriter());
		Document doc = tidy.parseDOM(conn.getInputStream(), null);

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile("//img[@src]");
		Object result = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		for (int i = 0; i < nodes.getLength(); i++) {
			String imgUrl = nodes.item(i).getAttributes().getNamedItem("src").getNodeValue();
			if (imgUrl != null && imgUrl.length() > 0)
				list.add(imgUrl);
		}
		return list;
	}

	public List<String> extractUrlList(String url) throws IOException, XPathExpressionException {
		LinkedList<String> list = new LinkedList<String>();
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

		Tidy tidy = new Tidy();
		tidy.setErrout(new NullPrintWriter());
		Document doc = tidy.parseDOM(conn.getInputStream(), null);

		int len = conn.getContentLength();
		if (len <= 0)
			len = 32000;
		ByteArrayOutputStream bout = new ByteArrayOutputStream(len);

		PrintStream ps = new PrintStream(bout);
		tidy.pprint(doc, ps);
		ps.flush();
		String content = bout.toString();

		Pattern p = Pattern.compile("(http://[\\w\\\\\\./=&?;-]+)");
		Matcher m = p.matcher(content);
		while (m.find()) {
			list.add(m.group());
		}

		return list;
	}

	public String getUpdatedUrl(UpdateableMapSource ums, String serviceUrl, boolean useImgSrcUrlsOnly) {

		try {
			List<String> urls;
			if (useImgSrcUrlsOnly)
				urls = extractImgSrcList(serviceUrl);
			else
				urls = extractUrlList(serviceUrl);

			// System.out.println(urls.size());

			HashSet<UrlString> tileUrlCandidates = new HashSet<UrlString>();

			for (String imgUrl : urls) {
				try {
					// filter out images with relative path
					if (!imgUrl.toLowerCase().startsWith("http://"))
						continue;
					imgUrl = imgUrl.replaceAll("\\\\x26", "&");
					imgUrl = imgUrl.replaceAll("\\\\x3d", "=");
					imgUrl = imgUrl.replaceAll("&amp;", "&");

					// System.out.println(imgUrl);

					imgUrl = ums.processFoundUrl(imgUrl);

					URL tileUrl = new URL(imgUrl);

					String host = tileUrl.getHost();
					host = host.replaceFirst("[0-3]", "{\\$servernum}");

					String path = tileUrl.getPath();
					path = path.replaceFirst("x=\\d+", "x={\\$x}");
					path = path.replaceFirst("y=\\d+", "y={\\$y}");
					path = path.replaceFirst("z=\\d+", "z={\\$z}");

					path = path.replaceFirst("cookie=[^&]+&", "");

					if (path.equalsIgnoreCase(tileUrl.getPath()))
						continue; // Nothing was replaced

					path = path.replaceFirst("hl=[^&]+", "hl={\\$lang}");
					path = path.replaceFirst("&s=[Galieo]*", "");

					String candidate = "http://" + host + path;

					tileUrlCandidates.add(new UrlString(candidate));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (tileUrlCandidates.size() == 0)
				return null;
			if (tileUrlCandidates.size() == 1)
				return tileUrlCandidates.iterator().next().url;

			// We have more than one candidate - therefore we have to decide
			// which one to take. We compare each candidate for similarity with
			// the old url. Hopefully this will always get the right one...

			Levenshtein similarityAlgo = new Levenshtein();
			MapSource mapSource = ums.mapSourceClass.newInstance();
			String currentUrl = UrlUpdater.getInstance().getMapSourceUrl(ums.key);

			if (currentUrl == null)
				throw new RuntimeException("mapsources url not loaded: " + mapSource);

			ArrayList<UrlString> candidateList = new ArrayList<UrlString>(tileUrlCandidates);
			for (UrlString us : candidateList) {
				us.f = similarityAlgo.getSimilarity(us.url, currentUrl);
			}
			Collections.sort(candidateList);

			// System.out.println("\n" + mapSource.getStoreName() +
			// " number of possible URLs found: "
			// + candidateList.size());
			// System.out.println("ORG  " + currentUrl);
			// for (UrlString s : candidateList)
			// System.out.println(s);
			return candidateList.get(0).url;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getUppdatedGoogleMapMakerUrl() {
		try {
			HttpURLConnection c = (HttpURLConnection) new URL("http://www.google.com/mapmaker").openConnection();
			InputStream in = c.getInputStream();
			String html = new String(Utilities.getInputBytes(in));
			in.close();
			Pattern p = Pattern.compile("\\\"gwm.([\\d]+)\\\"");
			Matcher m = p.matcher(html);
			if (!m.find())
				throw new RuntimeException("pattern not found");
			String number = m.group(1);
			String url = "http://gt{$servernum}.google.com/mt/n=404&v=gwm." + number + "&x={$x}&y={$y}&z={$z}";
			c.disconnect();
			return url;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getUpdateGoogleMapsChinaUrl() {
		try {
			HttpURLConnection c = (HttpURLConnection) new URL("http://ditu.google.com/").openConnection();
			InputStream in = c.getInputStream();
			String html = new String(Utilities.getInputBytes(in));
			in.close();
			c.disconnect();

			Pattern p = Pattern.compile("\\\"(http://mt\\d.google.cn/vt/lyrs=[^\\\"]*)\\\"");
			Matcher m = p.matcher(html);
			if (!m.find())
				throw new RuntimeException("pattern not found");
			String url = m.group(1);
			url = url.replaceAll("&amp;", "&");
			url = url.replaceFirst("[0-3]", "{\\$servernum}");
			if (!url.endsWith("&"))
				url += "&";
			url = url.replaceFirst("hl=[^&]+", "hl={\\$lang}");
			url = url.replaceAll("&[xyz]=\\d+", "");
			url = url.replaceAll("&s=[^&]+", "");
			url += "x={$x}&y={$y}&z={$z}";
			return url;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static class UpdateableMapSource {
		public final String updateUrl;
		public final String key;
		public final Class<? extends HttpMapSource> mapSourceClass;
		public final boolean useImgSrcUrlsOnly;

		public UpdateableMapSource(String updateUrl, Class<? extends HttpMapSource> mapSourceClass) {
			this(updateUrl, mapSourceClass, true);
		}

		public UpdateableMapSource(String updateUrl, Class<? extends HttpMapSource> mapSourceClass,
				boolean useImgSrcUrlsOnly) {
			super();
			this.updateUrl = updateUrl;
			this.key = mapSourceClass.getSimpleName() + ".url";
			this.mapSourceClass = mapSourceClass;
			this.useImgSrcUrlsOnly = useImgSrcUrlsOnly;
		}

		public String getUpdatedUrl(GoogleUrlUpdater g) {
			return g.getUpdatedUrl(this, updateUrl, useImgSrcUrlsOnly);
		}

		protected String processFoundUrl(String url) {
			return url;
		}
	}

	public static class UrlString implements Comparable<UrlString> {
		public final String url;
		public float f;

		public UrlString(String url) {
			super();
			this.url = url;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((url == null) ? 0 : url.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UrlString other = (UrlString) obj;
			if (url == null) {
				if (other.url != null)
					return false;
			} else if (!url.equals(other.url))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return String.format("%2.2f %s", f, url);
		}

		public int compareTo(UrlString o) {
			return Float.compare(o.f, f);
		}

	}

}
