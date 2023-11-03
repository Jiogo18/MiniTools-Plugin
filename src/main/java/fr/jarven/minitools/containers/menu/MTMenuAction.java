package fr.jarven.minitools.containers.menu;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import fr.jarven.minitools.Main;
import fr.jarven.minitools.commands.CommandInventory;
import fr.jarven.minitools.containers.player_menu.PlayerMenu;
import fr.jarven.minitools.homes.HomesMenu;
import fr.jarven.minitools.homes.WarpsMenu;

public enum MTMenuAction {
	INVENTORY,
	PLAYER,
	WARPS,
	HOMES,
	;

	public void execute(HumanEntity player) {
		switch (this) {
			case INVENTORY:
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
			case WARPS:
				if (!WarpsMenu.open(player)) {
					player.sendMessage("Impossible d'ouvrir l'inventaire");
				}
				break;
			case HOMES:
				if (!HomesMenu.open(player)) {
					player.sendMessage("Impossible d'ouvrir l'inventaire");
				}
				break;
			default:
				player.sendMessage("Action " + this + " not implemented");
				Main.LOGGER.warning("Action " + this + " not implemented");
				break;
		}
	}
}
