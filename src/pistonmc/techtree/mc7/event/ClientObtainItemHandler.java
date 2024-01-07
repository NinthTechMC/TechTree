package pistonmc.techtree.mc7.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.item.ItemStack;
import pistonmc.techtree.data.ItemSpecSingle;
import pistonmc.techtree.data.ProgressClient;

/**
 * In case the server does not pick up obtaining an item,
 * this will scan the current item stack and send a message to the server.
 */
public class ClientObtainItemHandler {

    private ProgressClient progress;
    public ClientObtainItemHandler(ProgressClient progress) {
        this.progress = progress;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (event.side != Side.CLIENT) {
            // shouldn't happen, just in case
            return;
        }

        // every 5 tick is probably reasonable
        if (event.player.ticksExisted % 5 != 0) {
            return;
        }

        ItemStack stack = event.player.inventory.getItemStack();
        if (stack == null) {
            return;
        }

        ItemSpecSingle item = ObtainItemHandler.toItemSpec(stack);
        this.progress.onClientObtainItem(item);
    }
}
