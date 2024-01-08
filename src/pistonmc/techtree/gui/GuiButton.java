package pistonmc.techtree.gui;

import pistonmc.techtree.adapter.IGuiHost;

public class GuiButton {
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
        return mx >= this.x && mx < this.x + GuiConstants.BUTTON_WIDTH && my >= this.y && my < this.y + GuiConstants.BUTTON_HEIGHT;
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
        int v = GuiConstants.GUI_HEIGHT;
        if (!this.enabled) {
            u = GuiConstants.BUTTON_WIDTH;
        } else if (this.isMouseOver(mx, my)) {
            u = GuiConstants.BUTTON_WIDTH * 2;
        }
        int left = this.host.getLeft() + this.x;
        int top = this.host.getTop() + this.y;
        this.host.drawTextureRect(left, top, u, v, GuiConstants.BUTTON_WIDTH, GuiConstants.BUTTON_HEIGHT);
        this.host.drawTextureRect(left, top, this.overlayU, v, GuiConstants.BUTTON_WIDTH, GuiConstants.BUTTON_HEIGHT);
    }

    /**
     * Called when the screen is clicked
     *
     * (mx, my) are relative to the top-left corner of the GUI
     *
     * return if the button is triggered
     */
    public boolean onClick(int mx, int my) {
        if (!this.visible || !this.enabled) {
            return false;
        }
        if (this.isMouseOver(mx, my)) {
            this.host.playButtonSound();
            this.action.run();
            return true;
        }
        return false;
    }
}
