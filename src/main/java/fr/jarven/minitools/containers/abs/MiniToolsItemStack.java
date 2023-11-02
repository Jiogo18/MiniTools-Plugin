package fr.jarven.minitools.containers.abs;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public abstract class MiniToolsItemStack {
	private final ItemStack itemStack;

	protected MiniToolsItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
		assert itemStack != null;
	}

	public ItemStack getItemStack() {
		return this.itemStack;
	}

	public abstract void handleClickEvent(InventoryClickEvent event, MiniToolsHolder holder);
	public abstract void handleDragEvent(InventoryDragEvent event, MiniToolsHolder holder);
}
