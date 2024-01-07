package pistonmc.techtree.gui;

import pistonmc.techtree.adapter.IGuiHost;

public class GuiButton {
    private static final int WIDTH = 22;
    private static final int HEIGHT = 17;
    public static final int OVERLAY_LEFT = HEIGHT * 3;
    public static final int OVERLAY_RIGHT = HEIGHT * 2;
    public static final int OVERLAY_LIST = HEIGHT * 4;
    private IGuiHost host;
    /** (x,y) relative to the top-left corner of the GUI */
    private int x;
    private int y;

    private boolean visible;
    private int overlayV;
    private Runnable action;

    public GuiButton(IGuiHost host, int x, int y, int overlayV, Runnable action) {
        this.host = host;
        this.x = x;
        this.y = y;
        this.overlayV = overlayV;
        this.action = action;
        this.visible = true;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * If the mouse is over this button
     *
     * (mx, my) are relative to the top-left corner of the GUI
     */
    public boolean isMouseOver(int mx, int my) {
        if (!this.visible) {
            return false;
        }
        return mx >= this.x && mx < this.x + WIDTH && my >= this.y && my < this.y + HEIGHT;
    }

    /**
     * Draw the background
     *
     * (mx, my) are relative to the top-left corner of the GUI
     */
    public void drawBackground(int mx, int my) {
        if (!this.visible) {
            return;
        }
        int u = GuiTechTree.WIDTH;
        int v = 0;
        if (this.isMouseOver(mx, my)) {
            v = HEIGHT;
        }
        int left = this.host.getLeft() + this.x;
        int top = this.host.getTop() + this.y;
        this.host.drawTextureRect(left, top, u, v, WIDTH, HEIGHT);
        this.host.drawTextureRect(left, top, u, this.overlayV, WIDTH, HEIGHT);
    }

    /**
     * Called when the screen is clicked
     *
     * (mx, my) are relative to the top-left corner of the GUI
     */
    public void onScreenClick(int mx, int my) {
        if (!this.visible) {
            return;
        }
        if (this.isMouseOver(mx, my)) {
            this.host.playButtonSound();
            this.action.run();
        }
    }
}
