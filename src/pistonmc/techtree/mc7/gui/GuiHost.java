package pistonmc.techtree.mc7.gui;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import pistonmc.techtree.ModInfo;
import pistonmc.techtree.adapter.IGuiHost;
import pistonmc.techtree.data.ProgressClient;
import pistonmc.techtree.gui.GuiState;
import pistonmc.techtree.gui.GuiTechTree;

public class GuiHost extends GuiContainer implements IGuiHost {
    static class StubContainer extends Container {
        @Override
        public boolean canInteractWith(EntityPlayer player) {
            return true;
        }
    }
    private GuiTechTree gui;
    public GuiHost(ProgressClient progress, GuiState state) {
        super(new StubContainer());
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
    protected void drawGuiContainerBackgroundLayer(float z, int mx, int my) {
        gui.drawBackground(mx, my);
    }
}
