package pistonmc.techtree.mc7.event;

import java.util.HashSet;
import java.util.Set;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import pistonmc.techtree.ModMain;
import pistonmc.techtree.mc7.ItemGuideBook;

/** Keeps the guide book in the inventory on death if the player hasn't completed the tutorial. */
public class TutorialHandler {
    private ItemGuideBook guideBook;
    private Set<String> playerWhoDroppedTutorialBooks;

    public TutorialHandler(ItemGuideBook guideBook) {
        this.guideBook = guideBook;
        this.playerWhoDroppedTutorialBooks = new HashSet<String>();
    }
    // this is only fired on the server side
    @SubscribeEvent
    public void onPlayerDeath(PlayerDropsEvent event) {
        for (int i = 0; i < event.drops.size(); i++) {
            EntityItem entityItem = event.drops.get(i);
            ItemStack stack = entityItem.getEntityItem();
            if (!(stack.getItem() instanceof ItemGuideBook)) {
                continue;
            }
            if (!this.guideBook.hasBookTag(stack, ItemGuideBook.TUTORIAL_TAG)) {
                continue;
            }
            event.drops.remove(i);
            i--;
            String name = event.entityPlayer.getCommandSenderName();
            if (this.playerWhoDroppedTutorialBooks.add(name)) {
                ModMain.log.info("Added player " + name + " to dropped tutorial book pool");
            }
        }
    }

    // this is only fired on the server side
    @SubscribeEvent
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        String name = event.player.getCommandSenderName();
        if (this.playerWhoDroppedTutorialBooks.remove(name)) {
            ModMain.log.info("Giving guide book to player " + name + " on respawn");
            ItemStack stack = new ItemStack(this.guideBook);
            event.player.inventory.addItemStackToInventory(stack);
        }
    }
}
