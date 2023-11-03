package fr.jarven.minitools.commands;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import fr.jarven.minitools.Main;

public class CommandAlias extends Base {
	private static final String ALIAS_FILE = "alias.yml";
	private static Set<String> aliasesRegistered = new HashSet<>();
	private static Set<String> allAliasesRegisteredByMiniTools = new HashSet<>();
	private static Set<String> commandAlreadyExist = new HashSet<>();
	private static boolean firstLoad = true;

	enum ArgType {
		STRING("string"),
		TEXT("text"),
		PLAYER("player");

		private final String name;

		private ArgType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static ArgType fromString(String name) {
			for (ArgType type : ArgType.values()) {
				if (type.getName().equalsIgnoreCase(name)) {
					return type;
				}
			}
			return null;
		}
	}

	public static class Alias {
		private String name;
		private String command;
		private String permission;
		private boolean override;
		private boolean checkCommand;
		private List<String> otherAliases;
		private List<ArgType> args;

		public Alias(String name, String command, String permission, boolean override, boolean checkCommand, List<String> otherAliases, List<ArgType> args) {
			this.name = name;
			this.command = command;
			this.permission = permission;
			this.override = override;
			this.checkCommand = checkCommand;
			this.otherAliases = otherAliases;
			this.args = args;
		}

		public String getName() {
			return name;
		}

		public String getCommand() {
			return command;
		}

		public String getPermission() {
			return permission;
		}

		public boolean getOverride() {
			return override;
		}

		public boolean noOverrideIfExist() {
			return !override;
		}

		public boolean checkCommand() {
			return checkCommand;
		}

		public List<String> getOtherAliases() {
			return otherAliases;
		}

		public List<ArgType> getArgs() {
			return args;
		}
	}

	private static void loadAliases() {
		Main.LOGGER.info("Loading aliases...");
		boolean warningConcurrentModificationSent = false;
		int aliasesBefore = aliasesRegistered.size();

		File file = new File(Main.getInstance().getDataFolder(), ALIAS_FILE);
		if (!file.exists()) {
			Main.getInstance().saveResource(ALIAS_FILE, false);
		}

		YamlConfiguration aliasConfig = YamlConfiguration.loadConfiguration(file);
		ConfigurationSection aliasesSection = aliasConfig.getConfigurationSection("aliases");
		if (aliasesSection == null) {
			firstLoad = false;
			return;
		}

		Set<String> aliasesKeys = aliasesSection.getKeys(false);
		for (String aliasName : aliasesKeys) {
			ConfigurationSection aliasSection = aliasesSection.getConfigurationSection(aliasName);
			if (aliasSection == null) continue;

			String command = aliasSection.getString("command", "");
			String permission = aliasSection.getString("permission", "");
			boolean override = aliasSection.getBoolean("override", false);
			boolean checkCommand = aliasSection.getBoolean("checkCommand", true);
			List<String> otherAliases = aliasSection.getStringList("otherAliases");
			List<ArgType> args = aliasSection.getStringList("args").stream().map(ArgType::fromString).toList();

			if (!firstLoad && !aliasesRegistered.contains(aliasName) && !warningConcurrentModificationSent) {
				Main.LOGGER.warning("You may see a ConcurrentModificationException below, don't worry, this is because of a new alias registered by MiniTools");
				warningConcurrentModificationSent = true;
			}

			Alias alias = new Alias(aliasName, command, permission, override, checkCommand, otherAliases, args);
			addAlias(alias);
		}

		int aliasesNow = aliasesRegistered.size();
		Main.LOGGER.info("Aliases loaded (" + (aliasesNow - aliasesBefore) + " new).");
		firstLoad = false;
	}

	/**
	 * getRegisteredCommands keep track of all registered commands, even if they are unregistered
	 */
	private static boolean isCommandAlreadyRegistered(String commandName) {
		return CommandAPI.getRegisteredCommands()
			.stream()
			.anyMatch(command -> command.commandName().equals(commandName));
	}

	private static boolean canRegisterAlias(String aliasName, boolean canOverride) {
		if (allAliasesRegisteredByMiniTools.contains(aliasName)) {
			// Alias registered by MiniTools, we can safely override. If it's not what you want just reload the server.

			if (commandAlreadyExist.contains(aliasName) && !canOverride) {
				// The command existed before MiniTools
				// Then MiniTools override it
				// And now you want to load it with override: false
				// => we override it again

				Main.LOGGER.warning("Alias " + aliasName + " already registered by another plugin and next by MiniTools => overriding again. This can append if you changed the override from true to false. If you expected to use the command from the other plugin, please reload the server.");
			}
			return true;
		}
		if (commandAlreadyExist.contains(aliasName) && !canOverride) {
			return false; // Alias already registered by another plugin, we can't override.
		}
		return true;
	}

	private static boolean addAlias(Alias alias) {
		// Register with CommandAPI

		if (!commandAlreadyExist.contains(alias.getName()) && !allAliasesRegisteredByMiniTools.contains(alias.getName())) {
			// If not registered in one or the other => first time we load it,
			// => we check if it's registered by another plugin
			if (isCommandAlreadyRegistered(alias.getName())) {
				commandAlreadyExist.add(alias.getName());
			}
		}

		if (!canRegisterAlias(alias.getName(), alias.getOverride())) {
			Main.LOGGER.warning("Alias " + alias.getName() + " already registered, you can force it with override: true.");
			return false;
		}

		if (alias.checkCommand()) {
			String commandName = alias.getCommand().split(" ")[0];
			boolean cmdExist = isCommandAlreadyRegistered(commandName);
			if (!cmdExist) {
				Main.LOGGER.warning("Alias " + alias.getName() + " : command " + commandName + " not found.");
				return false;
			}
		}

		CommandTree aliasCmd = buildCommand(alias);
		aliasCmd
			.executesPlayer((sender, args) -> sender.performCommand(alias.getCommand()) ? 1 : 0)
			.register();

		aliasesRegistered.add(alias.getName());
		allAliasesRegisteredByMiniTools.add(alias.getName());
		return true;
	}

	private static CommandTree buildCommand(Alias alias) {
		CommandTree cmd = new CommandTree(alias.getName())
					  .withShortDescription("Alias for " + alias.getCommand());
		if (!alias.getPermission().isEmpty()) {
			cmd.withPermission(alias.getPermission());
		}
		if (!alias.getOtherAliases().isEmpty()) {
			cmd.withAliases(alias.getOtherAliases().toArray(new String[0]));
		}

		buildWithArguments(cmd, alias);
		return cmd;
	}

	private static void buildWithArguments(CommandTree cmd, Alias alias) {
		for (ArgType arg : alias.getArgs()) {
			switch (arg) {
				case STRING:
					cmd.then(new StringArgument("string")
							 .executesPlayer((sender, args) -> sender.performCommand(alias.getCommand() + " " + args.get("string")) ? 1 : 0));
					break;
				case TEXT:
					cmd.then(new GreedyStringArgument("text")
							 .executesPlayer((sender, args) -> sender.performCommand(alias.getCommand() + " " + args.get("text")) ? 1 : 0));
					break;
				case PLAYER:
					cmd.then(new PlayerArgument("player")
							 .executesPlayer((sender, args) -> sender.performCommand(alias.getCommand() + " " + args.get("player")) ? 1 : 0));
					break;
			}
		}
	}

	private static void unloadAliases() {
		aliasesRegistered.forEach(cmd -> CommandAPI.unregister(cmd));
		aliasesRegistered.clear();
		Main.LOGGER.info("Aliases unloaded.");
	}

	public static void onLoad() {
		loadAliases();
	}

	public static void onDisable() {
		unloadAliases();
	}

	@Override
	public Argument<String> getSubCommand() {
		return literal("alias")
			.executes((sender, args) -> {
				// List registered aliases
				sender.sendMessage("Liste des alias enregistrés :\n" + String.join(", ", aliasesRegistered));
				return 1;
			});
	}
}
