package fr.jarven.minitools.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

import fr.jarven.minitools.Main;
import fr.jarven.minitools.commands.CommandChatAdmin;
import fr.jarven.minitools.commands.CommandHidden;

public class CommandsListeners implements Listener {
	/**
	 * On Login => Hide players if permanent
	 */
	@EventHandler
	public void onLogin(org.bukkit.event.player.PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		for (UUID playerToHideUUID : CommandHidden.hiddenPlayers) {
			Player playerToHide = Bukkit.getPlayer(playerToHideUUID);
			if (playerToHide != null) {
				player.hidePlayer(Main.getInstance(), playerToHide); // Hide "normal"/permanent hidden players
			} else {
				Main.LOGGER.warning("Player hidden " + playerToHideUUID + " is not online.");
			}
		}
		if (CommandHidden.permanentHiddenPlayers.contains(player.getUniqueId())) {
			CommandHidden.hidePlayerToAll(player); // Hide me if I am "permanent" hidden
		}
	}

	/**
	 * On Quit => Remove from hiddenPlayers (not permanent)
	 */
	@EventHandler
	public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
		CommandHidden.hiddenPlayers.remove(event.getPlayer().getUniqueId());
		// No need to call showPlayer because it is temporary
	}

	/**
	 * On chat => Send to Admin Chat
	 */
	@EventHandler
	public void onChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
		CommandChatAdmin.onChat(event);
	}
}
