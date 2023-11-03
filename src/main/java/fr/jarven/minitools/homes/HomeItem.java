package fr.jarven.minitools.homes;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import fr.jarven.minitools.containers.abs.MiniToolsHolder;
import fr.jarven.minitools.containers.abs.MiniToolsItemStack;

public class HomeItem extends MiniToolsItemStack {
	private final HomePoint homePoint;

	protected HomeItem(HomePoint homePoint) {
		super(homePoint.getItemIcon());
		this.homePoint = homePoint;
	}

	@Override
	public void handleClickEvent(InventoryClickEvent event, MiniToolsHolder holder) {
		event.setCancelled(true);
		event.getWhoClicked().teleport(homePoint.getLocation());
	}

	@Override
	public void handleDragEvent(InventoryDragEvent event, MiniToolsHolder holder) {
		event.setCancelled(true);
	}
}
