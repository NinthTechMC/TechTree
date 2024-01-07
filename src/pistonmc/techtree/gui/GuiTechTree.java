package pistonmc.techtree.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import pistonmc.techtree.adapter.IGuiHost;
import pistonmc.techtree.data.CategoryData;
import pistonmc.techtree.data.ItemData;
import pistonmc.techtree.data.ProgressClient;
import pistonmc.techtree.data.ProgressClient.CategoryState;

/**
 * Handler for the guide book GUI
 */
public class GuiTechTree {
    private static final String TEXTURE = "textures/gui/book.png";
    private static final int GRID_LEFT = 6;
    private static final int GRID_TOP = 27;
    private static final int GRID_WIDTH = 100;
    private static final int GRID_HEIGHT = 160;

    private static final int CATEGORY_HEIGHT = 32;
    private static final int ITEM_SIZE = 20;

    public static final int CATEGORES_PER_PAGE = GRID_HEIGHT / CATEGORY_HEIGHT;
    public static final int ITEMS_PER_PAGE = (GRID_HEIGHT / ITEM_SIZE) * (GRID_WIDTH / ITEM_SIZE);

    public static final int WIDTH = 222;
    public static final int HEIGHT = 212;

    private GuiState state;
    private ProgressClient progress;
    private IGuiHost host;

    private List<GuiButton> buttons;
    private GuiButton returnToIndexButton;
    private List<ItemData> displayedItems;
    private List<CategoryData> displayedCategories;

    public GuiTechTree(ProgressClient client, GuiState state, IGuiHost host) {
        this.progress = client;
        this.state = state;
        this.host = host;
        this.returnToIndexButton = new GuiButton(this.host, 85, 6, GuiButton.OVERLAY_LIST, this::handleReturnToIndex);
        this.buttons = Arrays.asList(
            new GuiButton(this.host, 5, 191, GuiButton.OVERLAY_LEFT, this::handleListPrevPage),
            new GuiButton(this.host, 85, 191, GuiButton.OVERLAY_RIGHT, this::handleListNextPage),
            new GuiButton(this.host, 116, 191, GuiButton.OVERLAY_LEFT, this::handleContentPrevPage),
            new GuiButton(this.host, 195, 191, GuiButton.OVERLAY_RIGHT, this::handleContentNextPage),
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
     * Draw the background
     *
     * (mx, my) is the mouse position on the screen
     */
    public void drawBackground(int mx, int my) {
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
    }

    /**
     * Draw the category and item widgets
     *
     * (mx, my) is the mouse position on the screen
     */
    public void drawWidgets(int mx, int my) {
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
        this.state.handleContentPrevPage();
    }
    public void handleContentNextPage() {
        String pageId = this.state.getCurrentPage();
        String[][] pages = this.progress.getText(pageId);
        this.state.handleContentNextPage(pages.length);
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
