package fr.jarven.minitools.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.StringArgument;

public class CommandWorld extends Base {
	public Argument<String> getSubCommand() {
		return literal("world")
			.then(executeEntityProxy(
				new StringArgument("world_name")
					.includeSuggestions((info, builder) -> {
						String current = info.currentArg().toLowerCase();
						for (World world : Bukkit.getWorlds()) {
							if (world.getName().toLowerCase().startsWith(current)) {
								builder.suggest(world.getName());
							}
						}
						return builder.buildFuture();
					}),
				(proxy, args) -> {
					World world = Bukkit.getWorld((String) args.get("world_name"));
					if (world == null) {
						proxy.sendMessage("§cLe monde n'existe pas !");
						return;
					}
					// Teleport the entity to the spawn of the world
					Entity entity = (Entity) proxy.getCallee();
					entity.teleport(world.getSpawnLocation());
				}))
			.executes((sender, args) -> {
				// Send the list of worlds
				StringBuilder sb = new StringBuilder();
				sb.append("Worlds:\n");
				for (World world : Bukkit.getWorlds()) {
					sb.append(" - ").append(world.getName()).append("\n");
				}
				sender.sendMessage(sb.toString());
			});
	}
}
