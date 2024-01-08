package pistonmc.techtree.gui;

public interface GuiConstants {
    int GUI_WIDTH = 256;
    int GUI_HEIGHT = 212;

    // general
    int ITEM_SIZE = 16;

    // button
    int BUTTON_WIDTH = 22;
    int BUTTON_HEIGHT = 17;
    int OVERLAY_LEFT = BUTTON_WIDTH * 3;
    int OVERLAY_RIGHT = BUTTON_WIDTH * 4;
    int OVERLAY_LIST = BUTTON_WIDTH * 5;

    // page
    int PAGE_X = 115;
    int PAGE_Y = 26;
    int PAGE_ITEM_X = 7;
    int PAGE_ITEM_Y = 6;
    int PAGE_TITLE_X = 30;
    int PAGE_TITLE_Y = 6;
    int PAGE_PROGRESSION_TITLE_Y = 17;
    int PAGE_HEIGHT = 154;
    int TEXT_LINE_WIDTH = 142;
    int TEXT_LINE_HEIGHT = 11;
    int ITEM_LINE_WIDTH = 6; // how many items max per line
    int ITEM_XOFF = -2;
    int ITEM_YOFF = (TEXT_LINE_HEIGHT * 2 - ITEM_SIZE) / 2; // offset of items from top of line
    int PAGE_ITEM_SPACING = 6;
    int PAGE_NUM_X = 203;
    int PAGE_NUM_Y = 197;

    // grid
    int GRID_LEFT = 6;
    int GRID_TOP = 27;
    int GRID_WIDTH = 100;
    int GRID_HEIGHT = 160;
    int CATEGORY_HEIGHT = 40;
    int CATEGORY_WIDTH = GRID_WIDTH;
    int CATEGORY_ITEM_X = 42;
    int CATEGORY_ITEM_Y = 8;
    int CATEGORY_PROGRESSION_Y = 29;
    int GRID_ITEM_SIZE = 20;
    int CATEGORIES_PER_PAGE = GRID_HEIGHT / CATEGORY_HEIGHT;
    int ITEMS_PER_PAGE = (GRID_HEIGHT / GRID_ITEM_SIZE) * (GRID_WIDTH / GRID_ITEM_SIZE);
    int ITEM_BLOCK_V = GUI_HEIGHT + BUTTON_HEIGHT;
    int GRID_PAGE_NUM_X = 59;

    // color
    int TEXT_COLOR = 0x202020;
    int DEBUG_COLOR = 0xFF0000;
    int PROGRESS_COLOR = 0x006600;


    String TEXTURE = "textures/gui/book.png";
    String PAGING_KEY = "techtree.gui.paging";
    String PAGING_SHORT_KEY = "techtree.gui.paging_short";
    String UNREADABLE_TITLE_KEY = "techtree.gui.unreadable_title";
    String UNREADABLE_TOOLTIP_KEY = "techtree.gui.unreadable_tooltip";
    String UNLOCKED_TOOLTIP_KEY = "techtree.gui.unlocked_tooltip";
    String DEBUG_FORMAT_KEY = "techtree.gui.debug.format";
    String DEBUG_COMPLETE_KEY = "techtree.gui.debug.complete";
}
