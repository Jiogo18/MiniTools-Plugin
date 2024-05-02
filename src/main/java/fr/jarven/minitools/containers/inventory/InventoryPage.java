package fr.jarven.minitools.containers.inventory;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

import fr.jarven.minitools.Main;
import fr.jarven.minitools.containers.abs.MiniToolsHolder;
import fr.jarven.minitools.containers.abs.MiniToolsItemStack;

public class InventoryPage extends MiniToolsHolder {
	private final InventoryMenu menu;
	private final int page;
	private ItemStack[] usableInventory; // the inventory saved in the config
	private boolean locked = false; // if the inventory is locked (not editable)

	public InventoryPage(InventoryMenu menu, int page) {
		super(54, menu.getName() + " - Page " + page);
		this.menu = menu;
		this.page = page;
		// call load(usableInventory) latter
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

	@Override
	public void setItem(int slot, ItemStack item, boolean byEvent) {
		if (slot < 0 || slot >= menu.getUsableSize()) return;
		usableInventory[slot] = item;
		super.setItem(slot, item, byEvent);
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

	public boolean isLoaded() {
		return usableInventory != null;
	}

	private void saveNow() {
		boolean changed = false;
		if (usableInventory == null) return; // not loaded yet

		// Clone inventory to usableInventory
		for (int i = 0; i < menu.getUsableSize(); i++) {
			ItemStack previousItem = usableInventory[i];
			ItemStack currentItem = getItem(i);

			if (currentItem == null) {
				if (previousItem != null) {
					changed = true;
					usableInventory[i] = null;
				}
			} else if (previousItem == null || !previousItem.equals(currentItem)) {
				changed = true;
				usableInventory[i] = currentItem.clone();
			}
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
			setItem(i, usableInventory[i], false);
		}
		menu.updateInventoryMenu(this);
		updateViewers();
	}

	public void refillLatter() {
		// Create a task to update the inventory
		Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> refillNow(), 1L);
	}

	@Override
	public void handleClickEvent(InventoryClickEvent event) {
		MiniToolsItemStack item = this.getMTItem(event.getCurrentItem());
		if (item != null) {
			// Menu item
			item.handleClickEvent(event, this);
			return;
		}

		boolean wasCancelled = event.isCancelled();
		boolean isHolderInventory = getInventory().equals(event.getClickedInventory());

		// 3 cases : cancel, cancel if holder, allow
		if (isLocked()) {
			boolean allow;
			switch (event.getAction()) {
				// case 1 : Cancel the event
				case UNKNOWN:
				case NOTHING:
					allow = false;
					break;

				// case 1bis : Cancel the event if in the inventory of the holder
				case PLACE_ALL:
				case PLACE_ONE:
				case PLACE_SOME:
					if (isHolderInventory) {
						allow = false;
					} else {
						allow = true;
					}
					break;

				// case 2 : Allow the event
				case CLONE_STACK:
				case DROP_ALL_CURSOR:
				case DROP_ONE_CURSOR:
				case COLLECT_TO_CURSOR: // Double click
				case DROP_ALL_SLOT:
				case DROP_ONE_SLOT:
				case HOTBAR_SWAP:
				case PICKUP_ALL:
				case PICKUP_HALF:
				case PICKUP_ONE:
				case PICKUP_SOME:
				case HOTBAR_MOVE_AND_READD:
				case MOVE_TO_OTHER_INVENTORY:
				case SWAP_WITH_CURSOR:
					allow = true;
					break;

				default:
					allow = false;
			}
			event.setCancelled(!allow || wasCancelled); // if not allowed or was cancelled before
			if (!event.isCancelled()) {
				refillLatter(); // Update
			}
		} else {
			// Do whatever you want with the item (not locked)
			saveLatter();
		}
	}

	private boolean isMenuSlot(int slot) {
		return menu.getUsableSize() <= slot && slot < menu.getSize();
	}

	@Override
	public void handleDragEvent(InventoryDragEvent event) {
		boolean wasCancelled = event.isCancelled();
		if (isLocked()) event.setCancelled(true); // security

		Set<Integer> slots = event.getRawSlots();
		int minSlot = -1;
		int maxSlot = -1;
		for (int slot : slots) {
			if (isMenuSlot(slot)) {
				event.setCancelled(true);
				return;
			}
			if (slot < minSlot || minSlot == -1) minSlot = slot;
			if (slot > maxSlot || maxSlot == -1) maxSlot = slot;
		}

		if (getMenu().getSize() <= minSlot) {
			// Only in the inventory of the player
			if (!wasCancelled) event.setCancelled(false);
		}
		saveLatter();
	}

	@Override
	public void handleCloseEvent(InventoryCloseEvent event) {
		menu.save();
	}
}
