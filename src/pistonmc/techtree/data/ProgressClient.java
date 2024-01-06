package pistonmc.techtree.data;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import pistonmc.techtree.ModMain;
import pistonmc.techtree.adapter.INetworkClient;
import pistonmc.techtree.event.Msg;
import pistonmc.techtree.event.MsgPostObtainItem;
import pistonmc.techtree.event.MsgPostReadPage;

public class ProgressClient {
    /** See README */
    static enum ItemState {
        HIDDEN,
        DISCOVERED,
        UNLOCKED,
        COMPLETABLE,
        COMPLETED,
    }

    static enum CategoryState {
        HIDDEN,
        DISCOVERED,
        UNLOCKED,
        PROGRESSED,
        COMPLETED,
    }

    private TechTree tree;
    /** Client cache of all item entry states */
    private HashMap<String, ItemState> states;
    /** Client cache of all category entry states */
    private HashMap<String, CategoryState> categoryStates;

    private PlayerProgress<ItemUnionUnpaged> progress;

    private INetworkClient<Msg> network;

    // init related state
    private boolean isInitializing = false;
    private long correlation = 0;
    private Queue<ItemSpecSingle> queue = new ArrayDeque<>();

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
    public void onInitObtainedItems(List<ItemSpec> items, long correlation, int expectedSize) {
        if (this.correlation != correlation) {
            // self is outdated, start from fresh
            this.progress.getObtained().clear();
            this.states.clear();
            this.categoryStates.clear();
            this.correlation = correlation;
            this.isInitializing = true;
        }
        ItemUnionUnpaged obtained = this.progress.getObtained();
        for (ItemSpec item: items) {
            obtained.union(item);
        }
        if (obtained.size() >= expectedSize) {
            // empty queued items
            this.isInitializing = false;
            this.refreshAllItems();
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

    public void onNewPage(String pageId) {
        this.progress.addNewPage(pageId);
    }

    public void onInitNewPages(List<String> pages) {
        this.progress.initNewPages(pages);
    }

    public void onReadPage(String pageId) {
        if (this.progress.removeNewPage(pageId)) {
            this.network.sendToServer(new MsgPostReadPage(pageId));
        }
    }

    private void refreshAllItems() {
        ModMain.log.info("Starting refresh all");
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
        if (dirtyItemIds.contains(item.id)) {
            return;
        }
        for (ItemData descendant: item.getDescendants()) {
            this.markItemDirty(descendant, dirtyItemIds);
        }
    }

    private void refreshDirtyItems(Set<String> dirtyItemIds) {
        Set<String> dirtyCategoryIds = new HashSet<>();
        for (String itemId: new ArrayList<>(dirtyItemIds)) {
            ItemData entry = this.tree.getItem(itemId);
            this.refreshItemStateAt(entry, dirtyItemIds, dirtyCategoryIds);
        }
        for (String category: dirtyCategoryIds) {
            this.refreshCategoryState(this.tree.getCategory(category));
        }
    }

    private void refreshItemStateAt(ItemData item, Set<String> dirtyItemIds, Set<String> dirtyCategoryIds) {
        if (!dirtyItemIds.contains(item.id)) {
            return;
        }
        ModMain.log.info("Refreshing: " + item.id);
        dirtyItemIds.remove(item.id);
        // ensure dependencies are clean
        for (ItemData dep: item.getDependencies()) {
            this.refreshItemStateAt(dep, dirtyItemIds, dirtyCategoryIds);
        }
        ItemState newState = this.computeStateForItem(item);
        ItemState oldState = this.getItemState(item.id);
        if (newState != oldState) {
            this.states.put(item.id, newState);
            dirtyCategoryIds.add(item.getCategoryId());
        }
    }

    /**
     * Computing state for entry. All dependencies must be up to date
     */
    private ItemState computeStateForItem(ItemData item) {
        ItemState newState = ItemState.DISCOVERED;
        boolean hasCompleted = false;
        boolean hasNotCompleted = false;
        // compute state
        outer: for (ItemData dep: item.getDependencies()) {
            ItemState depState = this.getItemState(dep.id);
            switch (depState) {
                // for item to be Discovered, all must be at least Unlocked
                case HIDDEN:
                case DISCOVERED:
                    hasNotCompleted = true;
                    newState = ItemState.HIDDEN;
                    break outer;
                case COMPLETED:
                    hasCompleted = true;
                    break;
                default:
                    hasNotCompleted = true;
                    break;
            }
        }
        if (newState == ItemState.DISCOVERED && hasCompleted) {
            newState = ItemState.UNLOCKED;
        }
        if (newState == ItemState.UNLOCKED && !hasNotCompleted) {
            newState = ItemState.COMPLETABLE;
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
     */
    private void refreshCategoryState(CategoryData category) {
        ModMain.log.info("Refreshing: " + category.id);
        boolean hasDiscovered = false;
        boolean hasUnlocked = false;
        boolean hasCompleted = false;
        boolean hasNotCompleted = false;
        for (ItemData item: category.items) {
            ItemState itemState = this.getItemState(item.id);
            switch (itemState) {
                case HIDDEN:
                    hasNotCompleted = true;
                    break;
                case DISCOVERED:
                    hasDiscovered = true;
                    hasNotCompleted = true;
                    break;
                case UNLOCKED:
                    hasUnlocked = true;
                    hasNotCompleted = true;
                    break;
                case COMPLETED:
                    hasCompleted = true;
                    break;
                case COMPLETABLE:
                    hasNotCompleted = true;
                    break;
            }
            if (hasCompleted && hasNotCompleted) {
                break;
            }
        }
        CategoryState state = CategoryState.HIDDEN;
        if (hasDiscovered) {
            state = CategoryState.DISCOVERED;
        }
        if (hasUnlocked) {
            state = CategoryState.UNLOCKED;
        }
        if (hasCompleted) {
            if (hasNotCompleted) {
                state = CategoryState.PROGRESSED;
            } else {
                state = CategoryState.COMPLETED;
            }
        }
        this.categoryStates.put(category.id, state);
    }

    public ItemState getItemState(String id) {
        return this.states.getOrDefault(id, ItemState.HIDDEN);
    }

    public CategoryState getCategoryState(String id) {
        return this.categoryStates.getOrDefault(id, CategoryState.HIDDEN);
    }
}
