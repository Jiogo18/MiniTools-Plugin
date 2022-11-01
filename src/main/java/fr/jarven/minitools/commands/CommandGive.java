package fr.jarven.minitools.commands;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.minitools.Main;

// Just a basic give with items stored in a config file
public class CommandGive extends Base {
	private static final String GIVE_FILE = "give.yml";
	private static YamlConfiguration giveConfig = null;
	private static Set<String> customItemNames = new HashSet<>();
	private static boolean vanillaItems = false;
	private static String defaultPermission = "";

	public static void onLoad() {
		File file = new File(Main.getInstance().getDataFolder(), GIVE_FILE);
		if (!file.exists()) {
			Main.getInstance().saveResource(GIVE_FILE, false);
		}

		giveConfig = YamlConfiguration.loadConfiguration(file);

		ConfigurationSection custom_items = giveConfig.getConfigurationSection("custom_give");
		customItemNames.clear();
		if (custom_items != null) {
			Set<String> customItemsKeys = custom_items.getKeys(false);
			customItemNames.addAll(customItemsKeys);
			Main.LOGGER.info("Give loaded: " + customItemNames.size() + " custom items.");
		}

		vanillaItems = giveConfig.getBoolean("vanilla_items", false);
		defaultPermission = giveConfig.getString("default_permission", "minitools.give.default");
	}

	private static ConfigurationSection getCustomItem(String name) {
		if (!customItemNames.contains(name)) return null;
		return giveConfig.getConfigurationSection("custom_give." + name);
	}

	private static boolean hasPermission(CommandSender player, ConfigurationSection custom_item) {
		String permission = custom_item.getString("permission", defaultPermission);
		if (permission == null || permission.isEmpty()) {
			permission = defaultPermission;
		}
		return player.hasPermission(permission);
	}

	private static boolean isVanillaItem(String name) {
		return vanillaItems && Material.getMaterial(name.toUpperCase()) != null;
	}

	private static int giveVanillaItem(Player player, String name) {
		player.getInventory().addItem(new ItemStack(Material.getMaterial(name.toUpperCase())));
		return 1;
	}

	private static Enchantment findEnchantment(String name) {
		Enchantment enchantment = Enchantment.getByKey(new NamespacedKey(Main.getInstance(), name));
		if (enchantment != null) return enchantment; // Found it in Enchantment (i.e. DAMAGE_ALL)
		enchantment = Arrays.asList(Enchantment.values()).stream().filter(e -> e.getKey().getKey().equalsIgnoreCase(name)).findFirst().orElse(null);
		if (enchantment != null) return enchantment; // Found it in EnchantmentWrapper (i.e. SHARPNESS)
		return null;
	}

	private static PotionEffectType findPotionEffectType(Object object) {
		@SuppressWarnings("deprecation")
		PotionEffectType type = object instanceof Integer ? PotionEffectType.getById((Integer) object) : null;
		if (type != null) return type; // Found it with type id

		if (object instanceof String) {
			String string = (String) object;

			@SuppressWarnings("deprecation")
			PotionEffectType type2 = Integer.getInteger(string) != null ? PotionEffectType.getById(Integer.getInteger(string)) : null;
			if (type2 != null) return type2; // Found it with type id
			type = PotionEffectType.getByName(string);
			if (type != null) return type; // Found it in PotionEffectType (i.e. DAMAGE_RESISTANCE)

			// Not found, try with MobEffects (not in Spigot-api)
			switch (string.toLowerCase()) {
				case "faster_movement":
					return PotionEffectType.SPEED;
				case "slowness":
				case "slower_movement":
					return PotionEffectType.SLOW;
				case "haste":
				case "faster_digging":
					return PotionEffectType.FAST_DIGGING;
				case "mining_fatigue":
				case "slower_digging":
					return PotionEffectType.SLOW_DIGGING;
				case "strength":
					return PotionEffectType.INCREASE_DAMAGE;
				case "instant_health":
					return PotionEffectType.HEAL;
				case "instant_damage":
					return PotionEffectType.HARM;
				case "jump_boost":
					return PotionEffectType.JUMP;
				case "nausea":
					return PotionEffectType.CONFUSION;
				case "resistance":
					return PotionEffectType.DAMAGE_RESISTANCE;
				case "absorbtion": // absorPtion and absorBtion...
					return PotionEffectType.ABSORPTION;
			}
		}

		return null;
	}

	// This item is used locally to have a non null ItemStack, you will never have it.
	// It is replaced by null in loadItems() if keepEmptyItems is true, else it is removed
	private static ItemStack createAIRDONTUSE() {
		ItemStack item = new ItemStack(Material.STONE); // AIR can't be given
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("AIR_DONT_USE");
		meta.setCustomModelData(1000000000);
		item.setItemMeta(meta);
		return item;
	}

	private static ItemStack loadCustomItemStack(Map<String, Object> args) {
		String materialStr = (String) args.get("material");
		if (materialStr == null) {
			Main.LOGGER.warning("[Give] Material of item is null (set EMPTY if you want to give nothing)");
			return null;
		}
		if (materialStr.equals("EMPTY")) return createAIRDONTUSE();
		Material material = Material.getMaterial((String) args.get("material"));
		if (material == null) {
			Main.LOGGER.warning("[Give] Material of item is not valid: " + materialStr);
			return null;
		}
		if (material == Material.AIR) return createAIRDONTUSE();
		ItemStack item = new ItemStack(material);
		item.setAmount((int) args.getOrDefault("amount", 1));

		if (args.containsKey("enchants")) {
			@SuppressWarnings("unchecked")
			Map<String, Integer> enchantsStr = (Map<String, Integer>) args.get("enchants");
			Map<Enchantment, Integer> enchants = new HashMap<>();

			try {
				for (Map.Entry<String, Integer> entry : enchantsStr.entrySet()) {
					Enchantment enchantment = findEnchantment(entry.getKey());
					if (enchantment == null) {
						Main.LOGGER.warning("[Give] Enchantment of item is not valid: " + entry.getKey());
						continue;
					}
					enchants.put(enchantment, entry.getValue());
				}

			} catch (Exception e) {
				Main.LOGGER.warning("[Give] Error while loading enchants of item: " + e.getMessage());
				return null;
			}

			item.addUnsafeEnchantments(enchants);
		}

		ItemMeta itemMeta = item.getItemMeta();

		if (itemMeta instanceof Damageable) {
			Damageable damageable = (Damageable) itemMeta;
			if (args.containsKey("durability")) {
				int durability = (int) args.get("durability");
				int damage = material.getMaxDurability() - durability; // full dura - dura = damage
				if (damage < 0) damage = 0;
				damageable.setDamage(damage);
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
			Object loreObj = args.get("lore");
			if (loreObj instanceof List) {
				@SuppressWarnings("unchecked")
				List<String> lore = (List<String>) loreObj;
				itemMeta.setLore(lore);
			} else if (loreObj instanceof String) {
				itemMeta.setLore(Arrays.asList(((String) loreObj).split("\n")));
			} else {
				Main.LOGGER.warning("[Give] Error while loading lore of item: " + loreObj.getClass().getName());
				return null;
			}
		}

		item.setItemMeta(itemMeta);
		return item;
	}

	public static ItemStack loadItemStack(Object object) {
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

	public static List<ItemStack> loadItems(Object objectItems, boolean keepEmptyItems) {
		List<ItemStack> items = new ArrayList<>();
		if (objectItems instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) objectItems;
			for (Object item : list) {
				ItemStack itemStack = loadItemStack(item);
				if (itemStack != null) {
					ItemMeta meta = itemStack.getItemMeta();
					if (itemStack.getType() == Material.STONE && meta.getCustomModelData() == 1000000000 && meta.getDisplayName().equals("AIR_DONT_USE")) {
						if (keepEmptyItems) items.add(null); // skip this item (AIR)
						continue;
					}
					items.add(itemStack);
				} else {
					Main.LOGGER.warning("Invalid item stack: " + objectItems.getClass() + " " + objectItems);
				}
			}
		}
		return items;
	}

	public static List<PotionEffect> loadPotionEffects(Object objectEffects) {
		List<PotionEffect> effects = new ArrayList<>();
		if (objectEffects instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) objectEffects;
			for (Object effect : list) {
				if (effect instanceof LinkedHashMap) {
					@SuppressWarnings("unchecked")
					LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) effect;
					PotionEffectType type = findPotionEffectType(map.get("type"));
					if (type == null) {
						Main.LOGGER.warning("[Give] Invalid potion effect type: " + map.get("type"));
						continue;
					}
					int duration = (int) map.getOrDefault("duration", 1);
					int amplifier = (int) map.getOrDefault("amplifier", 0);
					effects.add(new PotionEffect(type, duration, amplifier));
				} else {
					Main.LOGGER.warning("[Give] Invalid potion effect: " + effect.getClass() + " " + effect);
				}
			}
		}
		return effects;
	}

	private static int giveCustomItem(Player sender, ConfigurationSection custom_item) {
		if (custom_item.contains("items") && custom_item.getList("items") != null) {
			List<ItemStack> items = loadItems(custom_item.getList("items"), false);
			for (ItemStack item : items) {
				sender.getInventory().addItem(item);
			}
		}

		if (custom_item.contains("effects") && custom_item.getList("effects") != null) {
			List<PotionEffect> effects = loadPotionEffects(custom_item.getList("effects"));
			for (PotionEffect effect : effects) {
				sender.addPotionEffect(effect);
			}
		}

		if (custom_item.contains("commands") && custom_item.getList("commands") != null) {
			List<String> commands = custom_item.getStringList("commands");
			for (String command : commands) {
				sender.performCommand(command
							      .replace("%player%", sender.getName())
							      .replace("%uuid%", sender.getUniqueId().toString())
							      .replace("%playerDisplayName%", sender.getDisplayName()));
			}
		}

		return 1;
	}

	private static ArgumentSuggestions suggestItems = (info, builder) -> {
		String current = info.currentArg().toLowerCase();
		// Suggest items starting with the current argument
		long nbAdded =
			customItemNames
				.stream()
				.filter(item -> item.toLowerCase().startsWith(current))
				.map(item -> builder.suggest(item))
				.count();
		if (vanillaItems) {
			// Suggest items starting with the current argument
			nbAdded +=
				Stream.of(Material.values())
					.filter(material -> material.name().toLowerCase().startsWith(current))
					.map(material -> builder.suggest(material.name()))
					.count();
		}
		if (nbAdded == 0) {
			// Suggest items containing the current argument
			customItemNames
				.stream()
				.filter(item -> item.toLowerCase().contains(current))
				.forEach(item -> builder.suggest(item));
			if (vanillaItems) {
				// Suggest items containing the current argument
				Stream.of(Material.values())
					.filter(material -> material.name().toLowerCase().contains(current))
					.forEach(material -> builder.suggest(material.name()));
			}
		}
		return builder.buildFuture();
	};

	private static void executeWithPuppetPlayer(NativeProxyCommandSender proxy, Object[] args) {
		Player puppet = (Player) proxy.getCallee();
		boolean areCallerCalleeTheSame = areCallerCalleeTheSame(proxy);
		String itemName = (String) args[0];
		ConfigurationSection custom_item = getCustomItem(itemName);
		if (custom_item == null) {
			if (isVanillaItem(itemName)) {
				giveVanillaItem(puppet, itemName);
				if (!areCallerCalleeTheSame) {
					proxy.sendMessage(puppet.getName() + " received " + itemName + ".");
				}
			} else {
				proxy.sendMessage("§cThis item doesn't exist");
			}
			return;
		}
		if (!hasPermission(proxy, custom_item)) {
			proxy.sendMessage("§cYou don't have the permission to give this item");
		} else {
			if (custom_item.contains("enabled") && !custom_item.getBoolean("enabled")) {
				proxy.sendMessage("§cThis item is disabled");
				return;
			}
			giveCustomItem(puppet, custom_item);
			if (!areCallerCalleeTheSame) {
				proxy.sendMessage(puppet.getName() + " received the custom item " + itemName + ".");
			}
		}
	}

	public static ArgumentTree getSubCommand() {
		return literal("give")
			.then(executePlayerProxy(new StringArgument("item").includeSuggestions(suggestItems), CommandGive::executeWithPuppetPlayer));
	}
}
