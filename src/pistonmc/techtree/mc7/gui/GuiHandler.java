package pistonmc.techtree.mc7.gui;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pistonmc.techtree.data.ProgressClient;
import pistonmc.techtree.gui.GuiState;

public class GuiHandler implements IGuiHandler{
    private ProgressClient progress;

    public GuiHandler(ProgressClient progress) {
        this.progress = progress;
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
        return new GuiHost(this.progress, new GuiState());
    }
}
