package pistonmc.techtree.gui;

import libpiston.item.ParsedItem;
import pistonmc.techtree.data.ItemSpec;

public class GuiItem {
    /** (x,y) relative to the top-left corner of the GUI */
    public int x;
    public int y;
    public ParsedItem item;

    public GuiItem(ParsedItem item) {
        this.item = item;
    }

    public GuiItem(ItemSpec item, int stackSize) {
        this.item = new ParsedItem(item.modid, item.name, item.getMeta(), null, stackSize);
    }
}
