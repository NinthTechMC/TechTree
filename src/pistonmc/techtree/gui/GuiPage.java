package pistonmc.techtree.gui;

import java.util.ArrayList;
import java.util.List;
import pistonmc.techtree.adapter.IGuiHost;

/**
 * A page in the guide book to be displayed
 */
public class GuiPage {
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
            this.host.drawTextureRect(left + item.x - 2, top + item.y - 2, 20 * 4, GuiConstants.ITEM_BLOCK_V, itemSize + 4, itemSize + 4);
        }
    }

    public void drawPageText() {
        int x = this.host.getLeft() + GuiConstants.PAGE_X;
        int y = this.host.getTop() + GuiConstants.PAGE_Y;
        for (String line : this.lines) {
            this.host.drawString(line, x, y, GuiConstants.TEXT_COLOR);
            y += GuiConstants.TEXT_LINE_HEIGHT;
        }
    }

}
