package pistonmc.techtree.gui;

import pistonmc.techtree.data.ItemSpecSingle;

public class GuiItem {
    public int x;
    public int y;
    public ItemSpecSingle item;
    public int stackSize;

    public GuiItem(ItemSpecSingle item, int stackSize) {
        this.item = item;
        this.stackSize = stackSize;
    }
}
