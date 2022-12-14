package fr.jarven.minitools.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;

public class MyCommands extends Base {
	private MyCommands() {
	}

	public static void registerMiniTools() {
		String fullDescription = "Plugin MiniTools par Jarven"
			+ "\nLes commandes sont utilisables avec /execute."
			+ "\n - /mt admin_chat <message> : Message privé à tous les admins"
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
			.withRequirement((sender) -> sender.hasPermission("minitools.command"))
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
			.then(CommandSign.getSubCommand())
			.executes((sender, args) -> { sender.sendMessage(fullDescription); return 1; })
			.register();

		reload();
	}

	public static void onEnable() {
		registerMiniTools();
	}

	public static void reload() {
		CommandGive.onLoad();
		CommandInventory.onLoad();
		CommandChatAdmin.onLoad();
	}

	public static void onDisable() {
		CommandAPI.unregister("minitools");
		CommandAPI.unregister("mt");
		CommandAPI.unregister("ac");
	}
}
