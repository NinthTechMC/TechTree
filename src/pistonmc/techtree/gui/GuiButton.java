package pistonmc.techtree.gui;

import pistonmc.techtree.adapter.IGuiHost;

public class GuiButton {
    public static final int WIDTH = 22;
    public static final int HEIGHT = 17;
    public static final int OVERLAY_LEFT = WIDTH * 3;
    public static final int OVERLAY_RIGHT = WIDTH * 4;
    public static final int OVERLAY_LIST = WIDTH * 5;
    private IGuiHost host;
    /** (x,y) relative to the top-left corner of the GUI */
    private int x;
    private int y;

    private boolean visible;
    private boolean enabled;
    private int overlayU;
    private Runnable action;

    public GuiButton(IGuiHost host, int x, int y, int overlayU, Runnable action) {
        this.host = host;
        this.x = x;
        this.y = y;
        this.overlayU = overlayU;
        this.action = action;
        this.visible = true;
        this.enabled = true;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
        int u = 0;
        int v = GuiTechTree.HEIGHT;
        if (!this.enabled) {
            u = WIDTH;
        } else if (this.isMouseOver(mx, my)) {
            u = WIDTH * 2;
        }
        int left = this.host.getLeft() + this.x;
        int top = this.host.getTop() + this.y;
        this.host.drawTextureRect(left, top, u, v, WIDTH, HEIGHT);
        this.host.drawTextureRect(left, top, this.overlayU, v, WIDTH, HEIGHT);
    }

    /**
     * Called when the screen is clicked
     *
     * (mx, my) are relative to the top-left corner of the GUI
     */
    public void onClick(int mx, int my) {
        if (!this.visible || !this.enabled) {
            return;
        }
        if (this.isMouseOver(mx, my)) {
            this.host.playButtonSound();
            this.action.run();
        }
    }
}
