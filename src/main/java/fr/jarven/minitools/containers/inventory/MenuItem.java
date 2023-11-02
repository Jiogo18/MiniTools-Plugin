package fr.jarven.minitools.containers.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

import fr.jarven.minitools.containers.abs.MiniToolsHolder;
import fr.jarven.minitools.containers.abs.MiniToolsItemStack;

public class MenuItem extends MiniToolsItemStack {
	private final Consumer<InventoryClickEvent> action;

	protected MenuItem(ItemStack itemStack, Consumer<InventoryClickEvent> action) {
		super(itemStack);
		this.action = action;
	}

	@Override
	public void handleClickEvent(InventoryClickEvent event, MiniToolsHolder holder) {
		event.setCancelled(true);
		if (action != null)
			action.accept(event);
	}

	@Override
	public void handleDragEvent(InventoryDragEvent event, MiniToolsHolder holder) {
		event.setCancelled(true);
	}
}
