package fr.jarven.minitools.homes;

import org.bukkit.entity.HumanEntity;

import java.util.List;

import fr.jarven.minitools.containers.abs.MiniToolsHolder;

public class WarpsMenu extends MiniToolsHolder {
	private final HumanEntity player;

	/**
	 * Warps are player specific because of conditions
	 */
	protected WarpsMenu(HumanEntity player) {
		super(27, "MiniTools Warps");
		this.player = player;
		fill();
	}

	public static boolean open(HumanEntity player) {
		WarpsMenu menu = new WarpsMenu(player);
		return menu.openInventory(player);
	}

	@SuppressWarnings("java:S127") // itemIndex++
	private void fill() {
		List<HomePoint> warps = Homes.getWarps().getList();
		for (int i = 0, itemIndex = 0; i < warps.size() && itemIndex < 27; i++) {
			HomePoint warp = warps.get(i);
			if (warp.hasConditions(player)) {
				setItem(itemIndex, new HomeItem(warp));
				itemIndex++;
			}
		}
	}
}
