package fr.jarven.minitools.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ChatArgument;
import fr.jarven.minitools.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class CommandChatAdmin extends Base {
	private static Boolean enable_ac_command = false; // /ac message
	private static String chat_prefix = "!"; // !message
	private static String chat_send_permission = "minitools.admin_chat.sendprefix"; // Permission to send a message with the chat prefix
	private static String message_prefix = "&8[&4Admin&8] &7%player%&8: &7";
	private static String message_suffix = "";
	private static final String DESCRIPTION = "Envoi des messages aux admins (joueurs Op ou permission minitools.admin_chat.receive)";

	private static Argument<BaseComponent[]> createAdminChatArgument() {
		return new ChatArgument("message")
			.executes((sender, args) -> { return sendMessage(sender, (BaseComponent[]) args.get("message")); });
	}

	public Argument<String> getSubCommand() {
		return literal("admin_chat")
			.then(createAdminChatArgument())
			.executes((sender, args) -> { sender.sendMessage(DESCRIPTION); return 1; });
	}

	public static void onLoad() {
		ConfigurationSection admin_chat = Main.getInstance().getConfig().getConfigurationSection("admin_chat");
		boolean was_ac_command_enabled = enable_ac_command;

		if (admin_chat != null) {
			enable_ac_command = admin_chat.getBoolean("enable_ac_command", enable_ac_command);
			chat_prefix = admin_chat.getString("chat_prefix", chat_prefix);
			chat_send_permission = admin_chat.getString("chat_send_permission", chat_send_permission);
			message_prefix = admin_chat.getString("message_prefix", message_prefix);
			message_suffix = admin_chat.getString("message_suffix", message_suffix);
		}

		if (was_ac_command_enabled != enable_ac_command) {
			if (enable_ac_command) {
				new CommandTree("ac")
					.withShortDescription("Commande Admin Chat du Plugin MiniTools")
					.withRequirement((sender) -> sender.hasPermission("minitools.admin_chat.ac"))
					.then(createAdminChatArgument())
					.executes((sender, args) -> { sender.sendMessage(DESCRIPTION); return 1; })
					.register();
			}
		}
	}

	private static int sendToEveryone(BaseComponent[] message) {
		// Send a message to all online players
		int sent_count = Bukkit.getOnlinePlayers()
					 .stream()
					 .filter(player -> player.hasPermission("minitools.admin_chat.receive"))
					 .map(player -> { player.spigot().sendMessage(message); return player; })
					 .collect(java.util.stream.Collectors.toList())
					 .size();
		return sent_count;
	}

	private static int sendMessage(CommandSender sender, BaseComponent[] base) {
		// Send a message to all online op players
		ComponentBuilder builder = new ComponentBuilder();
		builder.append(ChatColor.translateAlternateColorCodes('&', message_prefix.replace("%player%", sender.getName())));
		builder.append(base);
		builder.append(ChatColor.translateAlternateColorCodes('&', message_suffix));
		return sendToEveryone(builder.create());
	}

	public static void onChat(AsyncPlayerChatEvent event) {
		String message = event.getMessage();
		if (event.getPlayer().hasPermission(chat_send_permission) && message.startsWith(chat_prefix)) {
			event.setCancelled(true);
			message = message.substring(chat_prefix.length());
			sendMessage(event.getPlayer(), new ComponentBuilder(message).create());
		}
	}
}
