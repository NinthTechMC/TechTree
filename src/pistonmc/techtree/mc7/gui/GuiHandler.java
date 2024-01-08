package pistonmc.techtree.mc7.gui;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pistonmc.techtree.data.ProgressClient;
import pistonmc.techtree.gui.GuiState;
import pistonmc.techtree.gui.GuiTechTree;
import pistonmc.techtree.mc7.ItemGuideBook;
import pistonmc.techtree.mc7.data.NBTTagCompoundWrapper;

public class GuiHandler implements IGuiHandler{
    private ProgressClient progress;
    private Item debugItem;

    public GuiHandler(ProgressClient progress, Item debugItem) {
        this.progress = progress;
        this.debugItem = debugItem;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y,
            int z) {
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y,
            int z) {
        if (this.progress == null) {
            return null;
        }
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack == null) {
            return null;
        }
        Item item = stack.getItem();
        if (!(item instanceof ItemGuideBook)) {
            return null;
        }
        boolean isDebug = stack.getItem() == this.debugItem;
        GuiState state;
        if (stack.hasTagCompound()) {
            state = GuiTechTree.readState(new NBTTagCompoundWrapper(stack.getTagCompound()), isDebug);
        } else {
            state = new GuiState(isDebug);
        }
        return new GuiHost(this.progress, state);
    }
}
