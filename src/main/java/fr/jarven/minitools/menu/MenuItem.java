package fr.jarven.minitools.menu;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import fr.jarven.minitools.Main;
import fr.jarven.minitools.utils.CustomItemStack;

public class MenuItem {
	private MenuAction action;
	private ItemStack icon;

	public MenuItem(MenuAction action, ItemStack icon) {
		this.action = action;
		this.icon = icon;
	}

	public MenuAction getAction() {
		return this.action;
	}

	public ItemStack getIcon() {
		return this.icon;
	}

	public static MenuItem fromConfig(ConfigurationSection config) {
		if (config == null) return null;
		MenuAction action = MenuAction.valueOf(config.getString("action"));
		ItemStack icon = CustomItemStack.fromObject(config.get("item"));
		if (icon == null) Main.LOGGER.warning("Menu item icon is null");

		return new MenuItem(action, icon);
	}
}
