package fr.jarven.minitools.containers.abs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MiniToolsHolder implements InventoryHolder {
	private final int size;
	private final String title;
	private Map<Integer, MiniToolsItemStack> items;
	private Inventory inventory; // the inventory displayed to the player (with the menu)

	protected MiniToolsHolder(int size, String title) {
		this.size = size;
		this.title = title;
		this.items = new HashMap<>();
		this.inventory = Bukkit.createInventory(this, size, title);
	}

	protected MiniToolsHolder(int size, String title, MiniToolsItemStack[] items) {
		this(size, title);
		for (int i = 0; i < items.length; i++) {
			if (items[i] == null) continue;
			this.items.put(i, items[i]);
			inventory.setItem(i, items[i].getItemStack());
		}
	}

	public void handleClickEvent(InventoryClickEvent event) {
		MiniToolsItemStack item = this.getMTItem(event.getCurrentItem());
		if (item != null)
			item.handleClickEvent(event, this);
	}

	public void handleDragEvent(InventoryDragEvent event) {
		MiniToolsItemStack item = this.getMTItem(event.getCursor());
		if (item != null)
			item.handleDragEvent(event, this);
	}

	public void handleCloseEvent(InventoryCloseEvent event) {
		// nothing to do
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	public int getSize() {
		return size;
	}

	public String getTitle() {
		return title;
	}

	public boolean openInventory(HumanEntity player) {
		player.openInventory(inventory);
		return true;
	}

	public void closeAll() {
		if (inventory == null) return;
		List<HumanEntity> viewers = new ArrayList<>(inventory.getViewers());
		for (HumanEntity humanEntity : viewers) {
			if (humanEntity.getOpenInventory() == null) continue;
			humanEntity.closeInventory();
		}
	}

	public boolean isOpen() {
		return !inventory.getViewers().isEmpty();
	}

	public void setItem(int slot, ItemStack item, boolean byEvent) {
		if (slot < 0 || slot >= size) return;
		if (!byEvent) {
			inventory.setItem(slot, item);
		}
	}

	public void setItem(int slot, MiniToolsItemStack item) {
		if (slot < 0 || slot >= size) return;
		inventory.setItem(slot, item.getItemStack());
		items.put(slot, item);
	}

	public void updateViewers() {
		for (HumanEntity viewer : inventory.getViewers()) {
			if (viewer instanceof Player) {
				((Player) viewer).updateInventory();
			}
		}
	}

	public MiniToolsItemStack getMTItem(ItemStack itemStack) {
		if (itemStack == null || itemStack.getType() == Material.AIR) return null;

		for (MiniToolsItemStack item : items.values()) {
			if (item.getItemStack().equals(itemStack)) return item;
		}
		return null;
	}

	public ItemStack getItem(int slot) {
		if (slot < 0 || slot >= size) return null;
		return inventory.getItem(slot);
	}
}
