package fr.jarven.minitools.homes;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fr.jarven.minitools.Main;

public class Homes {
	private static final String HOMES_FILE = "homes.yml";
	private static HomeList warpList = null;
	private static Map<UUID, HomeList> homeList = new HashMap<>();

	private Homes() {}

	private static YamlConfiguration getConfig() {
		File file = new File(Main.getInstance().getDataFolder(), HOMES_FILE);
		if (!file.exists()) {
			Main.getInstance().saveResource(HOMES_FILE, false);
		}

		return YamlConfiguration.loadConfiguration(file);
	}

	private static boolean trySave(YamlConfiguration config) {
		try {
			config.save(new File(Main.getInstance().getDataFolder(), HOMES_FILE));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void onLoad() {
		YamlConfiguration homesConfig = getConfig();

		warpList = HomeList.fromConfig(homesConfig.getConfigurationSection("warps"));
		if (warpList == null) {
			warpList = new HomeList();
		}
		Main.LOGGER.info("Loaded " + warpList.size() + " warps.");
		homeList.clear();
	}

	public static HomeList getWarps() {
		return warpList;
	}

	public static HomeList getHomes(UUID uuid) {
		if (homeList.containsKey(uuid)) {
			return homeList.get(uuid);
		}

		YamlConfiguration config = getConfig();
		ConfigurationSection section = config.getConfigurationSection("homes." + uuid.toString());
		HomeList homeList = HomeList.fromConfig(section);
		if (homeList == null) {
			homeList = new HomeList();
		}
		Homes.homeList.put(uuid, homeList);
		return homeList;
	}

	public static boolean addWarp(HomePoint homePoint) {
		YamlConfiguration config = getConfig();
		ConfigurationSection section = config.getConfigurationSection("warps");
		if (section == null) {
			section = config.createSection("warps");
		}
		HomeList warpList = Homes.getWarps();
		ConfigurationSection homeSection = section.createSection(homePoint.getKey());
		homePoint.saveTo(homeSection);
		if (!trySave(config)) return false;
		warpList.addHome(homePoint);
		return true;
	}

	public static boolean addHome(UUID uuid, HomePoint homePoint) {
		YamlConfiguration config = getConfig();
		ConfigurationSection section = config.getConfigurationSection("homes." + uuid.toString());
		if (section == null) {
			section = config.createSection("homes." + uuid.toString());
		}
		HomeList homeList = Homes.getHomes(uuid);
		ConfigurationSection homeSection = section.createSection(homePoint.getKey());
		homePoint.saveTo(homeSection);
		if (!trySave(config)) return false;
		homeList.addHome(homePoint);
		return true;
	}

	public static boolean updateWarp(HomePoint homePoint) {
		YamlConfiguration config = getConfig();
		ConfigurationSection homeSection = config.getConfigurationSection("warps." + homePoint.getKey());
		if (homeSection == null) {
			homeSection = config.createSection("warps." + homePoint.getKey());
		}
		homePoint.saveTo(homeSection);
		return trySave(config);
	}

	public static boolean updateHome(UUID uuid, HomePoint homePoint) {
		YamlConfiguration config = getConfig();
		ConfigurationSection homeSection = config.getConfigurationSection("homes." + uuid.toString() + "." + homePoint.getKey());
		if (homeSection == null) {
			homeSection = config.createSection("homes." + uuid.toString() + "." + homePoint.getKey());
		}
		homePoint.saveTo(homeSection);
		return trySave(config);
	}

	public static boolean removeWarp(HomePoint homePoint) {
		YamlConfiguration config = getConfig();
		ConfigurationSection section = config.getConfigurationSection("warps");
		if (section == null) return true;
		section.set(homePoint.getKey(), null);
		if (!trySave(config)) return false;
		warpList.removeHome(homePoint);
		return true;
	}

	public static boolean removeHome(UUID uuid, HomePoint homePoint) {
		YamlConfiguration config = getConfig();
		ConfigurationSection section = config.getConfigurationSection("homes." + uuid.toString());
		if (section == null) return true;
		section.set(homePoint.getKey(), null);
		if (!trySave(config)) return false;
		if (homeList.containsKey(uuid)) {
			homeList.get(uuid).removeHome(homePoint);
		}
		return true;
	}
}
