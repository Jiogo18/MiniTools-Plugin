package fr.jarven.minitools.commands;

import dev.jorel.commandapi.arguments.Argument;

public class CommandReload extends Base {
	public Argument<String> getSubCommand() {
		return literal("reload")
			.executes((sender, args) -> {
				sender.sendMessage("Reloading...");
				MyCommands.reload();
				sender.sendMessage("Reloaded!");
			});
	}
}
