package fr.jarven.minitools.commands;

import org.bukkit.entity.Player;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.FloatArgument;

public class CommandFly extends Base {
	public Argument<String> getSubCommand() {
		Argument<String> flySpeed = literal("speed")
						    .then(executePlayer(literal("default"), (player, args) -> player.setFlySpeed(0.1f)))
						    .then(executePlayer(literal("0"), (player, args) -> player.setFlySpeed(0.0f)))
						    .then(executePlayer(literal("0.1"), (player, args) -> player.setFlySpeed(0.1f)))
						    .then(executePlayer(literal("0.2"), (player, args) -> player.setFlySpeed(0.2f)))
						    .then(executePlayer(literal("1"), (player, args) -> player.setFlySpeed(1.0f)))
						    .then(executePlayer(new FloatArgument("speed"), (player, args) -> player.setFlySpeed((float) args.get("speed"))));
		return executePlayer(
			literal("fly")
				.then(executePlayer(literal("on"), (player, args) -> setFlying(player, true)))
				.then(executePlayer(literal("off"), (player, args) -> setFlying(player, false)))
				.then(executePlayer(literal("toggle"), (player, args) -> setFlying(player, !player.isFlying())))
				.then(executePlayerProxy(literal("info"), (proxy, args) -> {
					Player player = (Player) proxy.getCallee();
					proxy.sendMessage("Flying: " + player.isFlying() + ", Speed: " + player.getFlySpeed());
				}))
				.then(flySpeed),
			(player, args) -> setFlying(player, !player.isFlying()));
	}

	public static void setFlying(Player player, boolean flying) {
		if (flying) {
			player.teleport(player.getLocation().add(0, 0.01, 0));
		}
		player.setFlying(flying);
	}
}
