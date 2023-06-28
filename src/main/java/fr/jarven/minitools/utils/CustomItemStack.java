package fr.jarven.minitools.utils;

import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.jarven.minitools.Main;

public class CustomItemStack extends ItemStack {
	protected CustomItemStack(Material material) {
		super(material);
	}

	/**
	 * This item is used locally to have a non null ItemStack, you will never have it.
	 * It is replaced by null in loadItems() if keepEmptyItems is true, else it is removed.
	 * You can check if an item is this one with isAirDontUse()
	 * @return A non null ItemStack
	 */
	public static CustomItemStack createAirDontUse() {
		return new ItemStackAirDontUse();
	}

	/**
	 * Check if it is an AIRDONTUSE item
	 * @return true if it is an AIRDONTUSE item
	 */
	public boolean isAirDontUse() {
		return this instanceof ItemStackAirDontUse;
	}

	private static Material loadCustomItemStackMaterial(String materialStr) {
		if (materialStr == null) {
			Main.LOGGER.warning("[Give] Material of item is null (set EMPTY if you want to give nothing)");
			return null;
		}
		materialStr = materialStr.toUpperCase();
		if (materialStr.equals("EMPTY")) return Material.AIR;
		Material material = Material.getMaterial(materialStr);
		if (material == null) {
			Main.LOGGER.warning("[Give] Material of item is not valid: " + materialStr);
			return null;
		}
		return material;
	}

	private void setEnchants(Map<String, Integer> enchantsStr) {
		Map<Enchantment, Integer> enchants = new HashMap<>();

		try {
			for (Map.Entry<String, Integer> entry : enchantsStr.entrySet()) {
				Enchantment enchantment = FindEnchantment.find(entry.getKey());
				if (enchantment == null) {
					Main.LOGGER.warning("[Give] Enchantment of item is not valid: " + entry.getKey());
					continue;
				}
				enchants.put(enchantment, entry.getValue());
			}

		} catch (Exception e) {
			Main.LOGGER.warning("[Give] Error while loading enchants of item: " + e.getMessage());
		}

		addUnsafeEnchantments(enchants);
	}

	private static List<String> loadLore(Object loreObj) {
		if (loreObj instanceof List) {
			@SuppressWarnings("unchecked")
			List<String> lore = (List<String>) loreObj;
			return lore;
		} else if (loreObj instanceof String) {
			return Arrays.asList(((String) loreObj).split("\n"));
		} else {
			Main.LOGGER.warning("[Give] Error while loading lore of item: " + loreObj.getClass().getName());
			return null;
		}
	}

	private static ItemStack loadCustomItemStack(Map<String, Object> args) {
		String materialStr = (String) args.get("material");
		Material material = loadCustomItemStackMaterial(materialStr);
		if (material == null) return null;
		if (material.isAir()) return createAirDontUse();
		CustomItemStack item = new CustomItemStack(material);
		item.setAmount((int) args.getOrDefault("amount", 1));

		if (args.containsKey("enchants")) {
			@SuppressWarnings("unchecked")
			Map<String, Integer> enchantsStr = (Map<String, Integer>) args.get("enchants");
			item.setEnchants(enchantsStr);
		}

		ItemMeta itemMeta = item.getItemMeta();

		if (itemMeta instanceof Damageable) {
			Damageable damageable = (Damageable) itemMeta;
			if (args.containsKey("durability")) {
				int durability = (int) args.get("durability");
				int damage = material.getMaxDurability() - durability; // full dura - dura = damage
				damageable.setDamage(Math.max(0, damage));
			} else if (args.containsKey("damage")) {
				damageable.setDamage((int) args.get("damage"));
			}
		}

		if (args.containsKey("display_name") || args.containsKey("DisplayName")) {
			String displayName = (String) args.getOrDefault("display_name", args.get("DisplayName"));
			itemMeta.setDisplayName(displayName);
		}

		if (args.containsKey("custom_model_data") || args.containsKey("CustomModelData")) {
			itemMeta.setCustomModelData((int) args.getOrDefault("custom_model_data", args.get("CustomModelData")));
		}

		if (args.containsKey("lore")) {
			itemMeta.setLore(loadLore(args.get("lore")));
		}

		item.setItemMeta(itemMeta);
		return item;
	}

	public static ItemStack fromObject(Object object) {
		if (object instanceof ItemStack) {
			return (ItemStack) object;
		} else if (object instanceof LinkedHashMap) {
			@SuppressWarnings("unchecked")
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) object;
			return loadCustomItemStack(map);
		} else if (object instanceof MemorySection) {
			MemorySection section = (MemorySection) object;
			return loadCustomItemStack(section.getValues(false));
		} else {
			if (object != null) {
				Main.LOGGER.warning("[Give] Error while loading item: " + object.getClass().getName());
			}
			return null;
		}
	}

	public static List<ItemStack> fromListObject(Object objectItems, boolean keepEmptyItems) {
		List<ItemStack> items = new ArrayList<>();
		if (objectItems instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) objectItems;
			for (Object item : list) {
				ItemStack itemStack = fromObject(item);
				if (itemStack == null) {
					Main.LOGGER.warning("Invalid item stack: " + objectItems.getClass() + " " + objectItems);
					continue;
				}

				if (itemStack instanceof ItemStackAirDontUse) {
					if (keepEmptyItems) items.add(null); // keep the slot with AIR
					continue;
				}
				items.add(itemStack);
			}
		}
		return items;
	}
}
