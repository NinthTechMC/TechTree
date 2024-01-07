package pistonmc.techtree.mc7.gui;

import java.util.List;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import pistonmc.techtree.ModInfo;
import pistonmc.techtree.adapter.IGuiHost;
import pistonmc.techtree.data.ProgressClient;
import pistonmc.techtree.gui.GuiItem;
import pistonmc.techtree.gui.GuiState;
import pistonmc.techtree.gui.GuiTechTree;

public class GuiHost extends GuiContainer implements IGuiHost {
    private GuiTechTree gui;
    public GuiHost(ProgressClient progress, GuiState state) {
        super(new FakeContainer());
        this.gui = new GuiTechTree(progress, state, this);
        this.xSize = GuiTechTree.WIDTH;
        this.ySize = GuiTechTree.HEIGHT;
    }


    @Override
    public void bindTexture(String texture) {
        this.mc.getTextureManager().bindTexture(new ResourceLocation(ModInfo.ID, texture));
    }

    @Override
    public void bindItemsTexture() {
        this.mc.getTextureManager().bindTexture(TextureMap.locationItemsTexture);
    }

    @Override
    public int getLeft() {
        return this.guiLeft;
    }

    @Override
    public int getTop() {
        return this.guiTop;
    }

    @Override
    public void drawTextureRect(int x, int y, int u, int v, int width, int height) {
        this.drawTexturedModalRect(x, y, u, v, width, height);
    }

    @Override
    public void playButtonSound() {
        this.mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
    }

    @Override
    public void drawScreen(int mx, int my, float z) {
        super.drawScreen(mx, my, z);
        GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		RenderHelper.disableStandardItemLighting();
		GL11.glPushMatrix();
		GL11.glTranslatef(guiLeft, guiTop, 0.0F);
		
		this.gui.drawForegroundLayer(mx, my);
		
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableStandardItemLighting();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float z, int mx, int my) {
        this.gui.drawBackground(mx, my);
    }

    @Override
	protected void drawGuiContainerForegroundLayer(int mx, int my) {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.gui.drawItemLayer(mx, my);
        this.gui.drawTextLayer(mx, my);
    }

    @Override
	protected void mouseClicked(int x, int y, int button) {
		super.mouseClicked(x, y, button);
        this.gui.onMouseClick(x, y, button);
    }


    @Override
    public void drawItem(GuiItem item) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void drawItemDarkened(GuiItem item) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void setGuiItemDisplaySlots(List<GuiItem> slots) {
        FakeContainer container = (FakeContainer) this.inventorySlots;
        container.setSlots(slots);
    }


    @Override
    public int getStringWidth(String text) {
        return this.fontRendererObj.getStringWidth(text);
    }


    @Override
    public int getCharWidth(char text) {
        return this.fontRendererObj.getCharWidth(text);
    }


    @Override
    public void drawString(String text, int x, int y) {
        this.fontRendererObj.drawString(text, x - guiLeft, y - guiTop, 0x202020);
    }
}
