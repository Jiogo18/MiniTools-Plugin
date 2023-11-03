package fr.jarven.minitools.commands;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ItemStackArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import fr.jarven.minitools.homes.HomeList;
import fr.jarven.minitools.homes.HomePoint;
import fr.jarven.minitools.homes.Homes;
import fr.jarven.minitools.homes.WarpsMenu;

public class CommandWarp extends Base {
	@Override
	public Argument<String> getSubCommand() {
		return literal("warp")
			.then(literal("list").executesPlayer(this::executeWarpList))
			.then(literal("add").then((new StringArgument("warp_name")).executesPlayer(this::executeWarpAdd)))
			.then(literal("remove").then(warpArgument().executesPlayer(this::executeRemove)))
			.then(literal("info").then(warpArgument().executesPlayer(this::executeWarpInfo)))
			.then(literal("set").then(warpArgument().executesPlayer(this::executeWarpSetLocation)))
			.then(literal("set_condition").then(warpArgument().then((new StringArgument("condition")).executesPlayer(this::executeWarpSetCondition))))
			.then(literal("set_icon").then(warpArgument().then((new ItemStackArgument("icon")).executesPlayer(this::executeWarpSetIcon))))
			.then(literal("tp").then(executeEntityProxy(
				warpArgument(),
				(proxy, args) -> {
					if (!(proxy.getCaller() instanceof Player)) {
						proxy.sendMessage("Only players can use this command.");
						return;
					}
					if (!(proxy.getCallee() instanceof LivingEntity)) {
						proxy.sendMessage("Only living entities can be targeted.");
						return;
					}
					executeWarpTeleport((Player) proxy.getCaller(), (LivingEntity) proxy.getCallee(), args);
				})))
			.executesPlayer((player, args) -> WarpsMenu.open(player) ? 1 : 0);
	}

	private CompletableFuture<Suggestions> suggestWarps(SuggestionInfo<CommandSender> info, SuggestionsBuilder builder) {
		if (info.sender() instanceof Player) {
			HomeList warps = Homes.getWarps();
			warps.getList().forEach(warp -> builder.suggest(warp.getName()));
		}
		return builder.buildFuture();
	}

	private Argument<String> warpArgument() {
		return new StringArgument("warp_name").replaceSuggestions(this::suggestWarps);
	}

	private HomePoint getWarp(Player player, CommandArguments args) {
		String warpName = (String) args.get("warp_name");
		HomeList warps = Homes.getWarps();
		return warps.getHome(warpName, player);
	}

	private int executeWarpList(Player player, CommandArguments args) {
		HomeList warps = Homes.getWarps();
		player.sendMessage("Warps: " + String.join(", ", warps.getList().stream().map(HomePoint::getName).toArray(String[] ::new)));
		return warps.size();
	}

	private int executeWarpAdd(Player player, CommandArguments args) {
		String warpName = (String) args.get("warp_name");
		HomePoint warp = new HomePoint(warpName, player.getLocation());
		boolean ok = Homes.addWarp(warp);
		if (ok) {
			player.sendMessage("Added warp " + warpName + ".");
			return 1;
		} else {
			player.sendMessage("Failed to add warp " + warpName + ".");
			return 0;
		}
	}

	private int executeRemove(Player player, CommandArguments args) {
		HomePoint warp = getWarp(player, args);
		if (warp == null) {
			player.sendMessage("Warp not found.");
			return 0;
		}
		if (!Homes.removeHome(player.getUniqueId(), warp)) {
			player.sendMessage("Failed to remove warp " + warp.getName() + ".");
			return 0;
		} else {
			player.sendMessage("Removed warp " + warp.getName() + ".");
			return 1;
		}
	}

	private int executeWarpTeleport(Player source, LivingEntity target, CommandArguments args) {
		String warpName = (String) args.get("warp_name");
		HomeList warps = Homes.getWarps();
		HomePoint warp = warps.getHome(warpName, target instanceof Player ? (Player) target : source);
		if (warp == null) {
			source.sendMessage("Warp not found.");
			return 0;
		}

		target.teleport(warp.getLocation());
		return 1;
	}

	private int executeWarpInfo(Player player, CommandArguments args) {
		HomePoint warp = getWarp(player, args);
		if (warp == null) {
			player.sendMessage("Warp not found.");
			return 0;
		}

		player.sendMessage("Warp " + warp.getName() + ":");
		player.sendMessage("  - Location: " + warp.getLocation().getWorld().getName() + " " + warp.getLocation().getBlockX() + " " + warp.getLocation().getBlockY() + " " + warp.getLocation().getBlockZ());
		player.sendMessage("  - Condition: " + warp.getCondition());
		player.sendMessage("  - Material: " + warp.getIcon());
		return 1;
	}

	private int executeWarpSet(Player player, CommandArguments args, Consumer<HomePoint> consumer) {
		HomePoint warp = getWarp(player, args);
		if (warp == null) {
			return executeWarpAdd(player, args);
		}
		consumer.accept(warp);
		boolean ok = Homes.updateWarp(warp);
		if (ok) {
			player.sendMessage("Updated warp " + warp.getName() + ".");
			return 1;
		} else {
			player.sendMessage("Failed to update warp " + warp.getName() + ".");
			return 0;
		}
	}

	private int executeWarpSetLocation(Player player, CommandArguments args) {
		return executeWarpSet(player, args, warp -> warp.setLocation(player.getLocation()));
	}

	private int executeWarpSetCondition(Player player, CommandArguments args) {
		String condition = (String) args.get("condition");
		return executeWarpSet(player, args, warp -> warp.setCondition(condition));
	}

	private int executeWarpSetIcon(Player player, CommandArguments args) {
		ItemStack item = (ItemStack) args.get("icon");
		return executeWarpSet(player, args, warp -> warp.setIcon(item.getType()));
	}
}
