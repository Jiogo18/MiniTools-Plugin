package fr.jarven.minitools.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import fr.jarven.minitools.containers.abs.MiniToolsHolder;

public class InventoryListeners implements Listener {
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getInventory().getHolder() instanceof MiniToolsHolder) {
			MiniToolsHolder holder = (MiniToolsHolder) event.getInventory().getHolder();
			holder.handleClickEvent(event);
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (event.getInventory().getHolder() instanceof MiniToolsHolder) {
			MiniToolsHolder holder = (MiniToolsHolder) event.getInventory().getHolder();
			holder.handleDragEvent(event);
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getInventory().getHolder() instanceof MiniToolsHolder) {
			MiniToolsHolder holder = (MiniToolsHolder) event.getInventory().getHolder();
			holder.handleCloseEvent(event);
		}
	}
}
