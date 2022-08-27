package fr.jarven.minitools.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.RegisteredCommand;
import fr.jarven.minitools.Main;

public class MyCommands extends Base {
	private MyCommands() {
	}

	public static void registerMiniTools() {
		String fullDescription = "Plugin MiniTools par Jarven"
			+ "\nLes commandes sont utilisables avec /execute."
			+ "\n - /mt chat_admin <message> : Message privé à tous les admins"
			+ "\n - /mt fly : Commandes relatives au vol du joueur"
			+ "\n - /mt give : Commande permettant de donner un item custom"
			+ "\n - /mt gravity : Commande relatives à la gravité pour l'entitée"
			+ "\n - /mt hidden : Commande permettant de masquer complètement un joueur (items et permanent déco/reco)"
			+ "\n - /mt reload : Recharge le plugin"
			+ "\n - /mt vanish : Commande relatives au vanish du joueur"
			+ "\n - /mt whoami : Affiche votre nom"
			+ "\n - /mt world <name> : Téléporte au spawn du monde";

		new CommandTree("minitools")
			.withAliases("mt")
			.withHelp("Plugin MiniTools par Jarven", fullDescription + "\n§6Aliases : mt")
			.withRequirement((sender) -> sender.hasPermission("minitools"))
			.then(CommandChatAdmin.getSubCommand())
			.then(CommandFly.getSubCommand())
			.then(CommandGive.getSubCommand())
			.then(CommandGravity.getSubCommand())
			.then(CommandHidden.getSubCommand())
			.then(CommandReload.getSubCommand())
			.then(CommandVanish.getSubCommand())
			.then(CommandWhoAmI.getSubCommand())
			.then(CommandWorld.getSubCommand())
			.then(CommandInventory.getSubCommand())
			.executes((sender, args) -> { sender.sendMessage(fullDescription); return 1; })
			.register();

		reload();
	}

	public static void onLoad() {
		if (!CommandAPI.isLoaded())
			CommandAPI.onLoad(new CommandAPIConfig());
	}

	public static void onEnable() {
		// Enable the CommandAPI
		CommandAPI.onEnable(Main.getInstance());

		registerMiniTools();
	}

	public static void reload() {
		CommandGive.onLoad();
		CommandInventory.onLoad();
	}

	public static void onDisable() {
		CommandInventory.onDisable();

		// CommandAPI.onDisable(); // This is bad when we have 2 plugins using CommandAPI
		if (!CommandAPI.isLoaded()) {
			Main.LOGGER.severe("CommandAPI is not loaded, cannot disable. You must restart your server. (This can happen if another plugin calls CommandAPI.onDisable())");
		}
		List<RegisteredCommand> commands = CommandAPI.getRegisteredCommands();
		Set<String> commandsName = commands.stream().map(RegisteredCommand::commandName).collect(Collectors.toSet());
		Set<String> commandsAndAliasesName = commands.stream().flatMap(c -> {
									      List<String> list = new ArrayList<String>(Arrays.asList(c.aliases()));
									      list.add(c.commandName());
									      return list.stream();
								      })
							     .collect(Collectors.toSet());
		int aliasesCount = commandsAndAliasesName.size() - commandsName.size();
		Main.LOGGER.info("Unregistering " + commandsName.size() + " commands and " + aliasesCount + " aliases.");
		for (String commandName : commandsAndAliasesName) {
			CommandAPI.unregister(commandName);
		}
	}
}
