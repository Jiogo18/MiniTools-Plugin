package fr.jarven.minitools.inventory;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

import fr.jarven.minitools.commands.CommandGive;

public class PageData implements ConfigurationSerializable {
	public boolean locked;
	public ItemStack[] items;

	private PageData() {}

	public PageData(Holder h) {
		this.items = h.getUsableInventory();
		this.locked = h.isLocked();
	}

	public void apply(Holder h) {
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
		PageData d = new PageData();

		d.locked = (boolean) map.get("locked");
		@SuppressWarnings("unchecked")
		List<Object> listOfItems = (List<Object>) map.get("items");
		d.items = listOfItems != null ? listOfItems.stream().map(i -> CommandGive.loadItemStack(i)).toArray(ItemStack[] ::new) : null;

		return d;
	}
}
