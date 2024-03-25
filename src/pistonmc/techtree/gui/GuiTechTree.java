package pistonmc.techtree.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import libpiston.adapter.IMapReader;
import libpiston.adapter.IMapWriter;
import pistonmc.techtree.adapter.IGuiHost;
import pistonmc.techtree.data.CategoryData;
import pistonmc.techtree.data.DataEntry;
import pistonmc.techtree.data.ItemData;
import pistonmc.techtree.data.ProgressClient;
import pistonmc.techtree.data.ProgressClient.CategoryState;
import pistonmc.techtree.data.ProgressClient.ItemState;

/**
 * Handler for the guide book GUI
 */
public class GuiTechTree {
    static interface CategoryIterator {
        /** accepts index in the current page and data, returns true if should continue */
        boolean accept(int index, CategoryData category);
    }

    static interface ItemIterator {
        /** accepts index in the current page and data, returns true if should continue */
        boolean accept(int row, int col, ItemData item);
    }

    private GuiState state;
    private ProgressClient progress;
    private IGuiHost host;

    private List<GuiButton> buttons;
    private GuiButton listPrevButton;
    private GuiButton listNextButton;
    private GuiButton contentPrevButton;
    private GuiButton contentNextButton;
    private GuiButton returnToIndexButton;
    private List<ItemData> displayedItems;
    private List<CategoryData> displayedCategories;

    private String currentDisplayedPageId;
    private List<GuiPage> currentDisplayedPages;
    private GuiItem currentPageItem;
    private String currentPageTitle = "";

    public GuiTechTree(ProgressClient client, GuiState state, IGuiHost host) {
        this.progress = client;
        this.state = state;
        this.host = host;
        // @formatter:off
        this.returnToIndexButton = 
            new GuiButton(this.host, 117, 191, GuiConstants.OVERLAY_LIST, this::handleReturnToIndex);
        this.contentPrevButton = 
            new GuiButton(this.host, 207, 191, GuiConstants.OVERLAY_LEFT, this::handleContentPrevPage);
        this.contentNextButton = 
            new GuiButton(this.host, 229, 191, GuiConstants.OVERLAY_RIGHT, this::handleContentNextPage);
        this.listPrevButton = 
            new GuiButton(this.host, 63, 191, GuiConstants.OVERLAY_LEFT, this::handleListPrevPage);
        this.listNextButton = 
            new GuiButton(this.host, 85, 191, GuiConstants.OVERLAY_RIGHT, this::handleListNextPage);
        this.buttons = Arrays.asList(
            this.listPrevButton, 
            this.listNextButton,
            this.contentPrevButton, 
            this.contentNextButton, 
            this.returnToIndexButton);
        // @formatter:on

        this.displayedItems = new ArrayList<>();
        this.displayedCategories = new ArrayList<>();

        this.updateList();
    }

    /**
     * Called on mouse click
     *
     * (mx, my) is the mouse position on the screen
     */
    public void onMouseClick(int mx, int my, int mouseButton) {
        int left = this.host.getLeft();
        int top = this.host.getTop();
        for (GuiButton button : this.buttons) {
            if (button.onClick(mx - left, my - top)) {
                return;
            }
        }

        if (this.state.isOnCategoryList()) {
            this.handleCategoryListClick(mx, my, mouseButton);
        } else {
            this.handleItemListClick(mx, my, mouseButton);
        }
    }

    private void handleCategoryListClick(int mx, int my, int mouseButton) {
        this.iterateCategories((i, category) -> {
            if (category == null) {
                return true;
            }
            if (!this.isOverCategorySlot(mx, my, i)) {
                return true;
            }
            if (this.state.isDebug && this.host.isShiftDown()) {
                this.handleDebugComplete(category.id);
                return false;
            }
            this.handleEnterCategory(category.id);
            return false;
        });
    } 

    private void handleItemListClick(int mx, int my, int mouseButton) {
        this.iterateItems((r, c, item) -> {
            if (item == null) {
                return true;
            }
            if (!this.isOverItemSlot(mx, my, r, c)) {
                return true;
            }
            if (this.state.isDebug && this.host.isShiftDown()) {
                this.handleDebugComplete(item.id);
                return false;
            }
            if (this.state.getCurrentPage().equals(item.id)) {
                this.handleEnterCategory(item.getCategoryId());
            } else {
                this.handleEnterItem(item.id);
            }
            return false;
        });
    }

    /**
     * Draw the background
     *
     * (mx, my) is the mouse position on the screen
     */
    public void drawBackground(int mx, int my) {
        this.updateWidgets(mx, my);
        int left = this.host.getLeft();
        int top = this.host.getTop();

        this.host.bindTexture(GuiConstants.TEXTURE);
        this.host.drawTextureRect(left, top, 0, 0, GuiConstants.GUI_WIDTH, GuiConstants.GUI_HEIGHT);

        for (GuiButton button : this.buttons) {
            button.drawBackground(mx - left, my - top);
        }

        if (this.state.isOnCategoryList()) {
            this.drawCategoriesBackground(mx, my);
        } else {
            this.drawItemsBackground(mx, my);
        }

        // Page content
        GuiPage page = this.currentDisplayedPages.get(this.state.currentContentPage);
        page.drawPageBackground();
    }

    private void updateWidgets(int mx, int my) {
        this.ensurePageContentSync();
        if (this.state.isOnCategoryList()) {
            this.returnToIndexButton.setVisible(false);
        } else {
            this.returnToIndexButton.setVisible(true);
        }
        int displayedPageCount = this.currentDisplayedPages.size();
        if (displayedPageCount > 1) {
            this.contentPrevButton.setVisible(true);
            this.contentNextButton.setVisible(true);
            this.contentPrevButton.setEnabled(this.state.currentContentPage > 0);
            this.contentNextButton
                    .setEnabled(this.state.currentContentPage < displayedPageCount - 1);
        } else {
            this.contentPrevButton.setVisible(false);
            this.contentNextButton.setVisible(false);
        }

        int[] pageCountAndCurrentPage = this.getListPageCountAndCurrentPage();
        int pageCount = pageCountAndCurrentPage[0];
        if (pageCount > 1) {
            int currentPage = pageCountAndCurrentPage[1];
            this.listPrevButton.setVisible(true);
            this.listNextButton.setVisible(true);
            this.listPrevButton.setEnabled(currentPage > 0);
            this.listNextButton.setEnabled(currentPage < pageCount - 1);
        } else {
            this.listPrevButton.setVisible(false);
            this.listNextButton.setVisible(false);
        }
    }

    /**
     * Draw the item layer
     *
     * (mx, my) is the mouse position on the screen
     */
    public void drawItemLayer(int mx, int my) {
        if (this.currentPageItem != null) {
            this.host.drawItem(this.currentPageItem);
        }

        if (this.state.isOnCategoryList()) {
            this.drawCategoryIcons();
        } else {
            this.drawItemIcons();
        }
    }

    /**
     * Draw the text layer
     *
     * (mx, my) is the mouse position on the screen
     */
    public void drawTextLayer(int mx, int my) {
        int left = this.host.getLeft();
        int top = this.host.getTop();
        // Page title
        this.host.drawString(this.currentPageTitle, left + GuiConstants.PAGE_TITLE_X,
                top + GuiConstants.PAGE_TITLE_Y, GuiConstants.TEXT_COLOR);

        // Page content
        GuiPage page = this.currentDisplayedPages.get(this.state.currentContentPage);
        page.drawPageText();

        // Page content page number
        {
            int pageCount = this.currentDisplayedPages.size();
            if (pageCount > 1) {
                int xRight = GuiConstants.PAGE_NUM_X;
                String pageNumStr = this.host.translateFormatted(GuiConstants.PAGING_KEY, this.state.currentContentPage + 1, this.currentDisplayedPages.size());
                this.host.drawString(pageNumStr, left + xRight - this.host.getStringWidth(pageNumStr),
                    top + GuiConstants.PAGE_NUM_Y, GuiConstants.TEXT_COLOR);
            }
        }

        // list page number
        {
            int[] pageCountAndCurrentPage = this.getListPageCountAndCurrentPage();
            int pageCount = pageCountAndCurrentPage[0];
            if (pageCount > 1) {
                int currentPage = pageCountAndCurrentPage[1] + 1;
                int xRight = GuiConstants.GRID_PAGE_NUM_X;
                String pageNumStr = this.host.translateFormatted(GuiConstants.PAGING_SHORT_KEY, currentPage, pageCount);
                this.host.drawString(pageNumStr, left + xRight - this.host.getStringWidth(pageNumStr),
                    top + GuiConstants.PAGE_NUM_Y, GuiConstants.TEXT_COLOR);
            }
        }

        if (this.state.isOnCategoryList()) {
            this.drawCategoryText();
        } else {
            this.drawItemText();
            String currentPageId = this.state.getCurrentPage();
            String categoryId = this.progress.getCategoryFor(currentPageId);
            if (currentPageId.equals(categoryId)) {
                String progression = this.getCategoryProgressionString(categoryId);
                this.host.drawString(progression, left + GuiConstants.PAGE_TITLE_X, top + GuiConstants.PAGE_PROGRESSION_TITLE_Y, GuiConstants.PROGRESS_COLOR);
            }
        }

    }

    /**
     * Draw the foreground (texture) layer. This is used to add checkmarks and (!) marks
     *
     * (mx, my) is the mouse position on the screen
     */
    public void drawForegroundLayer(int mx, int my) {
        this.host.bindTexture(GuiConstants.TEXTURE);
        if (this.state.isOnCategoryList()) {
            this.drawCategoryForeground(mx, my);
        } else {
            this.drawItemForeground(mx, my);
        }
    }

    /**
     * Draw the tooltip layer
     *
     * (mx, my) is the mouse position on the screen
     */
    public void drawTooltipLayer(int mx, int my) {
        if (this.state.isOnCategoryList()) {
            this.drawCategoryTooltip(mx, my);
        } else {
            this.drawItemTooltip(mx, my);
        }
    }

    private void drawCategoriesBackground(int mx, int my) {
        int left = this.host.getLeft() + GuiConstants.GRID_LEFT;
        int top = this.host.getTop() + GuiConstants.GRID_TOP;
        this.iterateCategories((i, category) -> {
            int v;
            if (category == null) {
                // draw grey rect
                v = GuiConstants.GRID_TOP;
            } else {
                CategoryState state = this.progress.getCategoryState(category.id);
                if (state.shouldDisplayDarkened()) {
                    // draw dark rect
                    v = GuiConstants.GRID_TOP + GuiConstants.CATEGORY_HEIGHT * 2;
                } else {
                    // draw normal
                    if (isOverCategorySlot(mx, my, i)) {
                        v = GuiConstants.GRID_TOP + GuiConstants.CATEGORY_HEIGHT * 3;
                    } else {
                        v = GuiConstants.GRID_TOP + GuiConstants.CATEGORY_HEIGHT;
                    }
                }
            }
            this.host.drawTextureRect(left, top + i * GuiConstants.CATEGORY_HEIGHT,
                    GuiConstants.GRID_LEFT, v, GuiConstants.CATEGORY_WIDTH,
                    GuiConstants.CATEGORY_HEIGHT);
            return true;
        });
    }

    private void drawCategoryIcons() {
        int left = GuiConstants.GRID_LEFT;
        int top = GuiConstants.GRID_TOP;
        this.iterateCategories((i, category) -> {
            if (category == null) {
                return true;
            }
            GuiItem item = new GuiItem(category.data.icon, 1);
            item.x = left + GuiConstants.CATEGORY_ITEM_X;
            item.y = top + i * GuiConstants.CATEGORY_HEIGHT + GuiConstants.CATEGORY_ITEM_Y;
            CategoryState state = this.progress.getCategoryState(category.id);
            if (state.isReadable()) {
                // draw normal
                this.host.drawItem(item);
            } else {
                // draw dark (hidden (debug only) or discovered)
                this.host.drawItemDarkened(item);
            }
            return true;
        });
    }

    private void drawCategoryText() {
        int left = this.host.getLeft() + GuiConstants.GRID_LEFT;
        int top = this.host.getTop() + GuiConstants.GRID_TOP;
        this.iterateCategories((i, category) -> {
            if (category == null) {
                return true;
            }
            CategoryState state = this.progress.getCategoryState(category.id);
            String progression = this.getCategoryProgressionString(category.id);
            if (!progression.isEmpty()) {
                String[] parts = progression.split("/");
                String completed = parts[0];
                String total = parts[1];
                int sepWidth = this.host.getCharWidth('/');
                int sepX = (GuiConstants.CATEGORY_WIDTH - sepWidth)/2;
                int y = top + i * GuiConstants.CATEGORY_HEIGHT + GuiConstants.CATEGORY_PROGRESSION_Y;
                this.host.drawString("/", left + sepX, y, GuiConstants.PROGRESS_COLOR);
                int completedWidth = this.host.getStringWidth(completed);
                int compX = sepX - completedWidth;
                this.host.drawString(completed, left + compX, y, GuiConstants.PROGRESS_COLOR);
                this.host.drawString(total, left + sepX + sepWidth, y, GuiConstants.PROGRESS_COLOR);
            }
            if (this.state.isDebug) {
                String[] titles = category.data.title;
                String title = titles.length > 0 ? titles[0] : "";
                this.host.drawString(title, left, top + i * GuiConstants.CATEGORY_HEIGHT, GuiConstants.DEBUG_COLOR);
                this.host.drawString(state.toString(), left,
                        top + i * GuiConstants.CATEGORY_HEIGHT + 10, GuiConstants.DEBUG_COLOR);
            }
            return true;
        });
        boolean hasUnfinishedTutorial = this.progress.hasUnfinishedTutorial();
        if (hasUnfinishedTutorial) {
            if (!this.state.isDebug) {
                return;
            }
        }
        int color = hasUnfinishedTutorial ? GuiConstants.DEBUG_COLOR : GuiConstants.PROGRESS_COLOR;
        String progressionString = this.progress.getOverallProgressionString();
        this.host.drawString(progressionString, this.host.getLeft() + GuiConstants.PAGE_TITLE_X, this.host.getTop() + GuiConstants.PAGE_PROGRESSION_TITLE_Y, color);
    }

    private void drawCategoryForeground(int mx, int my) {
        int off = (GuiConstants.GRID_ITEM_SIZE - GuiConstants.ITEM_SIZE) / 2;
        int left = this.host.getLeft() + GuiConstants.GRID_LEFT - off;
        int top = this.host.getTop() + GuiConstants.GRID_TOP - off;

        this.iterateCategories((i, category) -> {
            if (category == null) {
                return true;
            }
            if (this.progress.hasNewPageInCategory(category.id)) {
                this.host.drawTextureRect(
                    left + GuiConstants.CATEGORY_ITEM_X + 1,
                    top + i * GuiConstants.CATEGORY_HEIGHT + GuiConstants.CATEGORY_ITEM_Y + 1,
                    GuiConstants.GRID_ITEM_SIZE * 6,
                    GuiConstants.ITEM_BLOCK_V,
                    GuiConstants.GRID_ITEM_SIZE,
                    GuiConstants.GRID_ITEM_SIZE);
            } else {
                CategoryState state = this.progress.getCategoryState(category.id);
                if (state == CategoryState.COMPLETED) {
                    this.host.drawTextureRect(
                        left + GuiConstants.CATEGORY_ITEM_X + 1,
                        top + i * GuiConstants.CATEGORY_HEIGHT + GuiConstants.CATEGORY_ITEM_Y + 1,
                        GuiConstants.GRID_ITEM_SIZE * 5,
                        GuiConstants.ITEM_BLOCK_V,
                        GuiConstants.GRID_ITEM_SIZE,
                        GuiConstants.GRID_ITEM_SIZE);
                }
            }

            return true;
        });

    }

    private void drawCategoryTooltip(int mx, int my) {
        this.iterateCategories((i, category) -> {
            if (category == null) {
                return true;
            }

            if (!this.isOverCategorySlot(mx, my, i)) {
                return true;
            }
            CategoryState state = this.progress.getCategoryState(category.id);
            List<String> tooltips = new ArrayList<>();
            if (!state.isReadable()) {
                tooltips.add(this.host.translate(GuiConstants.UNREADABLE_TITLE_KEY));
                tooltips.add(this.host.translate(GuiConstants.UNREADABLE_TOOLTIP_KEY));
            } else {
                for (String title: category.data.title) {
                    tooltips.add(title);
                }
            }

            if (this.state.isDebug) {
                List<String> debugList = new ArrayList<>();
                debugList.add("DEBUG");
                debugList.add("ID="+category.id);
                debugList.add("STATE="+state.toString());
                debugList.add("TUTORIAL="+category.data.isTutorial);
                debugList.add("TOTAL="+category.getNumItems());
                debugList.add("COMPLETED="+this.progress.getCategoryProgression(category.id));
                debugList.add(this.host.translate(GuiConstants.DEBUG_COMPLETE_KEY));
                for (String s: debugList) {
                    tooltips.add(this.host.translateFormatted(GuiConstants.DEBUG_FORMAT_KEY, s));
                }
            }

            if (tooltips.size() > 0) {
                this.host.drawTooltip(tooltips, mx, my);
            }

            return false;
        });
    }

    private void drawItemsBackground(int mx, int my) {
        int left = this.host.getLeft() + GuiConstants.GRID_LEFT;
        int top = this.host.getTop() + GuiConstants.GRID_TOP;
        String page = this.state.getCurrentPage();
        this.iterateItems((r, c,  item) -> {
            if (item == null) {
                // draw grey rect
                // @formatter:off
                this.host.drawTextureRect(
                    left + c * GuiConstants.GRID_ITEM_SIZE,
                    top + r * GuiConstants.GRID_ITEM_SIZE,
                    GuiConstants.GRID_LEFT, 
                    GuiConstants.GRID_TOP,
                    GuiConstants.GRID_ITEM_SIZE,
                    GuiConstants.GRID_ITEM_SIZE);
                // @formatter:on
                return true;
            }
            int u;
            ItemState state = this.progress.getItemState(item.id);
            if (state.isReadable()) {
                // draw normal
                if (page.equals(item.id)) {
                    u = GuiConstants.GRID_ITEM_SIZE * 3;
                } else if (isOverItemSlot(mx, my, r, c)) {
                    u = GuiConstants.GRID_ITEM_SIZE * 2;
                } else {
                    u = 0;
                }
            } else {
                // draw dark rect
                u = GuiConstants.GRID_ITEM_SIZE;
            }
            // @formatter:off
            this.host.drawTextureRect(
                left + c * GuiConstants.GRID_ITEM_SIZE,
                top + r * GuiConstants.GRID_ITEM_SIZE,
                u, 
                GuiConstants.ITEM_BLOCK_V,
                GuiConstants.GRID_ITEM_SIZE,
                GuiConstants.GRID_ITEM_SIZE);
            return true;
        });
    }

    public void drawItemIcons() {
        int off = (GuiConstants.GRID_ITEM_SIZE - GuiConstants.ITEM_SIZE) / 2;
        int left = GuiConstants.GRID_LEFT + off;
        int top = GuiConstants.GRID_TOP + off;
        this.iterateItems((r, c, item) -> {
            if (item == null) {
                return true;
            }
            ItemState state = this.progress.getItemState(item.id);
            GuiItem guiItem = new GuiItem(item.data.icon, 1);
            guiItem.x = left + c * GuiConstants.GRID_ITEM_SIZE;
            guiItem.y = top + r * GuiConstants.GRID_ITEM_SIZE;
            if (state.shouldDisplayDarkened()) {
                this.host.drawItemDarkened(guiItem);
            } else {
                this.host.drawItem(guiItem);
            }
            return true;
        });
    }

    private void drawItemText() {
        int left = this.host.getLeft() + GuiConstants.GRID_LEFT;
        int top = this.host.getTop() + GuiConstants.GRID_TOP;
        this.iterateItems((r, c, item) -> {
            if (item == null) {
                return true;
            }
            if (this.state.isDebug) {
                ItemState state = this.progress.getItemState(item.id);
                this.host.drawString(state.toString().substring(0, 1), left + c * GuiConstants.GRID_ITEM_SIZE,
                        top + r * GuiConstants.GRID_ITEM_SIZE, GuiConstants.DEBUG_COLOR);
            }
            return true;
        });
    }

    private void drawItemForeground(int mx, int my) {
        int left = this.host.getLeft() + GuiConstants.GRID_LEFT;
        int top = this.host.getTop() + GuiConstants.GRID_TOP;

        this.iterateItems((r, c, item) -> {
            if (item == null) {
                return true;
            }
            if (this.progress.isNewPage(item.id)) {
                this.host.drawTextureRect(
                    left + c * GuiConstants.GRID_ITEM_SIZE + 1,
                    top + r * GuiConstants.GRID_ITEM_SIZE + 1,
                    GuiConstants.GRID_ITEM_SIZE * 6,
                    GuiConstants.ITEM_BLOCK_V,
                    GuiConstants.GRID_ITEM_SIZE,
                    GuiConstants.GRID_ITEM_SIZE);
            } else {
                ItemState state = this.progress.getItemState(item.id);
                if (state == ItemState.COMPLETED) {
                    this.host.drawTextureRect(
                        left + c * GuiConstants.GRID_ITEM_SIZE + 1,
                        top + r * GuiConstants.GRID_ITEM_SIZE + 1,
                        GuiConstants.GRID_ITEM_SIZE * 5,
                        GuiConstants.ITEM_BLOCK_V,
                        GuiConstants.GRID_ITEM_SIZE,
                        GuiConstants.GRID_ITEM_SIZE);
                }
            }

            return true;
        });
    }

    private void drawItemTooltip(int mx, int my) {
        this.iterateItems((r, c, item) -> {
            if (item == null) {
                return true;
            }

            if (!this.isOverItemSlot(mx, my, r, c)) {
                return true;
            }
            ItemState state = this.progress.getItemState(item.id);
            List<String> tooltips = new ArrayList<>();
            if (state.isReadable()) {
                for (String title: item.data.title) {
                    tooltips.add(title);
                }
            } else if (state.shouldDisplayDarkened()) {
                tooltips.add(this.host.translate(GuiConstants.UNREADABLE_TITLE_KEY));
                tooltips.add(this.host.translate(GuiConstants.UNREADABLE_TOOLTIP_KEY));
            } else {
                String title = item.data.title.length > 0 ? item.data.title[0] : "";
                tooltips.add(title);
                tooltips.add(this.host.translate(GuiConstants.UNLOCKED_TOOLTIP_KEY));
            }

            if (this.state.isDebug) {
                List<String> debugList = new ArrayList<>();
                debugList.add("DEBUG");
                debugList.add("ID="+item.id);
                debugList.add("STATE="+state.toString());
                debugList.add(this.host.translate(GuiConstants.DEBUG_COMPLETE_KEY));
                for (String s: debugList) {
                    tooltips.add(this.host.translateFormatted(GuiConstants.DEBUG_FORMAT_KEY, s));
                }
            }

            if (tooltips.size() > 0) {
                this.host.drawTooltip(tooltips, mx, my);
            }

            return false;
        });
    }

    public void handleListPrevPage() {
        this.state.handleListPrevPage();
    }

    public void handleListNextPage() {
        int increment;
        int listSize;
        if (this.state.isOnCategoryList()) {
            increment = GuiConstants.CATEGORIES_PER_PAGE;
            listSize = this.displayedCategories.size();
        } else {
            increment = GuiConstants.ITEMS_PER_PAGE;
            listSize = this.displayedItems.size();
        }
        this.state.handleListNextPage(listSize, increment);
    }

    public void handleContentPrevPage() {
        this.ensurePageContentSync();
        this.state.handleContentPrevPage();
        this.currentDisplayedPages.get(this.state.currentContentPage).onSwitchTo();
    }

    public void handleContentNextPage() {
        this.ensurePageContentSync();
        this.state.handleContentNextPage(this.currentDisplayedPages.size());
        this.currentDisplayedPages.get(this.state.currentContentPage).onSwitchTo();
    }

    public void handleReturnToIndex() {
        this.state.handleReturnToIndex();
        this.updateList();
    }

    public void handleEnterCategory(String id) {
        CategoryState state = this.progress.getCategoryState(id);
        if (!state.isReadable() && !this.state.isDebug) {
            return;
        }
        this.host.playButtonSound();
        this.progress.onReadPage(id);
        this.state.handleEnterCategory(id);
        this.updateList();
    }

    public void handleEnterItem(String id) {
        ItemState state = this.progress.getItemState(id);
        if (!state.isReadable() && !this.state.isDebug) {
            return;
        }
        this.host.playButtonSound();
        this.progress.onReadPage(id);
        this.state.handleEnterItem(id);
    }

    private void handleDebugComplete(String id) {
        this.host.playButtonSound();
        this.progress.onDebugComplete(id);
        this.updateList();
    }

    public void updateList() {
        if (this.state.isOnCategoryList()) {
            this.displayedCategories = this.progress.getSortedVisibleCategories(this.state.isDebug);
            this.state.ensureListStartRange(this.displayedCategories.size());
            return;
        }
        String categoryId = this.progress.getCategoryFor(this.state.getCurrentPage());
        this.displayedItems = this.progress.getSortedVisibleItems(categoryId, this.state.isDebug);
        this.state.ensureListStartRange(this.displayedItems.size());
    }

    /**
     * Iterate through displayed categories on the current page,
     * and perform the action on each (index, data). Index is the position
     * on the current page.
     */
    private void iterateCategories(CategoryIterator consumer) {
        int count = GuiConstants.GRID_HEIGHT / GuiConstants.CATEGORY_HEIGHT;
        for (int i = this.state.currentListStart; i < this.state.currentListStart + count; i++) {
            int j = i - this.state.currentListStart;
            boolean shouldContinue;
            if (i >= 0 && i < this.displayedCategories.size()) {
                CategoryData category = this.displayedCategories.get(i);
                shouldContinue = consumer.accept(j, category);
            } else {
                shouldContinue = consumer.accept(j, null);
            }
            if (!shouldContinue) {
                break;
            }
        }
    }

    private boolean isOverCategorySlot(int mx, int my, int index) {
        int left = this.host.getLeft() + GuiConstants.GRID_LEFT;
        int top = this.host.getTop() + GuiConstants.GRID_TOP + index * GuiConstants.CATEGORY_HEIGHT;
        return mx >= left && mx < left + GuiConstants.GRID_WIDTH && my >= top
                && my < top + GuiConstants.CATEGORY_HEIGHT;
    }

    private void iterateItems(ItemIterator consumer) {
        int rows = GuiConstants.GRID_HEIGHT / GuiConstants.GRID_ITEM_SIZE;
        int cols = GuiConstants.GRID_WIDTH / GuiConstants.GRID_ITEM_SIZE;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int j = r * cols + c;
                int i = this.state.currentListStart + j;
                boolean shouldContinue;
                if (i >= 0 && i < this.displayedItems.size()) {
                    ItemData item = this.displayedItems.get(i);
                    shouldContinue = consumer.accept(r, c, item);
                } else {
                    shouldContinue = consumer.accept(r, c, null);
                }
                if (!shouldContinue) {
                    return;
                }
            }
        }
    }

    private boolean isOverItemSlot(int mx, int my, int r, int c) {
        int left = this.host.getLeft() + GuiConstants.GRID_LEFT + c * GuiConstants.GRID_ITEM_SIZE;
        int top = this.host.getTop() + GuiConstants.GRID_TOP + r * GuiConstants.GRID_ITEM_SIZE;
        return mx >= left && mx < left + GuiConstants.GRID_ITEM_SIZE && my >= top
                && my < top + GuiConstants.GRID_ITEM_SIZE;
    }

    /**
     * Update the page list to be synced with the current page id
     */
    private void ensurePageContentSync() {
        String pageId = this.state.getCurrentPage();
        boolean updated = false;
        if (this.currentDisplayedPages == null || !pageId.equals(this.currentDisplayedPageId)) {
            String[] text = this.progress.getText(pageId);
            GuiPageBuilder builder = new GuiPageBuilder(this.host);
            this.currentDisplayedPages = builder.buildPages(text);
            this.currentDisplayedPageId = pageId;
            this.state.currentContentPage = 0;
            updated = true;
        }
        this.state.ensureContentPageRange(this.currentDisplayedPages.size());
        if (updated) {
            this.currentDisplayedPages.get(this.state.currentContentPage).onSwitchTo();
            DataEntry data = this.progress.getData(pageId);
            if (data != null) {
                this.currentPageTitle = data.title.length > 0 ? data.title[0] : "";
                this.currentPageItem = new GuiItem(data.icon, 1);
                this.currentPageItem.x = GuiConstants.PAGE_ITEM_X;
                this.currentPageItem.y = GuiConstants.PAGE_ITEM_Y;
            } else {
                this.currentPageTitle = "";
                this.currentPageItem = null;
            }
        }
    }

    private int[] getListPageCountAndCurrentPage() {
        int listSize;
        int listPageSize;
        if (this.state.isOnCategoryList()) {
            listSize = this.displayedCategories.size();
            listPageSize = GuiConstants.CATEGORIES_PER_PAGE;
        } else {
            listSize = this.displayedItems.size();
            listPageSize = GuiConstants.ITEMS_PER_PAGE;
        }

        int pageCount = listSize / listPageSize + (listSize % listPageSize == 0 ? 0 : 1);
        int currentPage = this.state.currentListStart / listPageSize;
        return new int[] { pageCount, currentPage };
    }

    private String getCategoryProgressionString(String categoryId) {
        int progression = this.progress.getCategoryProgression(categoryId);
        if (progression <= 0) {
            return "";
        }
        CategoryData category = this.progress.getCategory(categoryId);
        int total = category.getNumItems();
        return progression + "/" + total; 
    }

    public void saveState(IMapWriter writer) {
        IMapWriter subWriter = writer.writeMap("TTGuiState");
        this.state.writeToMap(subWriter);
    }

    public static GuiState readState(IMapReader reader, boolean isDebug) {
        GuiState state = new GuiState(isDebug);
        IMapReader subReader = reader.readMap("TTGuiState");
        if (subReader != null) {
            state.readFromMap(subReader);
        }
        return state;
    }

}
