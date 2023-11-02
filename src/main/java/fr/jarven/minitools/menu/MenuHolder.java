package fr.jarven.minitools.menu;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Stream;

import fr.jarven.minitools.Main;

public class MenuHolder implements InventoryHolder {
	private final MTMenu menu;
	private final Inventory inventory;

	public MenuHolder(MTMenu menu) {
		this.menu = menu;

		String name = menu.getTitle();
		int size = menu.getSize();
		ItemStack[] items = new ItemStack[size];
		for (int i = 0; i < size; i++) {
			MenuItem menuItem = menu.getMenuItems()[i];
			if (menuItem == null) continue;
			items[i] = menuItem.getIcon();
		}

		this.inventory = Bukkit.createInventory(this, size, name);
		this.inventory.setContents(items);
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	public void onClick(InventoryClickEvent event) {
		ItemStack itemClicked = event.getCurrentItem();
		if (itemClicked == null) return;

		MenuItem menuItem = Stream.of(menu.getMenuItems()).filter(item -> item != null && item.getIcon().equals(itemClicked)).findAny().orElse(null);
		if (menuItem == null) return;

		menuItem.getAction().execute(event.getWhoClicked());
	}
}
