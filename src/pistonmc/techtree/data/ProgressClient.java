package pistonmc.techtree.data;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import pistonmc.techtree.ModMain;
import pistonmc.techtree.adapter.INetworkClient;
import pistonmc.techtree.event.Msg;
import pistonmc.techtree.event.MsgPostNewPages;
import pistonmc.techtree.event.MsgPostObtainItem;
import pistonmc.techtree.event.MsgPostReadPage;

public class ProgressClient {
    /** See README */
    public static enum ItemState {
        HIDDEN,
        DISCOVERED,
        UNLOCKED,
        COMPLETABLE,
        COMPLETED;

        public boolean isReadable() {
            return this == COMPLETABLE || this == COMPLETED;
        }

        public boolean shouldDisplayDarkened() {
            return this == HIDDEN || this == DISCOVERED;
        }

        public int sortOrder() {
            switch (this) {
                case COMPLETABLE:
                    return 0;
                case UNLOCKED:
                    return 1;
                case DISCOVERED:
                    return 2;
                case COMPLETED:
                    return 3;
                case HIDDEN:
                    return 4;
                default:
                    return 9;
            }
        }
    }

    public static enum CategoryState {
        HIDDEN,
        DISCOVERED,
        UNLOCKED,
        PROGRESSED,
        COMPLETED;

        public boolean isReadable() {
            return this == UNLOCKED || this == PROGRESSED || this == COMPLETED;
        }

        public boolean shouldDisplayDarkened() {
            return this == HIDDEN || this == DISCOVERED;
        }

        public int sortOrder() {
            switch (this) {
                case PROGRESSED:
                case UNLOCKED:
                    return 1;
                case DISCOVERED:
                    return 2;
                case COMPLETED:
                    return 3;
                case HIDDEN:
                    return 4;
                default:
                    return 9;
            }
        }
    }

    private TechTree tree;
    private PlayerProgress<ItemUnionUnpaged> progress;
    private INetworkClient<Msg> network;

    // init related state
    private boolean isInitializing = false;
    private long correlation = 0;
    private Queue<ItemSpecSingle> queue = new ArrayDeque<>();

    // client cache
    private HashMap<String, ItemState> states;
    private HashMap<String, CategoryState> categoryStates;
    private boolean isCompleted = false;
    private boolean isTutorialCompleted = false;

    public ProgressClient(TechTree tree, INetworkClient<Msg> network) {
        this.tree = tree;
        this.network = network;
        this.states = new HashMap<>();
        this.categoryStates = new HashMap<>();
        this.progress = new PlayerProgress<>(new ItemUnionUnpaged());
    }

    /**
     * Called when receiving init event from server
     */
    public void onInit(List<ItemSpec> items, long correlation, int expectedSize, List<String> initPages) {
        ModMain.log.info("Received init event with correlation " + correlation + " and expected size " + expectedSize);
        if (this.correlation != correlation) {
            // self is outdated, start from fresh
            this.progress.getObtained().clear();
            this.progress.clearNewPages();
            this.states.clear();
            this.categoryStates.clear();
            this.correlation = correlation;
            this.isInitializing = true;
        }
        ItemUnionUnpaged obtained = this.progress.getObtained();
        for (ItemSpec item: items) {
            obtained.union(item);
        }
        if (!initPages.isEmpty()) {
            ModMain.log.info("Received "+ initPages.size() + " new pages");
            this.progress.addNewPages(initPages);
        }
        if (obtained.size() >= expectedSize) {
            ModMain.log.info("Received all items, finishing init");
            // update initial state
            this.refreshAllItems();
            // empty queued items
            this.isInitializing = false;
            for (ItemSpecSingle item: this.queue) {
                this.addObtainedItem(item);
            }
        }
    }

    /**
     * Called when receiving item obtain event from server
     */
    public void onObtainItem(ItemSpecSingle item) {
        if (this.isInitializing) {
            this.queue.add(item);
            return;
        }
        this.addObtainedItem(item);
    }

    public void onClientObtainItem(ItemSpecSingle item) {
        // if we are initializing, the obtained state here could be incomplete,
        // so we use the server to determine the true state
        if (this.isInitializing || this.canObtainItem(item)) {
            this.network.sendToServer(new MsgPostObtainItem(item));
        }
    }

    public void onReadPage(String pageId) {
        if (this.progress.removeNewPage(pageId)) {
            this.network.sendToServer(new MsgPostReadPage(pageId));
        }
    }

    /**
     * Called when completing a page using debug mode
     */
    public void onDebugComplete(String id) {
        ItemData item = this.tree.getItem(id);
        if (item == null) {
            // try category
            CategoryData category = this.tree.getCategory(id);
            if (category == null) {
                ModMain.log.error("Cannot find item or category: " + id);
                return;
            }
            for (ItemData i: category.items) {
                this.onDebugCompleteItem(i);
            }
            return;
        }
        this.onDebugCompleteItem(item);
    }

    private void onDebugCompleteItem(ItemData item) {
        ItemSpec[] items = item.data.items;
        if (items.length == 0) {
            return;
        }
        ModMain.log.info("Debug completing item: " + item.id);
        ItemSpecSingle single = items[0].toSingle();
        this.onClientObtainItem(single);
    }

    public void refreshAllItems() {
        ModMain.log.info("Starting refreshing all items");
        long start = System.currentTimeMillis();
        Set<String> itemIds = this.tree.getAllEntryIds();
        this.refreshDirtyItems(itemIds);
        long end = System.currentTimeMillis();
        ModMain.log.info("Refresh done in " + (end - start) + "ms");
    }

    private void addObtainedItem(ItemSpecSingle item) {
        if (!this.canObtainItem(item)) {
            return;
        }

        ModMain.log.info("Adding obtained item: " + item.toString());
        ItemUnionUnpaged obtained = this.progress.getObtained();
        obtained.union(item);
        ModMain.log.info("Starting refresh");
        long start = System.currentTimeMillis();
        this.refreshItemState(item);
        long end = System.currentTimeMillis();
        ModMain.log.info("Refresh done in " + (end - start) + "ms");
    }

    public boolean canObtainItem(ItemSpecSingle item) {
        if (!this.tree.isItemInvolved(item)) {
            // item is not in tech tree at all
            return false;
        }
        ItemUnionUnpaged obtained = this.progress.getObtained();
        if (obtained.contains(item)) {
            // item is already recorded
            return false;
        }
        return true;
    }

    private void refreshItemState(ItemSpecSingle item) {
        Set<String> dirtyItemIds = new HashSet<>();
        // get the entries that could potentialy be updated directly
        Set<String> entries = this.tree.getEntryIdsForItem(item);
        for (String id: entries) {
            ItemData entry = this.tree.getItem(id);
            // does the new item affect this entry?
            for (ItemSpec s: entry.data.items) {
                if (s.contains(item)) {
                    markItemDirty(entry, dirtyItemIds);
                    break;
                }
            }
        }
        this.refreshDirtyItems(dirtyItemIds);
    }

    /**
     * Mark the item as having a dirty state.
     *
     * All its descendants will be marked dirty as well
     */
    private void markItemDirty(ItemData item, Set<String> dirtyItemIds) {
        if (!dirtyItemIds.add(item.id)) {
            return;
        }
        for (ItemData descendant: item.getDescendants()) {
            this.markItemDirty(descendant, dirtyItemIds);
        }
    }

    private void refreshDirtyItems(Set<String> dirtyItemIds) {
        ModMain.log.info("Refreshing " + dirtyItemIds.size() + " dirty items");
        Set<String> dirtyCategoryIds = new HashSet<>();
        List<String> newPages = new ArrayList<>();
        Set<String> dirtyItemIdsCopy = new HashSet<>(dirtyItemIds);
        for (String itemId: new ArrayList<>(dirtyItemIds)) {
            ItemData entry = this.tree.getItem(itemId);
            this.refreshItemStateAt(entry, dirtyItemIdsCopy, dirtyCategoryIds, newPages);
        }
        boolean categoryChanged = false;
        for (String category: dirtyCategoryIds) {
            if (this.refreshCategoryState(this.tree.getCategory(category), newPages)) {
                categoryChanged = true;
            }
        }
        if (!newPages.isEmpty()) {
            ModMain.log.info("New pages: " + newPages);
            this.progress.addNewPages(newPages);
            this.network.sendToServer(new MsgPostNewPages(newPages));
        }
        if (categoryChanged) {
            boolean isTutorialCompleted = true;
            boolean isCompleted = true;
            for (CategoryData category: this.tree.getCategories().values()) {
                boolean completed = this.getCategoryState(category.id) == CategoryState.COMPLETED;
                if (!completed) {
                    isCompleted = false;
                    if (category.data.isTutorial) {
                        isTutorialCompleted = false;
                    }
                }
            }
            this.isCompleted = isCompleted;
            this.isTutorialCompleted = isTutorialCompleted;
        }
    }

    private void refreshItemStateAt(ItemData item, Set<String> dirtyItemIds, Set<String> dirtyCategoryIds, List<String> newPages) {
        if (!dirtyItemIds.contains(item.id)) {
            return;
        }
        ModMain.log.info("Refreshing: " + item.id);
        dirtyItemIds.remove(item.id);
        // ensure dependencies are clean
        for (ItemData dep: item.getDependencies()) {
            this.refreshItemStateAt(dep, dirtyItemIds, dirtyCategoryIds, newPages);
        }
        ItemState newState = this.computeStateForItem(item);
        ItemState oldState = this.getItemState(item.id);
        if (newState != oldState) {
            if (!this.isInitializing && newState.isReadable() && !oldState.isReadable()) {
                // item is now readable
                newPages.add(item.id);
            }
            this.states.put(item.id, newState);
            dirtyCategoryIds.add(item.getCategoryId());
        }
    }

    /**
     * Computing state for entry. All dependencies must be up to date
     */
    private ItemState computeStateForItem(ItemData item) {
        ItemState newState = ItemState.DISCOVERED;
        boolean hasBelowUnlocked = false;
        boolean hasBelowCompletable = false;
        boolean allCompleted = true;
        // compute state
        if (item.getDependencies().length == 0) {
            newState = ItemState.COMPLETABLE;
        } else {
            outer: for (ItemData dep: item.getDependencies()) {
                ItemState depState = this.getItemState(dep.id);
                if (depState != ItemState.COMPLETED) {
                    allCompleted = false;
                }
                switch (depState) {
                    // for item to be Discovered, all must be at least Unlocked
                    case HIDDEN:
                    case DISCOVERED:
                        hasBelowUnlocked = true;
                        hasBelowCompletable = true;
                        newState = ItemState.HIDDEN;
                        break outer;
                    case UNLOCKED:
                        hasBelowCompletable = true;
                        break;
                    default:
                        break;
                }
            }
        }
        // use new state based on dependency states
        if (allCompleted) {
            newState = ItemState.COMPLETABLE;
        } else if (hasBelowUnlocked) {
            newState = ItemState.HIDDEN;
        } else if (hasBelowCompletable) {
            newState = ItemState.DISCOVERED;
        } else {
            newState = ItemState.UNLOCKED;
        }

        if (newState == ItemState.COMPLETABLE) {
            ItemUnionUnpaged obtained = this.progress.getObtained();
            for (ItemSpec s: item.data.items) {
                if (obtained.contains(s)) {
                    newState = ItemState.COMPLETED;
                    break;
                }
            }
        }
        if (newState != ItemState.COMPLETED && item.isHiddenDependency) {
            newState = ItemState.HIDDEN;
        }
        return newState;
    }

    /**
     * Refresh a category's state. All items states must be up to date
     *
     * Returns if any category changed state
     */
    private boolean refreshCategoryState(CategoryData category, List<String> newPages) {
        ModMain.log.info("Refreshing: " + category.id);
        CategoryState state = CategoryState.HIDDEN;
        if (category.items.length > 0) {
            boolean hasNotCompleted = false;
            boolean hasCompleted = false;
            boolean hasUnlocked = false;
            boolean hasDiscovered = false;
            for (ItemData item: category.items) {
                ItemState itemState = this.getItemState(item.id);
                switch (itemState) {
                    case COMPLETED:
                        hasCompleted = true;
                        break;
                    case HIDDEN:
                        hasNotCompleted = true;
                        break;
                    case DISCOVERED:
                        hasDiscovered = true;
                        hasNotCompleted = true;
                        break;
                    case UNLOCKED:
                    case COMPLETABLE:
                        hasUnlocked = true;
                        hasNotCompleted = true;
                        break;
                }
                if (hasCompleted && hasNotCompleted) {
                    // enough to determine state
                    break;
                }
            }
            if (!hasNotCompleted) {
                state = CategoryState.COMPLETED;
            } else if (hasCompleted) {
                state = CategoryState.PROGRESSED;
            } else if (hasUnlocked) {
                state = CategoryState.UNLOCKED;
            } else if (hasDiscovered) {
                state = CategoryState.DISCOVERED;
            }
        }
        CategoryState oldState = this.getCategoryState(category.id);
        if (state != oldState) {
            if (!this.isInitializing && state.isReadable() && !oldState.isReadable()) {
                // category is now readable
                newPages.add(category.id);
            }
            this.categoryStates.put(category.id, state);
        }
        return state != oldState;
    }

    public ItemState getItemState(String id) {
        return this.states.getOrDefault(id, ItemState.HIDDEN);
    }

    public CategoryState getCategoryState(String id) {
        return this.categoryStates.getOrDefault(id, CategoryState.HIDDEN);
    }

    public int getCategoryProgression(String id) {
        CategoryData category = this.tree.getCategory(id);
        if (category == null) {
            return 0;
        }
        int count = 0;
        for (ItemData item: category.items) {
            ItemState itemState = this.getItemState(item.id);
            if (itemState == ItemState.COMPLETED) {
                count++;
            }
        }
        return count;
    }

    public boolean hasUnfinishedTutorial() {
        return !this.isTutorialCompleted;
    }

    public boolean isCompleted() {
        return this.isCompleted && this.progress.getNewPages().isEmpty();
    }

    public String getOverallProgressionString() {
        int completed = 0;
        int total = 0;
        for (Entry<String, CategoryData> category: this.tree.getCategories().entrySet()) {
            CategoryState state = this.getCategoryState(category.getKey());
            CategoryData data = category.getValue();
            if (data.items.length <= 0 || "index".equals(data.id)) {
                // skip empty categories, especially index
                continue;
            }
            if (state == CategoryState.COMPLETED) {
                completed++;
            }
            total++;
        }
        return completed + "/" + total;
    }

    public String[] getText(String id) {
        return this.tree.getText(id);
    }

    public DataEntry getData(String id) {
        return this.tree.getData(id);
    }

    public CategoryData getCategory(String id) {
        return this.tree.getCategory(id);
    }

    public boolean hasNewPages() {
        return this.progress.hasNewPage();
    }

    public boolean isNewPage(String pageId) {
        return this.progress.getNewPages().contains(pageId);
    }

    public boolean hasNewPageInCategory(String categoryId) {
        if (this.isNewPage(categoryId)) {
            return true;
        }
        String prefix = categoryId + ".";
        for (String pageId: this.progress.getNewPages()) {
            if (pageId.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public String getCategoryFor(String id) {
        ItemData item = this.tree.getItem(id);
        if (item != null) {
            return item.getCategoryId();
        }
        return id;
    }

    public List<CategoryData> getSortedVisibleCategories(boolean isDebug) {
        List<CategoryData> categories = new ArrayList<>(this.categoryStates.size());
        for (Entry<String, CategoryData> category: this.tree.getCategories().entrySet()) {
            CategoryState state = this.getCategoryState(category.getKey());
            CategoryData data = category.getValue();
            if (data.items.length <= 0 || "index".equals(data.id)) {
                // skip empty categories, especially index
                continue;
            }
            if (isDebug || state != CategoryState.HIDDEN) {
                categories.add(data);
            }
        }
        // sort
        // categories are sorted in the order of 
        // UNLOCKED/PROGRESSED, DISCOVERED, COMPLETED
        // within each group, sort by id alphabetically
        Collections.sort(categories, (a, b) -> {
            int orderA = this.getCategoryState(a.id).sortOrder();
            int orderB = this.getCategoryState(b.id).sortOrder();
            if (orderA != orderB) {
                return orderA - orderB;
            }

            return a.id.compareTo(b.id);
        });

        return categories;
    }

    public List<ItemData> getSortedVisibleItems(String categoryId, boolean isDebug) {
        CategoryData category = this.tree.getCategory(categoryId);
        List<ItemData> items = new ArrayList<>(category.items.length);

        for (ItemData item: category.items) {
            if (isDebug || this.getItemState(item.id) != ItemState.HIDDEN) {
                items.add(item);
            }
        }

        Collections.sort(items, (a, b) -> {
            int orderA = this.getItemState(a.id).sortOrder();
            int orderB = this.getItemState(b.id).sortOrder();
            if (orderA != orderB) {
                return orderA - orderB;
            }

            return a.id.compareTo(b.id);
        });

        return items;
    }
}
