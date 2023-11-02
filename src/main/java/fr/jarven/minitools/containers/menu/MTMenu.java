package fr.jarven.minitools.containers.menu;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.jarven.minitools.Main;
import fr.jarven.minitools.containers.abs.MiniToolsHolder;

public class MTMenu extends MiniToolsHolder {
	private static MTMenu miniToolsMenu = null;

	public MTMenu(String title, int size, MTMenuItem[] menuItems) {
		super(size, title, menuItems);
	}

	public static MTMenu fromConfig(ConfigurationSection config) {
		boolean enabled = config.getBoolean("enabled");
		if (!enabled) {
			return null;
		}
		String title = config.getString("title");
		int size = config.getInt("size");
		MTMenuItem[] items = new MTMenuItem[size];

		for (int i = 0; i < size; i++) {
			items[i] = MTMenuItem.fromConfig(config.getConfigurationSection("items." + i));
		}

		return new MTMenu(title, size, items);
	}

	public static boolean openMenu(Player player) {
		if (miniToolsMenu == null) {
			Main.LOGGER.info("Loading menu from config");
			miniToolsMenu = fromConfig(Main.getInstance().getConfig().getConfigurationSection("menu"));
			if (miniToolsMenu == null) {
				return false;
			}
		}
		miniToolsMenu.openInventory(player);
		return true;
	}

	public static void onLoad() {
		miniToolsMenu = null; // Reload when open next time
	}
}
