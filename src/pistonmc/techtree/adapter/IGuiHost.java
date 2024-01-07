package pistonmc.techtree.adapter;

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
}
