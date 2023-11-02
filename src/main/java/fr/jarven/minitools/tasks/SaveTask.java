package fr.jarven.minitools.tasks;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import fr.jarven.minitools.Main;
import fr.jarven.minitools.commands.CommandInventory;

public class SaveTask implements Runnable {
	private static BukkitTask saveTask;

	public void run() {
		saveNow();
	}

	public static void saveNow() {
		CommandInventory.getInventoryMenu().save();
	}

	public static void start() {
		if (saveTask != null) {
			saveTask.cancel();
		}
		saveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), new SaveTask(), 6000, 6000);
		// Save every 300 seconds = 5 minutes
	}

	public static void onDisable() {
		if (saveTask != null) {
			saveTask.cancel();
			saveTask = null;
		}
		// Execute one last time
		saveNow();
	}
}
