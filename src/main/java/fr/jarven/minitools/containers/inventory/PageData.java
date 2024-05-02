package fr.jarven.minitools.containers.inventory;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

import fr.jarven.minitools.utils.CustomItemStack;

/**
 * Represents the data of an inventory page for IO operations only.
 * Each instance is saved in the list 'pages' of the inventory.yml file.
 */
public class PageData implements ConfigurationSerializable {
	private final boolean locked;
	private final ItemStack[] items;

	private PageData(boolean locked, ItemStack[] items) {
		this.locked = locked;
		this.items = items;
	}

	public PageData(InventoryPage h) {
		this.items = h.getUsableInventory();
		this.locked = h.isLocked();
	}

	public void apply(InventoryPage h) {
		h.setLocked(this.locked);
		h.load(this.items);
	}

	public static boolean registerSerialization() {
		return true;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new java.util.HashMap<>();
		map.put("locked", this.locked);
		map.put("items", this.items);
		return map;
	}

	public static PageData deserialize(Map<String, Object> map) {
		boolean locked = (boolean) map.get("locked");
		@SuppressWarnings("unchecked")
		List<Object> listOfItems = (List<Object>) map.get("items");
		ItemStack[] items = listOfItems != null ? listOfItems.stream().map(CustomItemStack::fromObject).toArray(ItemStack[] ::new) : null;

		return new PageData(locked, items);
	}
}
