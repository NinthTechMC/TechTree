package pistonmc.techtree.gui;

import pistonmc.techtree.data.ItemSpecSingle;

public class GuiItem {
    /** (x,y) relative to the top-left corner of the GUI */
    public int x;
    public int y;
    public ItemSpecSingle item;
    public int stackSize;

    public GuiItem(ItemSpecSingle item, int stackSize) {
        this.item = item;
        this.stackSize = stackSize;
    }
}
