package fr.jarven.minitools.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.minitools.Main;
import fr.jarven.minitools.utils.CustomItemStack;
import fr.jarven.minitools.utils.FindPotionEffect;

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

		ConfigurationSection customItems = giveConfig.getConfigurationSection("custom_give");
		customItemNames.clear();
		if (customItems != null) {
			Set<String> customItemsKeys = customItems.getKeys(false);
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

	private static boolean hasPermission(CommandSender player, ConfigurationSection customItem) {
		String permission = customItem.getString("permission", defaultPermission);
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

	private static int giveCustomItem(Player sender, ConfigurationSection customItem) {
		if (customItem.contains("items") && customItem.getList("items") != null) {
			List<ItemStack> items = CustomItemStack.fromListObject(customItem.getList("items"), false);
			for (ItemStack item : items) {
				sender.getInventory().addItem(item);
			}
		}

		if (customItem.contains("effects") && customItem.getList("effects") != null) {
			List<PotionEffect> effects = FindPotionEffect.loadPotionEffects(customItem.getList("effects"));
			for (PotionEffect effect : effects) {
				sender.addPotionEffect(effect);
			}
		}

		if (customItem.contains("commands") && customItem.getList("commands") != null) {
			List<String> commands = customItem.getStringList("commands");
			for (String command : commands) {
				sender.performCommand(command
							      .replace("%player%", sender.getName())
							      .replace("%uuid%", sender.getUniqueId().toString())
							      .replace("%playerDisplayName%", sender.getDisplayName()));
			}
		}

		return 1;
	}

	private static ArgumentSuggestions<CommandSender> suggestItems = (info, builder) -> {
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

	private static void executeWithPuppetPlayer(NativeProxyCommandSender proxy, CommandArguments args) {
		Player puppet = (Player) proxy.getCallee();
		boolean areCallerCalleeTheSame = areCallerCalleeTheSame(proxy);
		String itemName = (String) args.get("item");
		ConfigurationSection customItem = getCustomItem(itemName);
		if (customItem == null) {
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
		if (!hasPermission(proxy, customItem)) {
			proxy.sendMessage("§cYou don't have the permission to give this item");
		} else {
			if (customItem.contains("enabled") && !customItem.getBoolean("enabled")) {
				proxy.sendMessage("§cThis item is disabled");
				return;
			}
			giveCustomItem(puppet, customItem);
			if (!areCallerCalleeTheSame) {
				proxy.sendMessage(puppet.getName() + " received the custom item " + itemName + ".");
			}
		}
	}

	public Argument<String> getSubCommand() {
		return literal("give")
			.then(executePlayerProxy(
				new StringArgument("item").includeSuggestions(suggestItems),
				CommandGive::executeWithPuppetPlayer));
	}
}
