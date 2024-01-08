package pistonmc.techtree.mc7.gui;

import java.util.List;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import pistonmc.techtree.ModInfo;
import pistonmc.techtree.adapter.IGuiHost;
import pistonmc.techtree.data.ProgressClient;
import pistonmc.techtree.gui.GuiConstants;
import pistonmc.techtree.gui.GuiItem;
import pistonmc.techtree.gui.GuiState;
import pistonmc.techtree.gui.GuiTechTree;
import pistonmc.techtree.mc7.ItemGuideBook;
import pistonmc.techtree.mc7.data.NBTTagCompoundWrapper;

public class GuiHost extends GuiContainer implements IGuiHost {
    private GuiTechTree gui;
    public GuiHost(ProgressClient progress, GuiState state) {
        super(new FakeContainer());
        this.gui = new GuiTechTree(progress, state, this);
        this.xSize = GuiConstants.GUI_WIDTH;
        this.ySize = GuiConstants.GUI_HEIGHT;
    }

    public void onGuiClosed() {
        super.onGuiClosed();
        if (this.mc.thePlayer != null) {
            ItemStack stack = this.mc.thePlayer.getCurrentEquippedItem();
            if (stack == null) {
                return;
            }
            if (!(stack.getItem() instanceof ItemGuideBook)) {
                return;
            }
            NBTTagCompound tag;
            if (stack.hasTagCompound()) {
                tag = stack.getTagCompound();
            } else {
                tag = new NBTTagCompound();
                stack.setTagCompound(tag);
            }
            this.gui.saveState(new NBTTagCompoundWrapper(tag));
        }
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
		
        this.zLevel = 400;
		this.gui.drawTooltipLayer(mx, my);
		
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableStandardItemLighting();
        this.zLevel = 0;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float z, int mx, int my) {
        this.zLevel = 0;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.gui.drawBackground(mx, my);
    }

    @Override
	protected void drawGuiContainerForegroundLayer(int mx, int my) {
        super.drawGuiContainerForegroundLayer(mx, my);

        this.zLevel = 100;
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        RenderHelper.enableGUIStandardItemLighting();
        float oldItemRenderZLevel = itemRender.zLevel;
        itemRender.zLevel = 150;
        this.gui.drawItemLayer(mx, my);
        itemRender.zLevel = oldItemRenderZLevel;
        GL11.glPopAttrib();
        GL11.glPopMatrix();

        this.zLevel = 200;
        this.gui.drawTextLayer(mx, my);

        this.zLevel = 300;
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        // force translate back so it's consistent with background layer
        GL11.glTranslatef((float)-this.guiLeft, (float)-this.guiTop, 0.0F);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.gui.drawForegroundLayer(mx, my);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    @Override
	protected void mouseClicked(int x, int y, int button) {
		super.mouseClicked(x, y, button);
        this.gui.onMouseClick(x, y, button);
    }


    @Override
    public void drawItem(GuiItem item) {
        ItemStack stack = FakeContainer.itemStackFromGuiItem(item);
        if (stack != null) {
            this.renderItemStack(item, stack);
        }
    }

    @Override
    public void drawItemDarkened(GuiItem item) {
        ItemStack stack = FakeContainer.itemStackFromGuiItem(item);
        if (stack == null) {
            return;
        }
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glColor3f(0f, 0f, 0f);
        itemRender.renderWithColor = false;
        if (stack.getItem().requiresMultipleRenderPasses()) {
            RenderHacks.renderItemDarkenedForMultiplePasses(
                itemRender, 
                this.mc.getTextureManager(), 
                stack, item.x, item.y);
        } else {
            this.renderItemStack(item, stack);
        }
        itemRender.renderWithColor = true;
        GL11.glPopAttrib();
    }

    private void renderItemStack(GuiItem item, ItemStack stack) {
        itemRender.renderItemAndEffectIntoGUI(
            this.fontRendererObj, 
            this.mc.getTextureManager(), stack, item.x, item.y);
        itemRender.renderItemOverlayIntoGUI(
            this.fontRendererObj, 
            this.mc.getTextureManager(), stack, item.x, item.y);
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
    public void drawString(String text, int x, int y, int color, boolean shadow) {
        this.fontRendererObj.drawString(text, x - guiLeft, y - guiTop, color, shadow);
    }

    @Override
    public void drawTooltip(List<String> lines, int x, int y) {
        this.drawHoveringText(lines, x, y, this.fontRendererObj);
    }

    @Override
    public boolean isShiftDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    @Override
    public String translate(String key) {
        return StatCollector.translateToLocal(key);
    }

    @Override
    public String translateFormatted(String key, Object... args) {
        return StatCollector.translateToLocalFormatted(key, args);
    }
}
