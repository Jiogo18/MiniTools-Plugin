package fr.jarven.minitools.commands;

import dev.jorel.commandapi.ArgumentTree;

public class CommandReload extends Base {
	public static ArgumentTree getSubCommand() {
		return literal("reload")
			.executes((sender, args) -> {
				sender.sendMessage("Reloading...");
				MyCommands.reload();
				sender.sendMessage("Reloaded!");
			});
	}
}
