package pistonmc.techtree.mc7.data;

import java.io.File;
import java.nio.file.Path;

import pistonmc.techtree.adapter.IConfigHost;

public class ConfigHost implements IConfigHost {

	@Override
	public Path getConfigRoot() {
		return new File("").getAbsoluteFile().toPath().resolve("config").resolve("TechTree");
	}
	
}
