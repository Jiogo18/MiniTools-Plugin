package fr.jarven.minitools.commands;

import org.bukkit.Bukkit;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.ChatArgument;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class CommandChatAdmin extends Base {
	public static ArgumentTree getSubCommand() {
		return literal("chat_admin")
			.then(new ChatArgument("message")
					.executes((sender, args) -> {
						// Send a message to all online op players
						BaseComponent[] base = (BaseComponent[]) args[0];
						ComponentBuilder builder = new ComponentBuilder();
						builder.append("[Chat Admin] <" + sender.getName() + "> ");
						builder.append(base);
						BaseComponent[] message = builder.create();

						Bukkit.getOnlinePlayers()
							.stream()
							.filter(player -> player.hasPermission("minitools.chat_admin"))
							.forEach(player -> player.spigot().sendMessage(message));
					}))
			.executes((sender, args) -> {
				sender.sendMessage("Envoi des messages aux admins (joueurs Op ou permission minitools.chat_admin)");
			});
	}
}
