package fr.jarven.minitools.commands;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.RegisteredCommand;
import dev.jorel.commandapi.arguments.Argument;
import fr.jarven.minitools.Main;

public class CommandAlias extends Base {
	private static final String ALIAS_FILE = "alias.yml";
	private static Set<String> aliasesKeys = new HashSet<>();

	public static class Alias {
		private String name;
		private String command;
		private String permission;
		private boolean override;
		private boolean checkCommand;

		public Alias(String name, String command, String permission, boolean override, boolean checkCommand) {
			this.name = name;
			this.command = command;
			this.permission = permission;
			this.override = override;
			this.checkCommand = checkCommand;
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
	}

	private static void loadAliases() {
		unloadAliases();

		File file = new File(Main.getInstance().getDataFolder(), ALIAS_FILE);
		if (!file.exists()) {
			Main.getInstance().saveResource(ALIAS_FILE, false);
		}

		YamlConfiguration aliasConfig = YamlConfiguration.loadConfiguration(file);

		ConfigurationSection aliasesSection = aliasConfig.getConfigurationSection("aliases");
		if (aliasesSection == null) return;

		aliasesKeys = aliasesSection.getKeys(false);
		List<Alias> aliases = new ArrayList<>();
		for (String aliasName : aliasesKeys) {
			ConfigurationSection aliasSection = aliasesSection.getConfigurationSection(aliasName);
			if (aliasSection == null) continue;

			String command = aliasSection.getString("command", "");
			String permission = aliasSection.getString("permission", "");
			boolean override = aliasSection.getBoolean("override", false);
			boolean checkCommand = aliasSection.getBoolean("checkCommand", true);

			Alias alias = new Alias(aliasName, command, permission, override, checkCommand);
			aliases.add(alias);
		}

		aliases.forEach(CommandAlias::addAlias);
	}

	private static boolean addAlias(Alias alias) {
		// Register with CommandAPI
		Optional<RegisteredCommand> cmd;
		if (alias.noOverrideIfExist()) {
			cmd = CommandAPI.getRegisteredCommands()
				      .stream()
				      .filter(command -> command.commandName().equals(alias.getName()))
				      .findAny();
			if (cmd.isPresent()) {
				Main.LOGGER.warning("Alias " + alias.getName() + " already registered, you can force it with override: true.");
				return false;
			}
		}

		if (alias.checkCommand()) {
			String commandName = alias.getCommand().split(" ")[0];
			cmd = CommandAPI.getRegisteredCommands()
				      .stream()
				      .filter(command -> command.commandName().equals(commandName))
				      .findAny();
			if (!cmd.isPresent()) {
				Main.LOGGER.warning("Alias " + alias.getName() + " : command " + commandName + " not found.");
				return false;
			}
		}

		CommandTree aliasCmd = new CommandTree(alias.getName());
		if (!alias.getPermission().isEmpty()) {
			aliasCmd.withPermission(alias.getPermission());
		}
		aliasCmd.executesPlayer((sender, args) -> sender.performCommand(alias.getCommand()) ? 1 : 0)
			.register();

		Main.LOGGER.info("Alias " + alias.getName() + " registered.");
		return true;
	}

	private static void unloadAliases() {
		aliasesKeys.forEach(CommandAPI::unregister);
		aliasesKeys.clear();
	}

	public static void onLoad() {
		if (!aliasesKeys.isEmpty()) {
			unloadAliases();
		}
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
				sender.sendMessage("Liste des alias enregistrés :\n" + String.join(", ", aliasesKeys));
				return 1;
			});
	}
}
