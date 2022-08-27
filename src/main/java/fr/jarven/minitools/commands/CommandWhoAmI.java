package fr.jarven.minitools.commands;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.ArgumentTree;

public class CommandWhoAmI extends Base {
	public static ArgumentTree getSubCommand() {
		return literal("whoami")
			.executesNative((proxy, args) -> {
				CommandSender puppet = proxy.getCallee();
				proxy.sendMessage("You are " + puppet.getName() + " !");
			})
			.executes((sender, args) -> {
				sender.sendMessage("You are " + sender.getName() + " !");
			});
	}
}
