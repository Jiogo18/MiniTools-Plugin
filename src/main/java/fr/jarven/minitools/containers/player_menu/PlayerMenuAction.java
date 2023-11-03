package fr.jarven.minitools.containers.player_menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.BiConsumer;
import java.util.function.Function;

import fr.jarven.minitools.commands.CommandHidden;
import fr.jarven.minitools.commands.CommandNameVisible;

public enum PlayerMenuAction {
	GRAVITY(Material.FEATHER, Player::hasGravity, Player::setGravity),
	FLYING(Material.ELYTRA, Player::isFlying, (p, b) -> {
		if (Boolean.TRUE.equals(b)) p.setAllowFlight(true);
		p.setFlying(b);
		p.teleport(p.getLocation().add(0, 1e-6, 0));
		PlayerMenu.open(p);
	}),
	ALLOW_FLIGHT(Material.ELYTRA, Player::getAllowFlight, Player::setAllowFlight),
	VANISH(Material.POTION, Player::isInvisible, Player::setInvisible),
	HIDDEN(Material.ENDER_EYE, CommandHidden::isHidden, (p, b) -> { if(Boolean.TRUE.equals(b)) CommandHidden.hidePlayerToAll(p); else CommandHidden.showPlayerToAll(p); }),
	HIDDEN_PERMANENT(Material.ENDER_EYE, CommandHidden::isPermanentHidden, (p, b) -> { if(b == Boolean.TRUE) CommandHidden.permanentHidePlayerToAll(p); else CommandHidden.showPlayerToAll(p); }),
	SLEEPING_IGNORED(Material.RED_BED, Player::isSleepingIgnored, Player::setSleepingIgnored),
	NAME_TAG_VISIBLE(Material.NAME_TAG, CommandNameVisible::isNameTagVisible, CommandNameVisible::changeNameTagVisibility),
	INVULNERABLE(Material.SHIELD, Player::isInvulnerable, Player::setInvulnerable),
	GLOWING(Material.GLOWSTONE_DUST, Player::isGlowing, Player::setGlowing),
	;

	private final Material material;
	private final Function<Player, Boolean> predicate;
	private final BiConsumer<Player, Boolean> consumer;

	private PlayerMenuAction(Material material, Function<Player, Boolean> predicate, BiConsumer<Player, Boolean> consumer) {
		this.material = material;
		this.predicate = predicate;
		this.consumer = consumer;
	}

	public boolean isActivated(Player player) {
		return predicate.apply(player);
	}

	public void setActivated(Player player, boolean activated) {
		consumer.accept(player, activated);
	}

	public String getLabel() {
		return name();
	}

	public String getLabel(boolean activated) {
		return "§r" + getLabel() + (activated ? " : §aON " : " : §cOFF");
	}

	public ItemStack getItemStack(Player player) {
		boolean activated = isActivated(player);
		ItemStack itemStack = new ItemStack(material);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(getLabel(activated));
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}
}
