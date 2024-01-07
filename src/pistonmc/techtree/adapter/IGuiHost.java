package pistonmc.techtree.adapter;

import java.util.List;
import pistonmc.techtree.gui.GuiItem;

public interface IGuiHost {
    public void bindTexture(String texture);
    public void bindItemsTexture();
    public int getLeft();
    public int getTop();
    /**
     * Draw a texture rectangle
     * (x, y) is where to draw the rectangle on the screen
     * (u, v) is the top-left corner of the rectangle in the texture
     */
    public void drawTextureRect(int x, int y, int u, int v, int width, int height);

    public void playButtonSound();

    public void drawItem(GuiItem item);
    public void drawItemDarkened(GuiItem item);

    /**
     * Set a list of items to be displayed as if they are in a container slots
     * (i.e. with highlighting and tooltips when hovered)
     */
    public void setGuiItemDisplaySlots(List<GuiItem> slots);

    public int getStringWidth(String text);
    public int getCharWidth(char text);

    /**
     * Draw text
     * (x, y) is relative to the screen
     */
    public void drawString(String text, int x, int y);
}
