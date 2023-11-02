package fr.jarven.minitools.menu;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

import fr.jarven.minitools.Main;

public class MTMenu {
	private static MTMenu miniToolsMenu = null;

	private String title;
	private int size;
	private MenuItem[] menuItems;
	private List<MenuHolder> holders = new ArrayList<>();

	public MTMenu(String title, int size, MenuItem[] menuItems) {
		this.title = title;
		this.size = size;
		this.menuItems = menuItems;
	}

	public String getTitle() {
		return this.title;
	}

	public int getSize() {
		return this.size;
	}

	public MenuItem[] getMenuItems() {
		return this.menuItems;
	}

	public static MTMenu fromConfig(ConfigurationSection config) {
		boolean enabled = config.getBoolean("enabled");
		if (!enabled) {
			return null;
		}
		String title = config.getString("title");
		int size = config.getInt("size");
		MenuItem[] items = new MenuItem[size];

		for (int i = 0; i < size; i++) {
			items[i] = MenuItem.fromConfig(config.getConfigurationSection("items." + i));
		}

		return new MTMenu(title, size, items);
	}

	public void open(Player player) {
		MenuHolder holder = new MenuHolder(this);
		holders.add(holder);
		player.openInventory(holder.getInventory());
	}

	public static boolean openMenu(Player player) {
		if (miniToolsMenu == null) {
			Main.LOGGER.info("Loading menu from config");
			miniToolsMenu = fromConfig(Main.getInstance().getConfig().getConfigurationSection("menu"));
			if (miniToolsMenu == null) {
				return false;
			}
		}
		miniToolsMenu.open(player);
		return true;
	}

	public static void onLoad() {
		miniToolsMenu = null; // Reload when open next time
	}

	public static boolean isMenu(Inventory inventory) {
		return inventory.getHolder() instanceof MenuHolder;
	}

	public static boolean handleInventoryClickEvent(InventoryClickEvent event) {
		if (!isMenu(event.getInventory())) return false;
		event.setCancelled(true);

		MenuHolder holder = (MenuHolder) event.getInventory().getHolder();
		holder.onClick(event);

		return true;
	}
}
