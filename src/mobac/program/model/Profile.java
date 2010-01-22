package mobac.program.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import mobac.gui.panels.JProfilesPanel;
import mobac.program.DirectoryManager;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.AtlasObject;
import mobac.utilities.Utilities;


/**
 * A profile is a saved atlas. The available profiles ({@link Profile}
 * instances) are visible in the <code>profilesCombo</code> in the
 * {@link JProfilesPanel}.
 */
public class Profile implements Comparable<Profile> {

	public static final String PROFILE_NAME_REGEX = "[\\w _-]+";

	private static final Pattern PROFILE_FILENAME_PATTERN = Pattern.compile("mobac-profile-("
			+ PROFILE_NAME_REGEX + ").xml");

	private File file;
	private String name;
	private static Vector<Profile> profiles = new Vector<Profile>();

	public static void updateProfiles() {
		File profilesDir = DirectoryManager.currentDir;
		final Set<Profile> deletedProfiles = new HashSet<Profile>();
		deletedProfiles.addAll(profiles);
		profilesDir.list(new FilenameFilter() {

			public boolean accept(File dir, String fileName) {
				Matcher m = PROFILE_FILENAME_PATTERN.matcher(fileName);
				if (m.matches()) {
					String profileName = m.group(1);
					Profile profile = new Profile(new File(dir, fileName), profileName);
					deletedProfiles.remove(profile);
					profiles.add(profile);
				}
				return false;
			}
		});
		for (Profile p : deletedProfiles)
			profiles.remove(p);
	}

	public static Vector<Profile> getProfiles() {
		updateProfiles();
		return profiles;
	}

	public Profile(String name) {
		super();
		this.file = new File(DirectoryManager.currentDir, "mobac-profile-" + name + ".xml");
		this.name = name;
	}

	protected Profile(File file, String name) {
		super();
		this.file = file;
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public File getFile() {
		return file;
	}

	public String getName() {
		return name;
	}

	public boolean exists() {
		return file.isFile();
	}

	public void delete() {
		if (!file.delete())
			file.deleteOnExit();
	}

	public int compareTo(Profile o) {
		return file.compareTo(o.file);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Profile))
			return false;
		Profile p = (Profile) obj;
		return file.equals(p.file);
	}

	@Override
	public int hashCode() {
		assert false : "hashCode not designed";
		return -1;
	}

	public void save(AtlasInterface atlasInterface) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Atlas.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		FileOutputStream fo = null;
		try {
			fo = new FileOutputStream(file);
			m.marshal(atlasInterface, fo);
		} catch (FileNotFoundException e) {
			throw new JAXBException(e);
		} finally {
			Utilities.closeStream(fo);
		}
	}

	public AtlasInterface load() throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Atlas.class);
		Unmarshaller um = context.createUnmarshaller();
		AtlasInterface newAtlas = (AtlasInterface) um.unmarshal(file);
		return newAtlas;
	}

	public static boolean checkAtlas(AtlasInterface atlasInterface) {
		return checkAtlasObject(atlasInterface);
	}

	private static boolean checkAtlasObject(Object o) {
		boolean result = false;
		if (o instanceof AtlasObject) {
			result |= ((AtlasObject) o).checkData();
		}
		if (o instanceof Iterable<?>) {
			Iterable<?> it = (Iterable<?>) o;
			for (Object ao : it) {
				result |= checkAtlasObject(ao);
			}
		}
		return result;
	}
}
