package fr.jarven.minitools.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

import fr.jarven.minitools.Main;
import fr.jarven.minitools.commands.CommandHidden;

public class CommandsListeners implements Listener {
	/**
	 * On Login => Hide players if permanent
	 */
	@EventHandler
	public void onLogin(org.bukkit.event.player.PlayerLoginEvent event) {
		for (UUID playerToHideUUID : CommandHidden.hiddenPlayers) {
			Player playerToHide = Bukkit.getPlayer(playerToHideUUID);
			if (playerToHide != null) {
				event.getPlayer().hidePlayer(Main.getInstance(), playerToHide); // Hide "normal" hidden players
			}
		}
		if (CommandHidden.permanentHiddenPlayers.contains(event.getPlayer().getUniqueId())) {
			CommandHidden.hidePlayerToAll(event.getPlayer()); // Hide me if I am "permanent" hidden
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
}
