package fr.jarven.minitools.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.minitools.inventory.InventoryMenu;

// Command for a permanent "inventory" (a shared enderchest) to store custom items
public class CommandInventory extends Base {
	public static InventoryMenu inventoryMenu;

	public static ArgumentTree getSubCommand() {
		ArgumentSuggestions suggestExistingPage = (info, builder) -> {
			String current = info.currentArg();
			for (int i = 0; i < inventoryMenu.getPageCount(); i++) {
				if (Integer.toString(i + 1).startsWith(current)) {
					builder.suggest(i + 1);
				}
			}
			return builder.buildFuture();
		};
		ArgumentSuggestions suggestNextPage = (info, builder) -> {
			return builder.suggest(inventoryMenu.getPageCount() + 1).buildFuture();
		};

		return executeHumanProxy(
			literal("inventory")
				.then(executeHumanProxy(new IntegerArgument("page").includeSuggestions(suggestExistingPage), (proxy, args) -> openInventory(proxy, (int) args[0])))
				.then(literal("add_page")
						.then(new IntegerArgument("page").includeSuggestions(suggestNextPage).executes((sender, args) -> { return addPage(sender, (int) args[0]); }))
						.executes((sender, args) -> { return addPage(sender, inventoryMenu.getPageCount() + 1); }))
				.then(literal("remove_page")
						.then(new IntegerArgument("page").includeSuggestions(suggestExistingPage).executes((sender, args) -> { return removePage(sender, (int) args[0]); }))
						.executes((sender, args) -> { return removePage(sender, inventoryMenu.getPageCount()); })),
			(proxy, args) -> openInventory(proxy, 1));
	}

	public static void openInventory(NativeProxyCommandSender proxy, int page) {
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

	public static int addPage(CommandSender sender, int page) {
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

	public static int removePage(CommandSender sender, int page) {
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
}
