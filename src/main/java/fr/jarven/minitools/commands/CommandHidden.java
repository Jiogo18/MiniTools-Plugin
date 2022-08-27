package fr.jarven.minitools.commands;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.EntitySelector;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import fr.jarven.minitools.Main;

public class CommandHidden extends Base {
	public static final Set<UUID> hiddenPlayers = new HashSet<>();
	public static final Set<UUID> permanentHiddenPlayers = new HashSet<>();

	public static ArgumentTree getSubCommand() {
		return literal("hidden")
			.then(executePlayerProxy(literal("on"), (proxy, args) -> {
				Player playerToHide = (Player) proxy.getCallee();
				hidePlayerToAll(playerToHide);
				if (!areCallerCalleeTheSame(proxy)) {
					proxy.getCaller().sendMessage("§a" + playerToHide.getName() + " est désormais masqué.");
				}
			}))
			.then(executePlayerProxy(literal("off"), (proxy, args) -> {
				Player playerToShow = (Player) proxy.getCallee();
				showPlayerToAll(playerToShow);
				if (!areCallerCalleeTheSame(proxy)) {
					proxy.getCaller().sendMessage("§a" + playerToShow.getName() + " est désormais visible.");
				}
			}))
			.then(executePlayerProxy(literal("permanent"), (proxy, args) -> {
				Player playerToHide = (Player) proxy.getCallee();
				permanentHidePlayerToAll(playerToHide);
				if (!areCallerCalleeTheSame(proxy)) {
					proxy.getCaller().sendMessage("§a" + playerToHide.getName() + " est désormais masqué de façon permanente.");
				}
			}))
			.then(literal("i_cant_see").then(executePlayer(new EntitySelectorArgument<Collection<Player>>("players_who_should_be_hidden_to_me", EntitySelector.MANY_PLAYERS), (sender, args) -> {
				@SuppressWarnings("unchecked")
				Collection<Player> playersToHide = (Collection<Player>) args[0];
				for (Player playerToHide : playersToHide) {
					sender.hidePlayer(Main.getInstance(), playerToHide);
				}
			})))
			.then(literal("i_can_see").then(executePlayer(new EntitySelectorArgument<Collection<Player>>("players_who_should_be_shown_to_me", EntitySelector.MANY_PLAYERS), (sender, args) -> {
				@SuppressWarnings("unchecked")
				Collection<Player> playersToShow = (Collection<Player>) args[0];
				for (Player playerToShow : playersToShow) {
					sender.showPlayer(Main.getInstance(), playerToShow);
				}
			})))
			.then(literal("i_am_invisible_to").then(executePlayer(new EntitySelectorArgument<Collection<Player>>("players_who_wont_see_you", EntitySelector.MANY_PLAYERS), (sender, args) -> {
				@SuppressWarnings("unchecked")
				Collection<Player> playersToHide = (Collection<Player>) args[0];
				for (Player playerWhoWontSeeYou : playersToHide) {
					playerWhoWontSeeYou.hidePlayer(Main.getInstance(), sender);
				}
			})))
			.then(literal("i_am_visible_to").then(executePlayer(new EntitySelectorArgument<Collection<Player>>("players_who_will_see_you", EntitySelector.MANY_PLAYERS), (sender, args) -> {
				@SuppressWarnings("unchecked")
				Collection<Player> playersToShow = (Collection<Player>) args[0];
				for (Player playerWhoWillShowYou : playersToShow) {
					playerWhoWillShowYou.showPlayer(Main.getInstance(), sender);
				}
			})));
	}

	public static void hidePlayerToAll(Player playerToHide) {
		hiddenPlayers.add(playerToHide.getUniqueId());
		for (Player p : getOnlinePlayersWithout(playerToHide)) {
			p.hidePlayer(Main.getInstance(), playerToHide);
		}
		playerToHide.sendMessage("§aVous êtes maintenant invisible aux autres joueurs.");
	}

	public static void showPlayerToAll(Player playerToShow) {
		hiddenPlayers.remove(playerToShow.getUniqueId());
		permanentHiddenPlayers.remove(playerToShow.getUniqueId());
		for (Player p : getOnlinePlayersWithout(playerToShow)) {
			p.showPlayer(Main.getInstance(), playerToShow);
		}
		playerToShow.sendMessage("§aVous êtes maintenant visible aux autres joueurs.");
	}

	public static void permanentHidePlayerToAll(Player playerToHide) {
		hiddenPlayers.add(playerToHide.getUniqueId());
		permanentHiddenPlayers.add(playerToHide.getUniqueId());
		for (Player p : getOnlinePlayersWithout(playerToHide)) {
			p.hidePlayer(Main.getInstance(), playerToHide);
		}
		playerToHide.sendMessage("§aVous êtes maintenant invisible aux autres joueurs de façon permanent.");
	}

	public static boolean isHidden(Player player) {
		return hiddenPlayers.contains(player.getUniqueId());
	}
}
