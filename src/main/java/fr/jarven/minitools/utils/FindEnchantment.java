package fr.jarven.minitools.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.util.Arrays;

import fr.jarven.minitools.Main;

public class FindEnchantment {
	private FindEnchantment() {}

	public static Enchantment find(String name) {
		Enchantment enchantment = Enchantment.getByKey(new NamespacedKey(Main.getInstance(), name));
		if (enchantment != null) return enchantment; // Found it in Enchantment (i.e. DAMAGE_ALL)
		enchantment = Arrays.asList(Enchantment.values()).stream().filter(e -> e.getKey().getKey().equalsIgnoreCase(name)).findFirst().orElse(null);
		if (enchantment != null) return enchantment; // Found it in EnchantmentWrapper (i.e. SHARPNESS)
		return null;
	}
}
