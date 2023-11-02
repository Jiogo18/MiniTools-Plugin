package fr.jarven.minitools.containers.inventory;

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
import fr.jarven.minitools.utils.CustomItemStack;

public class InventoryMenu {
	private final String name;
	private YamlConfiguration config;
	/**
	 * List of inventory holders
	 */
	private List<InventoryPage> holders = new ArrayList<>();
	/**
	 * Items for the menu (bottom line)
	 */
	private List<ItemStack> menuItems = null;
	private MenuItem menuItemEmpty = null;
	private ItemStack menuItemLocked = null;
	private ItemStack menuItemUnlocked = null;
	private boolean dirty = false;

	public InventoryMenu() {
		this("MiniTools Inventory");
	}

	public InventoryMenu(String name) {
		this.name = name;
	}

	/**
	 * Number of slots available in the inventory to store items
	 * The bottom line is reserved for the inventory menu
	 * @return Number of slots available in the inventory to store items
	 */
	public int getUsableSize() {
		return 45;
	}

	/**
	 * Number of slots of the inventory
	 * @return Number of slots of the inventory
	 */
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
		menuItems = CustomItemStack.fromListObject(config.getList("menu_items"), true);
		menuItemEmpty = new MenuItem(CustomItemStack.fromObject(config.get("special_menu_items.empty")), null);
		menuItemLocked = CustomItemStack.fromObject(config.get("special_menu_items.locked"));
		menuItemUnlocked = CustomItemStack.fromObject(config.get("special_menu_items.unlocked"));

		// Load inventories
		@SuppressWarnings("unchecked")
		List<PageData> pagesData = (List<PageData>) config.getList("pages");
		if (pagesData == null) {
			int pageDefaultCount = config.getInt("pages_default_count", 1);
			setHoldersCount(pageDefaultCount, false);
		} else {
			// Create holders / Add items to their inventories
			setHoldersCount(pagesData.size(), true);
			for (int i = 0; i < holders.size(); i++) {
				// Fill inventories with pages data
				if (pagesData.get(i) != null) pagesData.get(i).apply(holders.get(i));
			}
		}

		dirty = false;
		if (!exists) setDirty(); // If didn't exist, set dirty to save it with intentories
	}

	private void setHoldersCount(int count, boolean canRemoveHolders) {
		if (count < 1) count = 1;
		if (count > holders.size()) {
			// Not enough inventories, create new ones
			for (int i = holders.size(); i < count; i++) {
				holders.add(new InventoryPage(this, i + 1));
			}
		} else if (count < holders.size()) {
			// Too many inventories, remove the last ones
			if (canRemoveHolders) {
				for (int i = holders.size() - 1; i >= count; i--) {
					holders.get(i).closeAll();
					holders.remove(i);
				}
			}
		}
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
		for (InventoryPage holder : holders) {
			holder.closeAll();
		}
		save();
		holders.clear();
	}

	public boolean open(HumanEntity player, int page) {
		int pageIndex = page - 1;
		if (pageIndex < 0 || pageIndex >= holders.size()) return false;
		InventoryPage holder = holders.get(pageIndex);
		if (!holder.isLoaded()) return false;
		holder.openInventory(player);
		return true;
	}

	public boolean isHolder(Inventory inventory) {
		return holders.stream().anyMatch(holder -> holder.getInventory().equals(inventory));
	}

	private MenuItem getMenuItem(int index, int page, InventoryPage holder) {
		if (index < 0 || index >= menuItems.size()) return null;
		ItemStack item = menuItems.get(index);

		switch (index) {
			case 0: // Left slot
				if (page == 1 || item == null)
					return menuItemEmpty; // No previous page
				else {
					item = item.clone();
					item.setAmount(page - 1);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(meta.getDisplayName().replace("%page%", page - 1 + ""));
					item.setItemMeta(meta);
					return new MenuItem(item, event -> open(event.getWhoClicked(), page - 1));
				}

			case 4: // Middle slot
				if (holder.isLocked()) {
					return new MenuItem(menuItemLocked, event -> holder.setLocked(false));
				} else {
					return new MenuItem(menuItemUnlocked, event -> holder.setLocked(true));
				}

			case 8: // Right slot
				if (page == holders.size() || item == null)
					return menuItemEmpty; // No next page
				else {
					item = item.clone();
					item.setAmount(page + 1);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(meta.getDisplayName().replace("%page%", page + 1 + ""));
					item.setItemMeta(meta);
					return new MenuItem(item, event -> open(event.getWhoClicked(), page + 1));
				}
		}

		// Other slots
		return item == null ? menuItemEmpty : new MenuItem(item, null);
	}

	public void updateInventoryMenu(InventoryPage holder) {
		if (menuItems == null) return;
		int begin = getUsableSize();
		int length = Math.min(menuItems.size(), getSize() - begin);
		int pageIndex = holders.indexOf(holder);
		for (int i = 0; i < length; i++) {
			MenuItem item = getMenuItem(i, pageIndex + 1, holder);
			holder.setItem(i + begin, item);
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
		InventoryPage holder = new InventoryPage(this, holders.size() + 1);
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
			InventoryPage current = holders.get(j);
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
			InventoryPage previous = holders.get(i);
			for (int j = i; j < holders.size(); j++) {
				InventoryPage current = holders.get(j);
				previous.load(current.getInventory().getContents().clone());
				previous = current;
			}
			previous.load(lastContents);
		}

		setDirty();

		return true;
	}
}
