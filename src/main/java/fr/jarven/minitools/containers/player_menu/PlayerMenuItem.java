package fr.jarven.minitools.containers.player_menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import fr.jarven.minitools.containers.abs.MiniToolsHolder;
import fr.jarven.minitools.containers.abs.MiniToolsItemStack;

public class PlayerMenuItem extends MiniToolsItemStack {
	private final PlayerMenuAction action;

	protected PlayerMenuItem(PlayerMenuAction action, Player player) {
		super(action.getItemStack(player));
		this.action = action;
	}

	@Override
	public void handleClickEvent(InventoryClickEvent event, MiniToolsHolder holder) {
		event.setCancelled(true);
		if (event.getWhoClicked() instanceof Player) {
			((PlayerMenu) holder).onPlayerActionTriggered(action);
		}
	}

	@Override
	public void handleDragEvent(InventoryDragEvent event, MiniToolsHolder holder) {
		event.setCancelled(true);
	}
}
