package pistonmc.techtree.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.util.StatCollector;
import pistonmc.techtree.adapter.IGuiHost;
import pistonmc.techtree.data.CategoryData;
import pistonmc.techtree.data.ItemData;
import pistonmc.techtree.data.ProgressClient;
import pistonmc.techtree.data.ProgressClient.CategoryState;

/**
 * Handler for the guide book GUI
 */
public class GuiTechTree {
    private static final String TEXTURE = "textures/gui/book2.png";
    private static final String PAGING_KEY = "techtree.gui.paging";
    private static final int GRID_LEFT = 6;
    private static final int GRID_TOP = 27;
    private static final int GRID_WIDTH = 100;
    private static final int GRID_HEIGHT = 160;

    private static final int CATEGORY_HEIGHT = 32;
    private static final int ITEM_SIZE = 20;

    public static final int CATEGORES_PER_PAGE = GRID_HEIGHT / CATEGORY_HEIGHT;
    public static final int ITEMS_PER_PAGE = (GRID_HEIGHT / ITEM_SIZE) * (GRID_WIDTH / ITEM_SIZE);

    public static final int WIDTH = 256;
    public static final int HEIGHT = 212;

    public static final int ITEM_BLOCK_V = HEIGHT + GuiButton.HEIGHT;

    public static final int PAGE_NUM_Y = 197;

    private GuiState state;
    private ProgressClient progress;
    private IGuiHost host;

    private List<GuiButton> buttons;
    private GuiButton contentPrevButton;
    private GuiButton contentNextButton;
    private GuiButton returnToIndexButton;
    private List<ItemData> displayedItems;
    private List<CategoryData> displayedCategories;
    private String currentDisplayedPageId;
    private List<GuiPage> currentDisplayedPages;

    public GuiTechTree(ProgressClient client, GuiState state, IGuiHost host) {
        this.progress = client;
        this.state = state;
        this.host = host;
        this.returnToIndexButton = new GuiButton(this.host, 117, 191, GuiButton.OVERLAY_LIST, this::handleReturnToIndex);
        this.contentPrevButton = new GuiButton(this.host, 207, 191, GuiButton.OVERLAY_LEFT, this::handleContentPrevPage);
        this.contentNextButton = new GuiButton(this.host, 229, 191, GuiButton.OVERLAY_RIGHT, this::handleContentNextPage);
        this.buttons = Arrays.asList(
            new GuiButton(this.host, 63, 191, GuiButton.OVERLAY_LEFT, this::handleListPrevPage),
            new GuiButton(this.host, 85, 191, GuiButton.OVERLAY_RIGHT, this::handleListNextPage),
            this.contentPrevButton,
            this.contentNextButton,
            this.returnToIndexButton
        );
        this.returnToIndexButton.setVisible(false);

        this.displayedItems = new ArrayList<>();
        this.displayedCategories = new ArrayList<>();

        if (this.state.isOnCategoryList()) {
            this.updateCategoryList();
        } else {
            this.updateItemList();
        }
    }

    /**
     * Called on mouse click
     *
     * (mx, my) is the mouse position on the screen
     */
    public void onMouseClick(int mx, int my, int mouseButton) {
        for (GuiButton button : this.buttons) {
            button.onClick(mx - this.host.getLeft(), my - this.host.getTop());
        }
    }

    /**
     * Draw the background
     *
     * (mx, my) is the mouse position on the screen
     */
    public void drawBackground(int mx, int my) {
        this.updateWidgets();
        int left = this.host.getLeft();
        int top = this.host.getTop();

        this.host.bindTexture(TEXTURE);
        this.host.drawTextureRect(left, top, 0, 0, WIDTH, HEIGHT);

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

    private void updateWidgets() {
        this.ensurePageContentSync();
        int displayedPageCount = this.currentDisplayedPages.size();

        this.contentPrevButton.setEnabled(this.state.currentContentPage > 0);
        this.contentNextButton.setEnabled(this.state.currentContentPage < displayedPageCount - 1);
    }

    /**
     * Draw the item layer
     *
     * (mx, my) is the mouse position on the screen
     */
    public void drawItemLayer(int mx, int my) {
    }

    /**
     * Draw the text layer
     *
     * (mx, my) is the mouse position on the screen
     */
    public void drawTextLayer(int mx, int my) {
        int left = this.host.getLeft();
        int top = this.host.getTop();
        // Page content
        GuiPage page = this.currentDisplayedPages.get(this.state.currentContentPage);
        page.drawPageText();

        // Page content page number
        int xRight = 203;
        String pageNumStr = StatCollector.translateToLocalFormatted(PAGING_KEY, this.state.currentContentPage + 1, this.currentDisplayedPages.size());
        this.host.drawString(pageNumStr, left + xRight - this.host.getStringWidth(pageNumStr), top + PAGE_NUM_Y);

    }

    /**
     * Draw the foreground (texture) layer. This is used to add checkmarks
     * and (!) marks as well as tooltips
     *
     * (mx, my) is the mouse position on the screen
     */
    public void drawForegroundLayer(int mx, int my) {
    }

    private void drawCategoriesBackground(int mx, int my) {
        int count = GRID_HEIGHT / CATEGORY_HEIGHT;
        int left = this.host.getLeft() + GRID_LEFT;
        int top = this.host.getTop() + GRID_TOP;
        for (int i = this.state.currentListStart; i < this.state.currentListStart + count; i++) {
            int j = i - this.state.currentListStart;
            CategoryData category = this.getDisplayCategoryData(i);
            int v;
            if (category == null) {
                // draw grey rect
                v = GRID_TOP;
            } else {
                CategoryState state = this.progress.getCategoryState(category.id);
                if (state == CategoryState.UNLOCKED || state == CategoryState.PROGRESSED) {
                    // draw normal
                    if (isOverCategorySlot(mx, my, j)) {
                        v = GRID_TOP + CATEGORY_HEIGHT * 4;
                    } else {
                        v = GRID_TOP + CATEGORY_HEIGHT * 2;
                    }
                } else {
                    // draw dark rect (completed or discovered)
                    v = GRID_TOP + CATEGORY_HEIGHT * 3;
                }
            }
            this.host.drawTextureRect(left, top + j * CATEGORY_HEIGHT, GRID_LEFT, v, GRID_WIDTH, CATEGORY_HEIGHT);
        }
    }

    private CategoryData getDisplayCategoryData(int index) {
        if (index >= 0 && index < this.displayedCategories.size()) {
            return this.displayedCategories.get(index);
        }
        return null;
    }

    private boolean isOverCategorySlot(int mx, int my, int index) {
        int left = this.host.getLeft() + GRID_LEFT;
        int top = this.host.getTop() + GRID_TOP + index * CATEGORY_HEIGHT;
        return mx >= left && mx < left + GRID_WIDTH && my >= top && my < top + CATEGORY_HEIGHT;
    }

    private void drawItemsBackground(int mx, int my) {
    }

    public void handleListPrevPage() {
        this.state.handleListPrevPage();
    }
    public void handleListNextPage() {
        int increment;
        int listSize;
        if (this.state.isOnCategoryList()) {
            increment = CATEGORES_PER_PAGE;
            listSize = this.displayedCategories.size();
        } else {
            increment = ITEMS_PER_PAGE;
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
    private void ensurePageContentSync() {
        String pageId = this.state.getCurrentPage();
        boolean updated = false;
        if (this.currentDisplayedPages == null || !pageId.equals(this.currentDisplayedPageId)) {
            String[] text = this.progress.getText(pageId);
            GuiPageBuilder builder = new GuiPageBuilder(this.host);
            this.currentDisplayedPages = builder.buildPages(text);
            updated = true;
        }
        this.state.ensureContentPageRange(this.currentDisplayedPages.size());
        if (updated) {
            this.currentDisplayedPages.get(this.state.currentContentPage).onSwitchTo();
        }
    }
    public void handleReturnToIndex() {
        this.state.handleReturnToIndex();
        this.updateCategoryList();
    }
    public void handleEnterCategory(String id) {
        this.state.handleEnterCategory(id);
        this.updateItemList();
    }
    public void handleEnterItem(String id) {
        this.state.handleEnterItem(id);
    }

    public void updateCategoryList() {
        this.displayedCategories = this.progress.getSortedVisibleCategories(this.state.isDebug);
        this.returnToIndexButton.setVisible(false);
    }
    public void updateItemList() {
        this.displayedItems = this.progress.getSortedVisibleItems(this.state.getCurrentPage(), this.state.isDebug);
        this.returnToIndexButton.setVisible(true);
    }
}
