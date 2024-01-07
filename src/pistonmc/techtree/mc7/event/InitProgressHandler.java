package pistonmc.techtree.mc7.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import pistonmc.techtree.adapter.IPlayerServerSide;
import pistonmc.techtree.mc7.ModServer;
import pistonmc.techtree.mc7.PlayerServerSide;

public class InitProgressHandler {
    private ModServer server;

    public InitProgressHandler(ModServer server) {
        this.server = server;
    }

    @SubscribeEvent
    public void onPlayerJoined(PlayerLoggedInEvent event) {
        IPlayerServerSide player = new PlayerServerSide(event.player);
        server.getProgress().sendProgressTo(player);
    }

}
