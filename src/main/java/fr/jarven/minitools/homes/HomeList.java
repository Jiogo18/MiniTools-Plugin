package fr.jarven.minitools.homes;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeList {
	private List<HomePoint> homes = new ArrayList<>();

	protected HomeList() {}

	public static HomeList fromConfig(ConfigurationSection section) {
		if (section == null) return null;
		if (section.getKeys(false).isEmpty()) return null;

		HomeList homeList = new HomeList();
		for (String key : section.getKeys(false)) {
			ConfigurationSection homeSection = section.getConfigurationSection(key);
			if (homeSection == null) continue;
			HomePoint homePoint = HomePoint.fromConfig(homeSection);
			homeList.homes.add(homePoint);
		}
		return homeList;
	}

	public int size() {
		return homes.size();
	}

	public List<HomePoint> getList() {
		return Collections.unmodifiableList(homes);
	}

	public HomePoint getHome(String name, Player player) {
		return homes.stream().filter(h -> h.getName().equals(name) && h.hasConditions(player)).findFirst().orElse(null);
	}

	public HomePoint getHome(String name, CommandSender sender) {
		if (sender instanceof Player) {
			return getHome(name, (Player) sender);
		} else {
			return homes.stream().filter(h -> h.getName().equals(name)).findFirst().orElse(null);
		}
	}

	protected void addHome(HomePoint homePoint) {
		homes.add(homePoint);
	}

	public void removeHome(HomePoint homePoint) {
		homes.remove(homePoint);
	}
}
