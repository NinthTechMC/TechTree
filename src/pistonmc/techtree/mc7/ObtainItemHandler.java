package pistonmc.techtree.mc7;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import pistonmc.techtree.ModMain;
import pistonmc.techtree.adapter.IPlayerServerSide;
import pistonmc.techtree.data.ItemSpecSingle;

public class ObtainItemHandler {
    private ModServer server;

    public ObtainItemHandler(ModServer server) {
        this.server = server;
    }

    @SubscribeEvent
    public void onPickupItem(EntityItemPickupEvent event) {
        if (event.entityPlayer.worldObj.isRemote) {
            // shouldn't happen, but just in case
            return;
        }
        this.handleObtainItem(event.entityPlayer, event.item.getEntityItem());
    }

    @SubscribeEvent
    public void onCraftItem(PlayerEvent.ItemCraftedEvent event) {
        if (event.player.worldObj.isRemote) {
            // shouldn't happen, but just in case
            return;
        }
        this.handleObtainItem(event.player, event.crafting);
    }

    @SubscribeEvent
    public void onSmeltItem(PlayerEvent.ItemSmeltedEvent event) {
        if (event.player.worldObj.isRemote) {
            // shouldn't happen, but just in case
            return;
        }
        this.handleObtainItem(event.player, event.smelting);
    }

    private void handleObtainItem(EntityPlayer entityPlayer, ItemStack stack) {
        IPlayerServerSide player = new PlayerServerSide(entityPlayer);
        ItemSpecSingle item = toItemSpec(stack);
        this.server.getProgress().onPlayerObtainItem(player, item);
    }

    public static ItemSpecSingle toItemSpec(ItemStack stack) {
        Item item = stack.getItem();
        UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(item);
        if (id == null) {
            if (item instanceof ItemBlock) {
                id = GameRegistry.findUniqueIdentifierFor(((ItemBlock) item).field_150939_a);
            }
            if (id == null) {
                ModMain.log.warn("Failed to find unique identifier for item!");
                return ItemSpecSingle.EMPTY;
            }
        }
        return new ItemSpecSingle(id.modId, id.name, stack.getItemDamage());
    }
}
