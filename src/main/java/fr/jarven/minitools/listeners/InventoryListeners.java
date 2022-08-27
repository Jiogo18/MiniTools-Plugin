package fr.jarven.minitools.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.Set;

import fr.jarven.minitools.commands.CommandInventory;
import fr.jarven.minitools.inventory.Holder;

public class InventoryListeners implements Listener {
	private boolean isMiniToolsInventory(Inventory inventory) {
		return inventory.getHolder() instanceof Holder
			&& (CommandInventory.inventoryMenu != null && CommandInventory.inventoryMenu.isHolder(inventory));
	}

	private boolean isMenuSlot(int slot, Holder holder) {
		return holder.getMenu().getUsableSize() <= slot && slot < holder.getMenu().getSize();
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Inventory inventory = event.getInventory();
		if (inventory == null || !isMiniToolsInventory(inventory)) return;

		Holder holder = (Holder) inventory.getHolder();
		boolean isMenuSlot = isMenuSlot(event.getRawSlot(), holder);
		boolean wasCancelled = event.isCancelled();
		boolean isHolderInventory = holder.getInventory().equals(event.getClickedInventory());
		if (holder.isLocked()) event.setCancelled(true); // security

		if (isMenuSlot) {
			event.setCancelled(true);
			int menuSlot = event.getSlot() - holder.getMenu().getUsableSize();
			switch (menuSlot) {
				case 0:
					if (holder.getPage() > 1) {
						CommandInventory.inventoryMenu.open(event.getWhoClicked(), holder.getPage() - 1);
					}
					break;
				case 4:
					if (holder.isLocked()) {
						holder.setLocked(false);
					} else {
						holder.setLocked(true);
					}
					break;
				case 8:
					if (holder.getPage() < holder.getMenu().getPageCount()) {
						CommandInventory.inventoryMenu.open(event.getWhoClicked(), holder.getPage() + 1);
					}
					break;
			}
		} else {
			// 3 cases : cancel, cancel if holder, allow
			if (holder.isLocked()) {
				boolean allow = false;
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
				}
				event.setCancelled(!allow || wasCancelled); // if not allowed or was cancelled before
				if (!event.isCancelled()) {
					holder.refillLatter(); // Update
				}

			} else {
				// Do whatever you want with the item (not locked)
				holder.saveLatter();
			}
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (!isMiniToolsInventory(event.getInventory())) return;

		Holder holder = (Holder) event.getInventory().getHolder();
		boolean wasCancelled = event.isCancelled();
		if (holder.isLocked()) event.setCancelled(true); // security

		Set<Integer> slots = event.getRawSlots();
		int minSlot = -1;
		int maxSlot = -1;
		for (int slot : slots) {
			if (isMenuSlot(slot, holder)) {
				event.setCancelled(true);
				return;
			}
			if (slot < minSlot || minSlot == -1) minSlot = slot;
			if (slot > maxSlot || maxSlot == -1) maxSlot = slot;
		}

		if (holder.getMenu().getSize() <= minSlot) {
			// Only in the inventory of the player
			if (!wasCancelled) event.setCancelled(false);
		}
		holder.saveLatter();
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!isMiniToolsInventory(event.getInventory())) return;
		CommandInventory.inventoryMenu.save();
	}
}
