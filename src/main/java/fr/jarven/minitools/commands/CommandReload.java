package fr.jarven.minitools.commands;

import dev.jorel.commandapi.arguments.Argument;
import fr.jarven.minitools.Main;

public class CommandReload extends Base {
	public Argument<String> getSubCommand() {
		return literal("reload")
			.executes((sender, args) -> {
				sender.sendMessage("Reloading...");
				Main.getInstance().reloadConfig();
				MyCommands.reload();
				sender.sendMessage("Reloaded!");
			});
	}
}
