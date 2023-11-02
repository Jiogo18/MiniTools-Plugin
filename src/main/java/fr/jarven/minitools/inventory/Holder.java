package fr.jarven.minitools.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import fr.jarven.minitools.Main;

public class Holder implements InventoryHolder {
	private final InventoryMenu menu;
	private final int page;
	private final Inventory inventory; // the inventory displayed to the player (with the menu)
	private ItemStack[] usableInventory; // the inventory saved in the config
	private boolean locked = false; // if the inventory is locked (not editable)

	public Holder(InventoryMenu menu, int page) {
		this.menu = menu;
		this.page = page;
		this.inventory = Bukkit.createInventory(this, menu.getSize(), menu.getName() + " - Page " + page);
		// call load(usableInventory) latter
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	protected ItemStack[] getUsableInventory() {
		return usableInventory;
	}

	public void load(ItemStack[] usableInventory) {
		if (usableInventory == null) {
			usableInventory = new ItemStack[menu.getUsableSize()];
		}
		this.usableInventory = usableInventory;
		refillNow();
	}

	public void closeAll() {
		if (inventory == null) return;
		List<HumanEntity> viewers = new ArrayList<>(inventory.getViewers());
		for (HumanEntity humanEntity : viewers) {
			if (humanEntity.getOpenInventory() == null) continue;
			humanEntity.closeInventory();
		}
	}

	public void open(HumanEntity player) {
		player.openInventory(inventory);
	}

	public boolean isOpen() {
		return !inventory.getViewers().isEmpty();
	}

	public void setItem(int slot, ItemStack item, boolean byEvent) {
		if (locked || slot < 0 || slot >= menu.getUsableSize()) return;
		usableInventory[slot] = item;
		if (!byEvent) {
			inventory.setItem(slot, item);
		}
		menu.setDirty();
	}

	public int getPage() {
		return page;
	}

	public InventoryMenu getMenu() {
		return menu;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
		menu.updateInventoryMenu(this);
		menu.setDirty();
	}

	private ItemStack clone(ItemStack item) {
		if (item == null) return null;
		return item.clone();
	}

	public boolean isLoaded() {
		return usableInventory != null;
	}

	private void saveNow() {
		boolean changed = false;
		if (usableInventory == null) return; // not loaded yet

		// Clone inventory to usableInventory
		for (int i = 0; i < menu.getUsableSize(); i++) {
			if (usableInventory[i] != null && inventory.getItem(i) != null && !usableInventory[i].equals(inventory.getItem(i))
				|| usableInventory[i] != null ^ inventory.getItem(i) != null) {
				changed = true;
			}
			usableInventory[i] = clone(inventory.getItem(i));
		}

		if (changed) {
			menu.setDirty();
		}
	}

	public void saveLatter() {
		Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> saveNow(), 1L);
	}

	private void refillNow() {
		for (int i = 0; i < menu.getUsableSize(); i++) {
			inventory.setItem(i, clone(usableInventory[i]));
		}
		menu.updateInventoryMenu(this);

		for (HumanEntity viewer : inventory.getViewers()) {
			if (viewer instanceof Player) {
				((Player) viewer).updateInventory();
			}
		}
	}

	public void refillLatter() {
		// Create a task to update the inventory
		Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> refillNow(), 1L);
	}
}
