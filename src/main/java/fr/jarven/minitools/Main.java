package fr.jarven.minitools;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

import fr.jarven.minitools.commands.MyCommands;
import fr.jarven.minitools.listeners.CommandsListeners;
import fr.jarven.minitools.listeners.InventoryListeners;
import fr.jarven.minitools.listeners.SignListener;
import fr.jarven.minitools.tasks.DayTask;
import fr.jarven.minitools.tasks.SaveTask;

public class Main extends JavaPlugin {
	public static final Logger LOGGER = Logger.getLogger("MiniTools");
	private static Main instance;

	@Override
	public void onLoad() {
		instance = this;

		saveDefaultConfig();
	}

	@Override
	public void onEnable() {
		MyCommands.onEnable();
		getServer().getPluginManager().registerEvents(new SignListener(), this);
		getServer().getPluginManager().registerEvents(new CommandsListeners(), this);
		getServer().getPluginManager().registerEvents(new InventoryListeners(), this);
		SaveTask.start();
		DayTask.start();
	}

	@Override
	public void onDisable() {
		MyCommands.onDisable();
		SaveTask.onDisable();
		DayTask.onDisable();
	}

	public static Plugin getInstance() {
		return instance;
	}
}