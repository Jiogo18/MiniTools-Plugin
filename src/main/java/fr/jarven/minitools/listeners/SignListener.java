package fr.jarven.minitools.listeners;

import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;

import fr.jarven.minitools.commands.CommandSign;

public class SignListener implements Listener {
	/**
	 * Click on sign with a sign item
	 */
	@EventHandler
	public void onSignInteract(org.bukkit.event.player.PlayerInteractEvent event) {
		if (!event.getPlayer().hasPermission("minitools.sign.edit"))
			return;
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof Sign) {
			Sign sign = (Sign) event.getClickedBlock().getState();
			if (event.getItem() != null && event.getItem().getType().data == org.bukkit.block.data.type.Sign.class) {
				// Sign item
				if (!event.getPlayer().isSneaking()) {
					// Not Sneaking => Edit sign
					CommandSign.sendSignChatEditor(event.getPlayer(), sign);
					event.setCancelled(true);
				}
			}
		}
	}
}
