package fr.jarven.minitools.homes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HomePoint {
	private Material icon;
	private String name;
	private String world;
	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;
	private String condition;

	private HomePoint() {}

	public HomePoint(String name, Location location) {
		this.name = name;
		this.world = location.getWorld().getName();
		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();
		this.yaw = location.getYaw();
		this.pitch = location.getPitch();
		this.icon = getMaterialAroundLocation(location);
		if (this.icon == Material.AIR) {
			this.icon = Material.ENDER_PEARL;
		}
		this.condition = null;
	}

	public String getKey() {
		return name.toLowerCase() + "_" + condition;
	}

	public boolean hasConditions(HumanEntity player) {
		return condition == null || player.hasPermission(condition) || player.getName().equals(condition);
	}

	public static HomePoint fromConfig(ConfigurationSection homeSection) {
		if (homeSection == null) return null;

		HomePoint homePoint = new HomePoint();
		homePoint.icon = Material.getMaterial(homeSection.getString("icon", "ENDER_PEARL"));
		homePoint.name = homeSection.getString("name", "home");
		homePoint.world = homeSection.getString("world", "world");
		homePoint.x = homeSection.getDouble("x", 0);
		homePoint.y = homeSection.getDouble("y", 0);
		homePoint.z = homeSection.getDouble("z", 0);
		homePoint.yaw = (float) homeSection.getDouble("yaw", 0);
		homePoint.pitch = (float) homeSection.getDouble("pitch", 0);
		homePoint.condition = homeSection.getString("condition", null);
		return homePoint;
	}

	public Material getIcon() {
		return icon;
	}

	public String getName() {
		return name;
	}

	public Location getLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
	}

	public String getCondition() {
		return condition;
	}

	public void saveTo(ConfigurationSection homeSection) {
		homeSection.set("icon", icon.name());
		homeSection.set("name", name);
		homeSection.set("world", world);
		homeSection.set("x", x);
		homeSection.set("y", y);
		homeSection.set("z", z);
		homeSection.set("yaw", yaw);
		homeSection.set("pitch", pitch);
		if (condition != null) {
			homeSection.set("condition", condition);
		}
	}

	private static Material getMaterialAroundLocation(Location location) {
		Block block = location.getBlock();
		for (int i = 0; i < 5; i++) {
			if (block.getType() != Material.AIR) {
				break;
			}
			block = block.getRelative(BlockFace.DOWN);
		}
		Material material = block.getType();
		switch (material) {
			case WATER:
				return Material.WATER_BUCKET;
			case LAVA:
				return Material.LAVA_BUCKET;
			case AIR:
				return Material.ENDER_PEARL;
			default:
				return material;
		}
	}

	public ItemStack getItemIcon() {
		ItemStack item = new ItemStack(icon);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§r" + name);
		item.setItemMeta(meta);
		return item;
	}

	public void setLocation(Location location) {
		this.world = location.getWorld().getName();
		this.x = location.getBlockX();
		this.y = location.getBlockY();
		this.z = location.getBlockZ();
		this.yaw = location.getYaw();
		this.pitch = location.getPitch();
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public void setIcon(Material icon) {
		this.icon = icon;
	}
}
