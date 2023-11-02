package fr.jarven.minitools.containers.menu;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import fr.jarven.minitools.Main;
import fr.jarven.minitools.containers.abs.MiniToolsHolder;
import fr.jarven.minitools.containers.abs.MiniToolsItemStack;
import fr.jarven.minitools.utils.CustomItemStack;

public class MTMenuItem extends MiniToolsItemStack {
	private MTMenuAction action;
	private ItemStack icon;

	public MTMenuItem(MTMenuAction action, ItemStack icon) {
		super(icon);
		this.action = action;
	}

	public MTMenuAction getAction() {
		return this.action;
	}

	public ItemStack getIcon() {
		return this.icon;
	}

	public static MTMenuItem fromConfig(ConfigurationSection config) {
		if (config == null) return null;
		MTMenuAction action = MTMenuAction.valueOf(config.getString("action"));
		ItemStack icon = CustomItemStack.fromObject(config.get("item"));
		if (icon == null) Main.LOGGER.warning("Menu item icon is null");

		return new MTMenuItem(action, icon);
	}

	@Override
	public void handleClickEvent(InventoryClickEvent event, MiniToolsHolder holder) {
		event.setCancelled(true);
		this.action.execute(event.getWhoClicked());
	}

	@Override
	public void handleDragEvent(InventoryDragEvent event, MiniToolsHolder holder) {
		event.setCancelled(true);
	}
}
