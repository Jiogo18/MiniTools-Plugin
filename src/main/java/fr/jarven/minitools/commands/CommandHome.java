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
import fr.jarven.minitools.homes.HomesMenu;

public class CommandHome extends Base {
	@Override
	public Argument<String> getSubCommand() {
		return literal("home")
			.then(literal("list").executesPlayer(this::executeHomeList))
			.then(literal("add").then((new StringArgument("home_name")).executesPlayer(this::executeHomeAdd)))
			.then(literal("remove").then((new StringArgument("home_name")).replaceSuggestions(this::suggestHomes).executesPlayer(this::executeHomeRemove)))
			.then(literal("info").then((new StringArgument("home_name")).replaceSuggestions(this::suggestHomes).executesPlayer(this::executeHomeInfo)))
			.then(literal("set").then((new StringArgument("home_name")).replaceSuggestions(this::suggestHomes).executesPlayer(this::executeHomeSetLocation)))
			.then(literal("set_condition").then(homeArgument().then((new StringArgument("condition")).executesPlayer(this::executeHomeSetCondition))))
			.then(literal("set_icon").then(homeArgument().then((new ItemStackArgument("icon")).executesPlayer(this::executeHomeSetIcon))))
			.then(literal("tp").then(executeEntityProxy(
				new StringArgument("home_name").replaceSuggestions(this::suggestHomes),
				(proxy, args) -> {
					if (!(proxy.getCaller() instanceof Player)) {
						proxy.sendMessage("Only players can use this command.");
						return;
					}
					if (!(proxy.getCallee() instanceof LivingEntity)) {
						proxy.sendMessage("Only living entities can be targeted.");
						return;
					}
					executeHomeTeleport((Player) proxy.getCaller(), (LivingEntity) proxy.getCallee(), args);
				})))
			.executesPlayer((player, args) -> HomesMenu.open(player) ? 1 : 0);
	}

	private CompletableFuture<Suggestions> suggestHomes(SuggestionInfo<CommandSender> info, SuggestionsBuilder builder) {
		if (info.sender() instanceof Player) {
			Player player = (Player) info.sender();
			HomeList homes = Homes.getHomes(((Player) info.sender()).getUniqueId());
			homes.getList().forEach(home -> {
				if (home.hasConditions(player))
					builder.suggest(home.getName());
			});
		}
		return builder.buildFuture();
	}

	private Argument<String> homeArgument() {
		return new StringArgument("home_name").replaceSuggestions(this::suggestHomes);
	}

	private HomePoint getHome(Player player, CommandArguments args) {
		String warpName = (String) args.get("home_name");
		HomeList homes = Homes.getHomes(player.getUniqueId());
		return homes.getHome(warpName, player);
	}

	private int executeHomeList(Player player, CommandArguments args) {
		HomeList homes = Homes.getHomes(player.getUniqueId());
		player.sendMessage("Homes: " + String.join(", ", homes.getList().stream().map(HomePoint::getName).toArray(String[] ::new)));
		return homes.size();
	}

	private int executeHomeAdd(Player player, CommandArguments args) {
		String homeName = (String) args.get("home_name");
		HomePoint home = new HomePoint(homeName, player.getLocation());
		boolean ok = Homes.addHome(player.getUniqueId(), home);
		if (ok) {
			player.sendMessage("Added home " + homeName + ".");
			return 1;
		} else {
			player.sendMessage("Failed to add home " + homeName + ".");
			return 0;
		}
	}

	private int executeHomeRemove(Player player, CommandArguments args) {
		HomePoint home = getHome(player, args);
		if (home == null) {
			player.sendMessage("Home not found.");
			return 0;
		}
		if (!Homes.removeHome(player.getUniqueId(), home)) {
			player.sendMessage("Failed to remove home " + home.getName() + ".");
			return 0;
		} else {
			player.sendMessage("Removed home " + home.getName() + ".");
			return 1;
		}
	}

	private int executeHomeTeleport(Player source, LivingEntity target, CommandArguments args) {
		String homeName = (String) args.get("home_name");
		HomeList homes = Homes.getHomes(source.getUniqueId());
		HomePoint home = homes.getHome(homeName, target instanceof Player ? (Player) target : source);
		if (home == null) {
			source.sendMessage("Home not found.");
			return 0;
		}

		target.teleport(home.getLocation());
		return 1;
	}

	private int executeHomeInfo(Player player, CommandArguments args) {
		HomePoint home = getHome(player, args);
		if (home == null) {
			player.sendMessage("Home not found.");
			return 0;
		}

		player.sendMessage("Home " + home.getName() + ":");
		player.sendMessage("  - Location: " + home.getLocation().getWorld().getName() + " " + home.getLocation().getBlockX() + " " + home.getLocation().getBlockY() + " " + home.getLocation().getBlockZ());
		player.sendMessage("  - Condition: " + home.getCondition());
		player.sendMessage("  - Material: " + home.getIcon());
		return 1;
	}

	private int executeHomeSet(Player player, CommandArguments args, Consumer<HomePoint> consumer) {
		HomePoint home = getHome(player, args);
		if (home == null) {
			return executeHomeAdd(player, args);
		}
		consumer.accept(home);
		boolean ok = Homes.updateHome(player.getUniqueId(), home);
		if (ok) {
			player.sendMessage("Updated home " + home.getName() + ".");
			return 1;
		} else {
			player.sendMessage("Failed to update home " + home.getName() + ".");
			return 0;
		}
	}

	private int executeHomeSetLocation(Player player, CommandArguments args) {
		return executeHomeSet(player, args, home -> home.setLocation(player.getLocation()));
	}

	private int executeHomeSetCondition(Player player, CommandArguments args) {
		String condition = (String) args.get("condition");
		return executeHomeSet(player, args, home -> home.setCondition(condition));
	}

	private int executeHomeSetIcon(Player player, CommandArguments args) {
		ItemStack item = (ItemStack) args.get("icon");
		return executeHomeSet(player, args, home -> home.setIcon(item.getType()));
	}
}
