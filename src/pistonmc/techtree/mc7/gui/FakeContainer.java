package pistonmc.techtree.mc7.gui;

import java.util.List;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import pistonmc.techtree.ModMain;
import pistonmc.techtree.data.ItemSpecSingle;
import pistonmc.techtree.gui.GuiItem;

/**
 * Fake container used to display items
 */
public class FakeContainer extends Container implements IInventory {

    static class FakeSlot extends Slot {
        public FakeSlot(IInventory inv, int i, int x, int y) {
            super(inv, i, x, y);
        }

        public boolean canTakeStack(EntityPlayer player) {
            return false;
        }
    }

    private ItemStack[] stacks;

    public FakeContainer() {
        this.stacks = new ItemStack[0];
    }

    public void setSlots(List<GuiItem> items) {
        ItemStack[] newStacks = new ItemStack[items.size()];
        this.inventorySlots.clear();
        this.inventoryItemStacks.clear();
        for (int i = 0; i < items.size(); i++) {
            GuiItem guiItem = items.get(i);
            ItemStack stack = itemStackFromGuiItem(guiItem);
            if (stack == null) {
                ModMain.log.warn("Cannot find item to display: " + guiItem.item.getNamespacedId());
                newStacks[i] = null;
            } else {
                newStacks[i] = stack;
            }
            this.addSlotToContainer(new FakeSlot(this, i, guiItem.x, guiItem.y));
        }
        this.stacks = newStacks;
    }

    public static ItemStack itemStackFromGuiItem(GuiItem guiItem) {
        ItemSpecSingle spec = guiItem.item;
        Item item = GameRegistry.findItem(spec.modid, spec.name);
        if (item == null) {
            // try block?
            Block block = GameRegistry.findBlock(spec.modid, spec.name);
            // block should never be null because it falls back to air
            if (block != null) {
                item = Item.getItemFromBlock(block);
            }
        }
        if (item == null) {
            return null;
        }
        return new ItemStack(item, guiItem.stackSize, spec.meta);
    }

    @Override
    public int getSizeInventory() {
        return this.stacks.length;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return this.stacks[i];
    }

    @Override
    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_) { return null; }

    @Override
    public ItemStack getStackInSlotOnClosing(int p_70304_1_) { return null; }

    @Override
    public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) { }

    @Override
    public String getInventoryName() { return ""; }

    @Override
    public boolean hasCustomInventoryName() { return false; }

    @Override
    public int getInventoryStackLimit() { return 0; }

    @Override
    public void markDirty() { }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_) { return true; }

    @Override
    public void openInventory() { }

    @Override
    public void closeInventory() { }

    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) { return false; }

    @Override
    public boolean canInteractWith(EntityPlayer p_75145_1_) { return true; }
}
