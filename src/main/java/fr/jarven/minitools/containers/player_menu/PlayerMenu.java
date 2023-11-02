package fr.jarven.minitools.containers.player_menu;

import org.bukkit.entity.Player;

import java.util.List;

import fr.jarven.minitools.Main;
import fr.jarven.minitools.containers.abs.MiniToolsHolder;

public class PlayerMenu extends MiniToolsHolder {
	private final Player player;
	private static PlayerMenuAction[] actions = null;

	public PlayerMenu(Player player) {
		super(27, "MiniTools Player Menu");
		this.player = player;
		fill();
	}

	public static void open(Player player) {
		PlayerMenu menu = new PlayerMenu(player);
		menu.openInventory(player);
	}

	private void fill() {
		if (actions == null) loadActions();

		for (int i = 0; i < actions.length; i++) {
			PlayerMenuAction action = actions[i];
			setItem(i, new PlayerMenuItem(action, player));
		}
	}

	public void onPlayerActionTriggered(PlayerMenuAction action) {
		action.setActivated(player, !action.isActivated(player));
		fill();
		updateViewers();
	}

	private static void loadActions() {
		Object playerMenuActions = Main.getInstance().getConfig().get("player_menu.actions");
		actions = null;
		if (playerMenuActions instanceof List) {
			List<?> list = (List<?>) playerMenuActions;
			if (!list.isEmpty() && list.get(0) instanceof String) {
				actions = list.stream().map(o -> PlayerMenuAction.valueOf((String) o)).toArray(PlayerMenuAction[] ::new);
			} else if (!list.isEmpty()) {
				Main.LOGGER.warning("player_menu.actions mut be a list of strings");
			}
		} else {
			Main.LOGGER.warning("player_menu.actions is not a list");
		}

		if (actions == null) {
			actions = new PlayerMenuAction[0];
		}
	}

	public static void onLoad() {
		actions = null;
	}
}
