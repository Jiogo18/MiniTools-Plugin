package fr.jarven.minitools.commands;

import org.bukkit.entity.Player;

import dev.jorel.commandapi.arguments.Argument;

public class CommandVanish extends Base {
	public Argument<String> getSubCommand() {
		return executePlayer(literal("vanish")
					     .then(executePlayer(literal("on"), (player, args) -> player.setInvisible(true)))
					     .then(executePlayer(literal("off"), (player, args) -> player.setInvisible(false)))
					     .then(executePlayer(literal("toggle"), (player, args) -> player.setInvisible(!player.isInvisible())))
					     .then(executePlayerProxy(literal("info"), (proxy, args) -> {
						     Player player = (Player) proxy.getCallee();
						     proxy.sendMessage("Vanish: " + player.isInvisible(), "Hidden: " + CommandHidden.isHidden(player));
					     })),
			(player, args) -> player.setInvisible(!player.isInvisible()));
	}
}
