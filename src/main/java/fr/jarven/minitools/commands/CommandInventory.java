package fr.jarven.minitools.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.minitools.inventory.Holder;
import fr.jarven.minitools.inventory.InventoryMenu;

// Command for a permanent "inventory" (a shared enderchest) to store custom items
public class CommandInventory extends Base {
	private static InventoryMenu inventoryMenu;

	public Argument<String> getSubCommand() {
		ArgumentSuggestions<CommandSender> suggestExistingPage = (info, builder) -> {
			String current = info.currentArg();
			for (int i = 0; i < inventoryMenu.getPageCount(); i++) {
				if (Integer.toString(i + 1).startsWith(current)) {
					builder.suggest(i + 1);
				}
			}
			return builder.buildFuture();
		};
		ArgumentSuggestions<CommandSender> suggestNextPage = (info, builder) -> {
			return builder.suggest(inventoryMenu.getPageCount() + 1).buildFuture();
		};

		return executeHumanProxy(
			literal("inventory")
				.then(executeHumanProxy(
					new IntegerArgument("page").includeSuggestions(suggestExistingPage),
					CommandInventory::openInventory))
				.then(literal("add_page")
						.then(
							new IntegerArgument("page")
								.includeSuggestions(suggestNextPage)
								.executes(CommandInventory::addPage))
						.executes(CommandInventory::addPage))
				.then(literal("remove_page")
						.then(new IntegerArgument("page")
								.includeSuggestions(suggestExistingPage)
								.executes(CommandInventory::removePage))
						.executes(CommandInventory::removePage)),
			CommandInventory::openInventory);
	}

	public static void openInventory(NativeProxyCommandSender proxy, CommandArguments args) {
		int page = (int) args.getOptional("page").orElse(1);
		HumanEntity player = (HumanEntity) proxy.getCallee();
		boolean open = inventoryMenu.open(player, page);
		if (!areCallerCalleeTheSame(proxy)) {
			if (open) {
				proxy.sendMessage("Inventaire ouvert pour " + player.getName());
			} else {
				proxy.sendMessage("Impossible d'ouvrir l'inventaire pour " + player.getName());
			}
		} else if (!open) {
			if (page < 1 || inventoryMenu.getPageCount() < page) {
				proxy.sendMessage("Page " + page + " inexistante");
			} else {
				proxy.sendMessage("Impossible d'ouvrir l'inventaire");
			}
		}
	}

	public static boolean openInventory(HumanEntity player, int page) {
		return inventoryMenu.open(player, page);
	}

	public static int addPage(CommandSender sender, CommandArguments args) {
		int page = (int) args.getOptional("page").orElse(inventoryMenu.getPageCount() + 1);
		page = Math.max(1, Math.min(page, inventoryMenu.getPageCount() + 1));
		int index = page - 1;
		boolean insert = index < inventoryMenu.getPageCount();
		boolean added = inventoryMenu.addPage(index);
		int totalCount = inventoryMenu.getPageCount();
		if (added) {
			if (insert) {
				sender.sendMessage("Page " + page + " insérée à la place " + (index + 1));
			} else {
				sender.sendMessage("Page " + page + " ajoutée");
			}
			return 1;
		} else {
			if (insert) {
				sender.sendMessage("Impossible d'insérer la page " + page + "(total: " + totalCount + ")");
			} else {
				sender.sendMessage("Impossible d'ajouter la page " + page + "(total: " + totalCount + ")");
			}
			return 0;
		}
	}

	public static int removePage(CommandSender sender, CommandArguments args) {
		int page = (int) args.getOptional("page").orElse(inventoryMenu.getPageCount());
		if (page < 1 || page > inventoryMenu.getPageCount()) {
			sender.sendMessage("Cette page n'existe pas");
			return 0;
		}
		int index = page - 1;
		boolean added = inventoryMenu.removePage(index);
		int totalCount = inventoryMenu.getPageCount();
		if (added) {
			sender.sendMessage("Page " + page + " supprimée (total: " + totalCount + ")");
			return 1;
		} else {
			sender.sendMessage("Impossible de supprimer la page " + page + " (total: " + totalCount + ")");
			return 0;
		}
	}

	public static void onLoad() {
		if (inventoryMenu == null) inventoryMenu = new InventoryMenu();
		inventoryMenu.load();
	}

	public static void onDisable() {
		inventoryMenu.onDisable();
	}

	public static InventoryMenu getInventoryMenu() {
		return inventoryMenu;
	}

	public static boolean isInventoryMenu(Inventory inventory) {
		return inventory.getHolder() instanceof Holder && (inventoryMenu != null && inventoryMenu.isHolder(inventory));
	}
}
