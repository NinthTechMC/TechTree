package pistonmc.techtree.mc7.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraft.util.ChatComponentTranslation;
import pistonmc.techtree.adapter.IPlayerServerSide;
import pistonmc.techtree.mc7.ModServer;
import pistonmc.techtree.mc7.PlayerServerSide;

public class InitProgressHandler {
    private ModServer server;

    public InitProgressHandler(ModServer server) {
        this.server = server;
    }

    // this event is only fired on the server side
    @SubscribeEvent
    public void onPlayerJoined(PlayerLoggedInEvent event) {
        if (server.getTechTree().hasError) {
            event.player.addChatMessage(new ChatComponentTranslation("techtree.load_error"));
        }
        IPlayerServerSide player = new PlayerServerSide(event.player);
        server.getProgress().sendProgressTo(player);
    }

}
