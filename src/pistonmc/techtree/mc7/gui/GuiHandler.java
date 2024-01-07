package pistonmc.techtree.mc7.gui;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pistonmc.techtree.data.ProgressClient;
import pistonmc.techtree.gui.GuiState;

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
        // TODO: load gui state based on item
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack == null) {
            return null;
        }
        boolean isDebug = stack.getItem() == this.debugItem;
        return new GuiHost(this.progress, new GuiState(isDebug));
    }
}
