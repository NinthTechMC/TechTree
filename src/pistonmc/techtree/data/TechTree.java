package pistonmc.techtree.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.relauncher.Side;
import pistonmc.techtree.ModMain;
import pistonmc.techtree.adapter.IConfigHost;
import pistonmc.techtree.adapter.ILocaleLoader;

public class TechTree {
    /** Keep item text cached for 5 minutes */
    private static final long ITEM_CACHE_DURATION = 1000 * 5 * 60;


	private ILocaleLoader localeLoader;
	private IConfigHost configHost;
	private long lastCacheTime = 0;
    public boolean hasError = false;
    /** text entry id (category and item) to text section */
    private final HashMap<String, String[]> textCache = new HashMap<>();
    /** category id to data */
    private final HashMap<String, CategoryData> categories = new HashMap<>();
    /** item full id to data */
    private final HashMap<String, ItemData> items = new HashMap<>();
    /** item modid:name to (full) entry ids */
    private final HashMap<String, HashSet<String>> itemNameToEntryId = new HashMap<>();

    private final ItemUnionUnpaged involvedItems = new ItemUnionUnpaged();
	

    public TechTree(ILocaleLoader localeLoader, IConfigHost gamePath) {
    	this.localeLoader = localeLoader;
    	this.configHost = gamePath;
    }
	
	public boolean reload() {
		ModMain.log.info("Loading config");
        this.hasError = false;
		long start = System.currentTimeMillis();
        HashMap<String, DataEntry> categories = this.loadCategories();
        if (this.hasError) {
            return false;
        }
        HashMap<String, DataEntry> items = this.loadItems(categories);
        if (this.hasError) {
            return false;
        }
        this.detectDependencyCycle(items);
        if (this.hasError) {
            return false;
        }
        this.createItemGraph(items);
        if (this.hasError) {
            return false;
        }
        this.createCategoryGraph(categories);
        if (this.hasError) {
            return false;
        }

        this.textCache.clear();
        File indexFile = getLocalizedRootDirectory().resolve("index.txt").toFile();
        if (!indexFile.exists()) {
            ModMain.log.warn("index.txt does not exist. Creating it...");
            try {
                indexFile.createNewFile();
            } catch (IOException e) {
                ModMain.error(e);
            }
        }
        DataEntry indexEntry = DataEntry.readFrom(indexFile);
        if (indexEntry == null) {
            ModMain.log.error("Failed to load index");
            this.hasError = true;
            return false;
        }
        this.categories.put("index", new CategoryData("index", indexEntry, new ItemData[0]));
		ModMain.log.info("Config loaded in " + (System.currentTimeMillis() - start) + "ms");
        return true;
	}

    private HashMap<String, DataEntry> loadCategories() {
        HashMap<String, DataEntry> categories = new HashMap<>();
        File localeRoot = getLocalizedRootDirectory().toFile();
        File[] entries = localeRoot.listFiles();
        if (entries == null) {
            ModMain.log.error("Failed to load categories from: " + localeRoot.getAbsolutePath());
            this.hasError = true;
            return categories;
        }
        for (File entry: entries) {
            String name = entry.getName();
            if (name.equals("index.txt")) {
                // index is treated specially later
                continue;
            }
            if (!name.endsWith(".txt")) {
                continue;
            }
            ModMain.log.info("Loading category: " + name);
            String categoryId = name.substring(0, name.length()-4);
            DataEntry data = DataEntry.readFrom(entry);
            if (data == null) {
                ModMain.log.error("Failed to load category: " + categoryId);
                this.hasError = true;
                continue;
            }
            categories.put(categoryId, data);
        }
        ModMain.log.info("Loaded " + categories.size() + " categories");
        return categories;
    }

    private HashMap<String, DataEntry> loadItems(HashMap<String, DataEntry> categories) {
        HashMap<String, DataEntry> items = new HashMap<>();
        for (String categoryId: categories.keySet()) {
            File categoryDir = getCategoryDirectory(categoryId).toFile();
            File[] entries = categoryDir.listFiles();
            if (entries == null) {
                ModMain.log.error("Failed to load items from: " + categoryDir.getAbsolutePath());
                this.hasError = true;
                continue;
            }
            for (File file: entries) {
                String name = file.getName();
                if (!name.endsWith(".txt")) {
                    continue;
                }
                String itemId = name.substring(0, name.length()-4);
                String fullId = getFullId(categoryId, itemId);
                ModMain.log.info("Loading item: " + fullId);
                DataEntry itemData = DataEntry.readFrom(file);
                if (itemData == null) {
                    ModMain.log.error("Failed to load item: " + fullId);
                    this.hasError = true;
                    continue;
                }
                items.put(fullId, itemData);
            }
        }
        ModMain.log.info("Loaded " + items.size() + " items");
        this.involvedItems.clear();
        this.itemNameToEntryId.clear();
        for (Entry<String, DataEntry> e: items.entrySet()) {
            ItemSpec[] entryItems = e.getValue().items;
            for (ItemSpec i: entryItems) {
                this.involvedItems.union(i);
                String name = i.getNamespacedId();
                HashSet<String> entryIds = this.itemNameToEntryId.get(name);
                if (entryIds == null) {
                    entryIds = new HashSet<>();
                    this.itemNameToEntryId.put(name, entryIds);
                }
                entryIds.add(e.getKey());
            }

        }
        ModMain.log.info("Loaded " + this.involvedItems.size() + " involved item names");
        return items;
    }

	private void detectDependencyCycle(HashMap<String, DataEntry> items) {
		ArrayList<String> stack = new ArrayList<>();
		HashSet<String> inStack = new HashSet<>();
		HashSet<String> visited = new HashSet<>();
		for (String key: items.keySet()) {
			this.detectDependencyCycleAt(items, key, stack, inStack, visited);
		}
	}
	
	private void detectDependencyCycleAt(HashMap<String, DataEntry> items, String key, ArrayList<String> stack, HashSet<String> inStack, HashSet<String> visited) {
		if (visited.contains(key)) {
			return;
		}
		visited.add(key);
		DataEntry e = items.get(key);
		if (e == null) {
			return;
		}

		stack.add(key);
		inStack.add(key);
		for (String d: e.after) {
			if (inStack.contains(d)) {
				ModMain.log.error("Dependency cycle detected: " + stack +" (visiting: " + d + ")");
                this.hasError = true;
				continue;
			}
			if (!visited.contains(d)) {
				this.detectDependencyCycleAt(items, d, stack, inStack, visited);
			}
		}
		inStack.remove(key);
		stack.remove(stack.size()-1);
	}

    private void createItemGraph(HashMap<String, DataEntry> items) {
        ModMain.log.info("Creating item graph");
        this.items.clear();
        HashMap<String, ArrayList<String>> descendants = new HashMap<>();

        for (String itemId: items.keySet()) {
            descendants.put(itemId, new ArrayList<>());
        }

        HashSet<String> visited = new HashSet<>();
        for (Entry<String, DataEntry> item: items.entrySet()) {
            this.buildItemGraphNodeAt(item.getKey(), item.getValue(), items, descendants, visited);
        }

        for (Entry<String, ArrayList<String>> des: descendants.entrySet()) {
            ItemData item = this.items.get(des.getKey());
            ItemData[] d = new ItemData[des.getValue().size()];
            for (int i = 0; i < d.length; i++) {
                d[i] = this.items.get(des.getValue().get(i));
            }
            item.setDescendants(d);
        }

        int count = 0;
        for (ItemData item: this.items.values()) {
            ItemData[] des = item.getDescendants();
            if (des == null) {
                ModMain.log.error("Item has no descendants: " + item.id);
                this.hasError = true;
                continue;
            }
            count += des.length;
        }

        ModMain.log.info(count + " descendant edges created");

        int count2 = 0;
        for (ItemData item: this.items.values()) {
            ItemData[] dep = item.getDependencies();
            if (dep == null) {
                ModMain.log.error("Item has no descendants: " + item.id);
                this.hasError = true;
                continue;
            }
            count2 += dep.length;
        }
        ModMain.log.info(count2 + " dependency edges created");
        if (count != count2) {
            ModMain.log.error("Descendant edge count does not match dependency edge count");
            this.hasError = true;
        }
    }

    private ItemData buildItemGraphNodeAt(
        String id, 
        DataEntry data, 
        HashMap<String, DataEntry> items, 
        HashMap<String, ArrayList<String>> descendants,
        HashSet<String> visited
    ) {
        if (visited.contains(id)) {
            return this.items.get(id);
        }
        visited.add(id);
        List<ItemData> dependencies = new ArrayList<>(data.after.length);
        String categoryId = id.substring(0, id.indexOf('.'));
        boolean isHiddenDependency = false;
        for (int i = 0; i < data.after.length; i++) {
            String depId = data.after[i];
            if (depId.startsWith(".")) {
                depId = categoryId + depId;
            } else if (depId.equalsIgnoreCase("hidden")) {
                isHiddenDependency = true;
                continue;
            }
            ItemData dep = this.items.get(depId);
            if (dep == null) {
                DataEntry depData = items.get(depId);
                if (depData == null) {
                    ModMain.log.error("Dependency not found: " + depId + " (in " + id + ")");
                    this.hasError = true;
                    return null;
                }
                dep = this.buildItemGraphNodeAt(depId, depData, items, descendants, visited);
                if (dep == null) {
                    ModMain.log.error("Failed to build dependency graph: " + depId + " (in " + id + ")");
                    ModMain.log.error("This is likely due to circular dependencies, which is somehow not caught above?");
                    this.hasError = true;
                    return null;
                }
            }
            dependencies.add(dep);
            descendants.get(depId).add(id);
        }

        ItemData[] depArray = dependencies.toArray(new ItemData[dependencies.size()]);
        ItemData itemData = new ItemData(id, this, data, depArray, isHiddenDependency);
        this.items.put(id, itemData);
        return itemData;
    }

    private void createCategoryGraph(HashMap<String, DataEntry> categories) {
        HashMap<String, ArrayList<ItemData>> childrenLists = new HashMap<>();
        for (String categoryId: categories.keySet()) {
            childrenLists.put(categoryId, new ArrayList<>());
        }
        for (Entry<String, ItemData> item: this.items.entrySet()) {
            String itemFullId = item.getKey();
            String categoryId = itemFullId.substring(0, itemFullId.indexOf('.'));
            childrenLists.get(categoryId).add(item.getValue());
        }
        this.categories.clear();
        for (Entry<String, DataEntry> e: categories.entrySet()) {
            String categoryId = e.getKey();
            ArrayList<ItemData> children = childrenLists.get(categoryId);
            ItemData[] childrenArray = children.toArray(new ItemData[children.size()]);
            CategoryData category = new CategoryData(categoryId, e.getValue(), childrenArray);
            this.categories.put(categoryId, category);
        }
    }
	
	public ItemData getItem(String fullId) {
        return this.items.get(fullId);
    }

    public CategoryData getCategory(String categoryId) {
        return this.categories.get(categoryId);
    }

    public DataEntry getData(String fullId) {
        ItemData item = this.getItem(fullId);
        if (item == null) {
            CategoryData category = this.getCategory(fullId);
            if (category != null) {
                return category.data;
            }
            return null;
        }
        return item.data;

    }

	public CategoryData getIndex() {
		return this.getCategory("index");
	}
	
    @SideOnly(Side.CLIENT)
	public String[] getText(String id) {
		long currentTime = System.currentTimeMillis();
		if (this.lastCacheTime == 0 || currentTime - lastCacheTime > ITEM_CACHE_DURATION) {
			this.lastCacheTime = currentTime;
			this.textCache.clear();
		}
		String[] text = textCache.get(id);
		if (text != null) {
			return text;
		}
        int i = id.indexOf('.');
        File file;
        if (i < 0) {
            file = this.getCategoryFile(id);
        } else {
            file = this.getItemFile(id.substring(0, i), id.substring(i+1));
        }
        text = DataEntry.readTextFrom(file);
        textCache.put(id, text);
        return text;
	}
	
	public static String getFullId(String categoryId, String itemId) {
		return categoryId + "." + itemId;
	}

    public boolean isItemInvolved(ItemSpec item) {
        return this.involvedItems.contains(item);
    }

    public boolean isItemInvolved(ItemSpecSingle item) {
        return this.involvedItems.contains(item);
    }

    public Map<String, CategoryData> getCategories() {
        return this.categories;
    }

    /**
     * Get a set of entry ids that could (potentially) be completed by obtaining the given item
     */
    public Set<String> getEntryIdsForItem(ItemSpecSingle item) {
        Set<String> s = this.itemNameToEntryId.get(item.getNamespacedId());
        if (s != null) {
            return s;
        }
        return Collections.emptySet();
    }

    public Set<String> getAllEntryIds() {
        return this.items.keySet();
    }
	
	public File getItemFile(String categoryId, String itemId) {
		return getCategoryDirectory(categoryId).resolve(itemId+".txt").toFile();
	}
	
	public File getCategoryFile(String categoryId) {
		return getLocalizedRootDirectory().resolve(categoryId+".txt").toFile();
	}
	
	public Path getCategoryDirectory(String categoryId) {
		return getLocalizedRootDirectory().resolve(categoryId);
	}
	
	public Path getLocalizedRootDirectory() {
		Path root = configHost.getConfigRoot();
		String locale = localeLoader.getCurrentLocale();
		Path dir = root.resolve(locale);
		if (dir.toFile().exists()) {
			return dir;
		}
		ModMain.log.warn("Locale " + locale + " does not exist. Defaulting to en_US");
		dir = root.resolve("en_US");
		if (!dir.toFile().exists()) {
            dir.toFile().mkdirs();
		}
        return dir;
	}
	
	
}
