package fr.jarven.minitools.menu;

import org.bukkit.entity.HumanEntity;

import fr.jarven.minitools.Main;
import fr.jarven.minitools.commands.CommandInventory;

public enum MenuAction {
	INVENTORY;

	public void execute(HumanEntity player) {
		switch (this) {
			case INVENTORY:
				player.closeInventory();
				if (!CommandInventory.openInventory(player, 1)) {
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
