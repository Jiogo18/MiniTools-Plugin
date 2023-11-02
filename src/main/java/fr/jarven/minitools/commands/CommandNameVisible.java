package fr.jarven.minitools.commands;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import dev.jorel.commandapi.arguments.Argument;
import fr.jarven.minitools.Main;

public class CommandNameVisible extends Base {
	private static String teamName = null;

	@Override
	public Argument<String> getSubCommand() {
		return literal("name")
			.then(executePlayer(literal("show"), (player, args) -> changeNameTagVisibility(player, true)))
			.then(executePlayer(literal("hide"), (player, args) -> changeNameTagVisibility(player, false)))
			.then(executePlayerProxy(literal("info"), (proxy, args) -> proxy.sendMessage("§rName tag visibility: " + (isNameTagVisible((Player) proxy.getCallee()) ? "§aON" : "§cOFF"))));
	}

	public static boolean isNameTagVisible(Player player) {
		Team team = player.getScoreboard().getTeam(teamName);
		if (team == null) return true;
		return !team.hasEntry(player.getName());
	}

	public static void changeNameTagVisibility(Player player, boolean visible) {
		Team team = player.getScoreboard().getTeam(teamName);
		if (team == null) {
			team = player.getScoreboard().registerNewTeam(teamName);
			team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
		}

		if (visible) {
			team.removeEntry(player.getName());
		} else {
			team.addEntry(player.getName());
		}
	}

	public static void onLoad() {
		teamName = Main.getInstance().getConfig().getString("name_tag_hidden.team", "name_tag_hidden");
	}
}
