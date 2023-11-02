package fr.jarven.minitools.commands;

import org.bukkit.entity.Player;

import dev.jorel.commandapi.arguments.Argument;
import fr.jarven.minitools.containers.player_menu.PlayerMenu;

public class CommandPlayer extends Base {
	@Override
	public Argument<String> getSubCommand() {
		return executePlayerProxy(literal("player"),
			(proxy, args) -> {
				if (proxy.getCaller() instanceof Player) {
					openPlayerMenu((Player) proxy.getCaller(), (Player) proxy.getCallee());
				} else {
					proxy.sendMessage("Only players can use this command");
				}
			});
	}

	public static void openPlayerMenu(Player watcher, Player target) {
		PlayerMenu menu = new PlayerMenu(target);
		menu.openInventory(watcher);
	}
}
