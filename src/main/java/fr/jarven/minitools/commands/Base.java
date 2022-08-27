package fr.jarven.minitools.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.function.BiConsumer;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;

public class Base {
	public static LiteralArgument literal(String name) {
		return new LiteralArgument(name);
	}

	public static Optional<Player> getPlayer(NativeProxyCommandSender proxy) {
		if (proxy.getCallee() instanceof Player) {
			return Optional.of((Player) proxy.getCallee());
		} else {
			proxy.getCaller().sendMessage("Not a player");
			return Optional.empty();
		}
	}

	public static Optional<HumanEntity> getHuman(NativeProxyCommandSender proxy) {
		if (proxy.getCallee() instanceof HumanEntity) {
			return Optional.of((HumanEntity) proxy.getCallee());
		} else {
			proxy.getCaller().sendMessage("Not a human entity");
			return Optional.empty();
		}
	}

	public static Optional<Entity> getEntity(NativeProxyCommandSender proxy) {
		if (proxy.getCallee() instanceof Entity) {
			return Optional.of((Entity) proxy.getCallee());
		} else {
			proxy.getCaller().sendMessage("Not an entity");
			return Optional.empty();
		}
	}

	public static ArgumentTree executePlayerProxy(ArgumentTree arg, BiConsumer<NativeProxyCommandSender, Object[]> action) {
		return arg
			.executesNative((proxy, args) -> {
				Optional<Player> player = getPlayer(proxy);
				if (player.isPresent()) {
					action.accept(proxy, args);
					return 1;
				} else {
					return 0;
				}
			});
	}

	public static ArgumentTree executeHumanProxy(ArgumentTree arg, BiConsumer<NativeProxyCommandSender, Object[]> action) {
		return arg
			.executesNative((proxy, args) -> {
				Optional<HumanEntity> player = getHuman(proxy);
				if (player.isPresent()) {
					action.accept(proxy, args);
					return 1;
				} else {
					return 0;
				}
			});
	}

	public static ArgumentTree executePlayer(ArgumentTree arg, BiConsumer<Player, Object[]> action) {
		return executePlayerProxy(arg, (proxy, args) -> action.accept((Player) proxy.getCallee(), args));
	}

	public static ArgumentTree executeEntityProxy(ArgumentTree arg, BiConsumer<NativeProxyCommandSender, Object[]> action) {
		return arg
			.executesNative((proxy, args) -> {
				Optional<Entity> entity = getEntity(proxy);
				if (entity.isPresent()) {
					action.accept(proxy, args);
					return 1;
				} else {
					return 0;
				}
			});
	}

	public static boolean areCallerCalleeTheSame(NativeProxyCommandSender proxy) {
		if (!(proxy.getCallee() instanceof Entity)) return true; // Server and Server = the same
		if (!(proxy.getCaller() instanceof Entity)) return false; // Server and Player = different
		return ((Entity) proxy.getCaller()).getUniqueId().equals(((Entity) proxy.getCallee()).getUniqueId());
	}

	public static ArgumentTree executeEntity(ArgumentTree arg, BiConsumer<Entity, Object[]> action) {
		return executeEntityProxy(arg, (proxy, args) -> action.accept((Entity) proxy.getCallee(), args));
	}

	public static Player[] getOnlinePlayersWithout(Player playerToExclude) {
		return Bukkit.getOnlinePlayers().stream().filter(p -> !p.getUniqueId().equals(playerToExclude.getUniqueId())).toArray(Player[] ::new);
	}

	public static ArgumentSuggestions suggestArray(String[] suggestions) {
		return (info, builder) -> {
			String current = info.currentArg().toLowerCase();
			for (String suggestion : suggestions) {
				String str = suggestion.toLowerCase();
				if (str.startsWith(current)) {
					builder.suggest(str);
				}
			}
			return builder.buildFuture();
		};
	}

	public static ArgumentSuggestions suggestSome(String... suggestions) {
		return suggestArray(suggestions);
	}
}
