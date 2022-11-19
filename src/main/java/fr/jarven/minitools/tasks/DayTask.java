package fr.jarven.minitools.tasks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import fr.jarven.minitools.Main;

public class DayTask implements Runnable {
	private static BukkitTask dayTask;

	public void run() {
		sendDayMessage();
	}

	public static String getDayMessage() {
		// Print the date with the format: "2022-01-01 00:00:00 (UTC)"
		LocalDateTime dateObj = LocalDateTime.now();
		String dateFormat = Main.getInstance().getConfig().getString("day-message.date-format");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
		String dateText = dateObj.format(formatter);
		return dateText;
	}

	public static String getDayMessage(String configName) {
		String message = Main.getInstance().getConfig().getString(configName);
		message = ChatColor.translateAlternateColorCodes('&', message);
		message = message.replace("%date%", getDayMessage());
		return message;
	}

	public static void sendDayMessage() {
		Main.LOGGER.info(getDayMessage("day-message.message"));
	}

	public static void start() {
		if (dayTask != null) {
			dayTask.cancel();
			dayTask = null;
		}
		boolean enabled = Main.getInstance().getConfig().getBoolean("day-message.enabled");
		if (enabled) {
			int delay = Main.getInstance().getConfig().getInt("day-message.delay") * 20;
			if (delay < 1) {
				delay = 1;
			}
			dayTask = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), new DayTask(), 0, delay);
		}
	}

	public static void onDisable() {
		if (dayTask != null) {
			dayTask.cancel();
			dayTask = null;
		}
		// Execute one last time
		Main.LOGGER.info(getDayMessage("day-message.message-on-disable"));
	}
}
