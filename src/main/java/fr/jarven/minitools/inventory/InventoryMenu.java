package fr.jarven.minitools.inventory;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.jarven.minitools.Main;
import fr.jarven.minitools.commands.CommandGive;

public class InventoryMenu {
	private final String name;
	private YamlConfiguration config;
	private List<Holder> holders = new ArrayList<>();
	private List<ItemStack> menuItems = null;
	private ItemStack menuItemEmpty = null;
	private ItemStack menuItemLocked = null;
	private ItemStack menuItemUnlocked = null;
	private boolean dirty = false;

	public InventoryMenu() {
		this("MiniTools Inventory");
	}

	public InventoryMenu(String name) {
		this.name = name;
	}

	// Number of slots available in the inventory to store items
	// The bottom line is reserved for the inventory menu
	public int getUsableSize() {
		return 45;
	}

	// Number of slots of the inventory
	public int getSize() {
		return 54;
	}

	public String getName() {
		return name;
	}

	public void load() {
		save(); // save if reload

		PageData.registerSerialization();
		File configFile = new File(Main.getInstance().getDataFolder(), "inventory.yml");
		boolean exists = configFile.exists();
		if (!exists) Main.getInstance().saveResource("inventory.yml", false);
		config = YamlConfiguration.loadConfiguration(configFile);

		// Load menu items
		menuItems = CommandGive.loadItems(config.getList("menu_items"), true);
		menuItemEmpty = CommandGive.loadItemStack(config.get("special_menu_items.empty"));
		menuItemLocked = CommandGive.loadItemStack(config.get("special_menu_items.locked"));
		menuItemUnlocked = CommandGive.loadItemStack(config.get("special_menu_items.unlocked"));

		// Load inventories
		@SuppressWarnings("unchecked")
		List<PageData> pagesData = (List<PageData>) config.getList("pages");
		if (pagesData == null) {
			int pageDefaultCount = config.getInt("pages_default_count", 1);
			pagesData = new ArrayList<>();
			if (holders.size() == 0) {
				for (int i = 0; i < pageDefaultCount; i++) {
					holders.add(new Holder(this, i + 1));
				}
			}
		} else {
			// Create holders / Add items to their inventories
			if (holders != null) {
				if (pagesData.size() < holders.size()) {
					for (int i = pagesData.size(); i < holders.size(); i++) {
						holders.get(i).closeAll();
					}
					holders.subList(pagesData.size(), holders.size()).clear();
				} else if (pagesData.size() > holders.size()) {
					for (int i = holders.size(); i < pagesData.size(); i++) {
						holders.add(new Holder(this, i + 1));
					}
				}
			} else {
				holders = new ArrayList<>();
				for (int i = 0; i < pagesData.size(); i++) {
					holders.add(new Holder(this, i + 1));
				}
			}
			for (int i = 0; i < holders.size(); i++) {
				if (pagesData.get(i) != null) pagesData.get(i).apply(holders.get(i));
			}
		}

		dirty = false;
		if (!exists) setDirty(); // If didn't exist, set dirty to save it with intentories
	}

	public void save() {
		if (!dirty) return;
		dirty = false;
		Main.LOGGER.info("Sauvegarde de l'inventaire " + name);

		File configFile = new File(Main.getInstance().getDataFolder(), "inventory.yml");

		List<PageData> inventories = holders.stream().map(h -> new PageData(h)).collect(Collectors.toList());
		config.set("pages", inventories);

		try {
			config.save(configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onDisable() {
		for (Holder holder : holders) {
			holder.closeAll();
		}
		save();
		holders.clear();
	}

	public boolean open(HumanEntity player, int page) {
		int pageIndex = page - 1;
		if (pageIndex < 0 || pageIndex >= holders.size()) return false;
		Holder holder = holders.get(pageIndex);
		if (!holder.isLoaded()) return false;
		holder.open(player);
		return true;
	}

	public boolean isHolder(Inventory inventory) {
		return holders.stream().anyMatch(holder -> holder.getInventory().equals(inventory));
	}

	private ItemStack getMenuItem(int index, int page, Holder holder) {
		if (index < 0 || index >= menuItems.size()) return null;
		ItemStack item = menuItems.get(index);
		switch (index) {
			case 0: // First
				if (page == 1 || item == null)
					return menuItemEmpty; // No previous page
				else if (item != null) {
					item = item.clone();
					item.setAmount(page - 1);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(meta.getDisplayName().replace("%page%", page - 1 + ""));
					item.setItemMeta(meta);
					return item;
				}

			case 4:
				if (holder.isLocked()) {
					return menuItemLocked;
				} else {
					return menuItemUnlocked;
				}

			case 8: // Last
				if (page == holders.size() || item == null)
					return menuItemEmpty; // No next page
				else {
					item = item.clone();
					item.setAmount(page + 1);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(meta.getDisplayName().replace("%page%", page + 1 + ""));
					item.setItemMeta(meta);
					return item;
				}

			default:
				return item == null ? menuItemEmpty : item;
		}
	}

	public void updateInventoryMenu(Holder holder) {
		if (menuItems == null) return;
		int begin = getUsableSize();
		int length = Math.min(menuItems.size(), getSize() - begin);
		int pageIndex = holders.indexOf(holder);
		for (int i = 0; i < length; i++) {
			ItemStack item = getMenuItem(i, pageIndex + 1, holder);
			holder.getInventory().setItem(i + begin, item);
		}
	}

	public void setDirty() {
		dirty = true;
	}

	public int getPageCount() {
		return holders.size();
	}

	public boolean addPage(int i) {
		boolean append = holders.size() <= i;
		if (i < 0) return false;

		// Add a new holder at the end
		Holder holder = new Holder(this, holders.size() + 1);
		holders.add(holder);

		if (append) {
			// Just add the new holder
			holder.load(null);
			setDirty();
			return true;
		}

		// Else, move the inventories between each holders
		ItemStack[] previousContent = null; // the holder added will be empty
		for (int j = i; j < holders.size(); j++) {
			Holder current = holders.get(j);
			ItemStack[] currentContents = current.getInventory().getContents().clone();
			current.load(previousContent);
			previousContent = currentContents;
		}

		setDirty();
		return true;
	}

	public boolean removePage(int i) {
		if (i < 0 || i >= holders.size()) return false;

		// Remove last
		int last = holders.size() - 1;
		ItemStack[] lastContents = holders.get(last).getInventory().getContents().clone();
		holders.get(last).closeAll();
		holders.remove(last);

		if (i < holders.size()) {
			// Move the inventories between each holders
			Holder previous = holders.get(i);
			for (int j = i; j < holders.size(); j++) {
				Holder current = holders.get(j);
				previous.load(current.getInventory().getContents().clone());
				previous = current;
			}
			previous.load(lastContents);
		}

		setDirty();

		return true;
	}
}
