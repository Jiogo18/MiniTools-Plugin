package fr.jarven.minitools.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.StringArgument;

public class CommandWorld extends Base {
	public static ArgumentTree getSubCommand() {
		return literal("world")
			.then(new StringArgument("name")
					.includeSuggestions((info, builder) -> {
						String current = info.currentArg().toLowerCase();
						for (World world : Bukkit.getWorlds()) {
							if (world.getName().toLowerCase().startsWith(current)) {
								builder.suggest(world.getName());
							}
						}
						return builder.buildFuture();
					})
					.executesEntity((sender, args) -> {
						World world = Bukkit.getWorld(args[0].toString());
						if (world == null) {
							sender.sendMessage("§cLe monde n'existe pas !");
							return 0;
						}
						// Teleport the player to the spawn of the world
						sender.teleport(world.getSpawnLocation());
						return 1;
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
