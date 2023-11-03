package fr.jarven.minitools.homes;

import org.bukkit.entity.HumanEntity;

import java.util.List;

import fr.jarven.minitools.containers.abs.MiniToolsHolder;

public class HomesMenu extends MiniToolsHolder {
	private final HumanEntity player;

	protected HomesMenu(HumanEntity player) {
		super(27, "MiniTools Homes");
		this.player = player;
		fill();
	}

	public static boolean open(HumanEntity player) {
		HomesMenu menu = new HomesMenu(player);
		return menu.openInventory(player);
	}

	@SuppressWarnings("java:S127") // itemIndex++
	private void fill() {
		List<HomePoint> homes = Homes.getHomes(player.getUniqueId()).getList();
		for (int i = 0, itemIndex = 0; i < homes.size() && itemIndex < 27; i++) {
			HomePoint home = homes.get(i);
			if (home.hasConditions(player)) {
				setItem(itemIndex, new HomeItem(home));
				itemIndex++;
			}
		}
	}
}
