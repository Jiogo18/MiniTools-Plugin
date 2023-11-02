package fr.jarven.minitools.containers.menu;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import fr.jarven.minitools.Main;
import fr.jarven.minitools.commands.CommandInventory;
import fr.jarven.minitools.containers.player_menu.PlayerMenu;

public enum MTMenuAction {
	INVENTORY,
	PLAYER;

	public void execute(HumanEntity player) {
		switch (this) {
			case INVENTORY:
				player.closeInventory();
				if (!CommandInventory.openInventory(player, 1)) {
					player.sendMessage("Impossible d'ouvrir l'inventaire");
				}
				break;
			case PLAYER:
				if (player instanceof Player)
					PlayerMenu.open((Player) player);
				else
					player.sendMessage("Only players can use this command");
				break;
			default:
				player.sendMessage("Action " + this + " not implemented");
				Main.LOGGER.warning("Action " + this + " not implemented");
				break;
		}
	}
}
