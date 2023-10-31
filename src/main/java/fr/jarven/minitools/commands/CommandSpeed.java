package fr.jarven.minitools.commands;

import org.bukkit.entity.Player;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.FloatArgument;

public class CommandSpeed extends Base {
	public Argument<String> getSubCommand() {
		Argument<String> flySpeed = literal("fly")
						    .then(executePlayer(literal("default"), (player, args) -> player.setFlySpeed(0.1f)))
						    .then(executePlayer(new FloatArgument("speed", -10, 10).includeSuggestions(ArgumentSuggestions.strings("0", "2", "10")),
							    (player, args) -> player.setFlySpeed((float) args.get("speed") / 10)));
		Argument<String> walkSpeed = literal("walk")
						     .then(executePlayer(literal("default"), (player, args) -> player.setFlySpeed(0.2f)))
						     .then(executePlayer(new FloatArgument("speed", 0, 10).includeSuggestions(ArgumentSuggestions.strings("0", "1", "10")),
							     (player, args) -> player.setWalkSpeed((float) args.get("speed") / 10)));

		return literal("speed")
			.then(executePlayerProxy(new FloatArgument("speed", 0, 10), (proxy, args) -> {
				float speed = (float) args.get("speed");
				Player player = (Player) proxy.getCallee();
				if (player.isFlying()) {
					player.setFlySpeed(speed / 10);
					proxy.sendMessage("Fly Speed mise à " + player.getFlySpeed() * 10);
				} else {
					player.setWalkSpeed(speed / 10);
					proxy.sendMessage("Walk Speed mise à " + player.getWalkSpeed() * 10);
				}
			}))
			.then(flySpeed)
			.then(walkSpeed)
			.then(executePlayerProxy(literal("info"), (proxy, args) -> {
				Player player = (Player) proxy.getCallee();
				proxy.sendMessage("Flying: " + player.isFlying() + ", Fly Speed: " + player.getFlySpeed() * 10 + ", Walk Speed: " + player.getWalkSpeed() * 10);
			}))
			.then(executePlayerProxy(literal("default"), (proxy, args) -> {
				Player player = (Player) proxy.getCallee();
				player.setFlySpeed(0.1f);
				player.setWalkSpeed(0.2f);
				proxy.sendMessage("Fly Speed et Walk Speed remis à 1 et 2");
			}));
	}
}
