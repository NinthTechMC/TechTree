package pistonmc.techtree.gui;

import java.util.ArrayList;
import java.util.List;
import pistonmc.techtree.adapter.IGuiHost;

/**
 * A page in the guide book to be displayed
 */
public class GuiPage {
    public static final int TEXT_LINE_HEIGHT = 11;
    public static final int PAGE_X = 115;
    public static final int PAGE_Y = 27;
    private IGuiHost host;

    /** computed render properties */
    public List<String> lines;
    public List<GuiItem> items;

    public GuiPage(IGuiHost host) {
        this.host = host;
        this.lines = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    public boolean isEmpty() {
        return this.lines.isEmpty() && this.items.isEmpty();
    }

    public void onSwitchTo() {
        this.host.setGuiItemDisplaySlots(this.items);
    }

    public void drawPageBackground() {
        int left = this.host.getLeft();
        int top = this.host.getTop();
        for (GuiItem item: items) {
            int itemSize = 16;
            this.host.drawTextureRect(left + item.x - 2, top + item.y - 2, 20 * 4, GuiTechTree.ITEM_BLOCK_V, itemSize + 4, itemSize + 4);
        }
    }

    public void drawPageText() {
        int x = this.host.getLeft() + PAGE_X;
        int y = this.host.getTop() + PAGE_Y;
        for (String line : this.lines) {
            this.host.drawString(line, x, y);
            y += TEXT_LINE_HEIGHT;
        }
    }

}
