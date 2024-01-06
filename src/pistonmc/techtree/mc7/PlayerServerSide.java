package pistonmc.techtree.mc7;

import net.minecraft.entity.player.EntityPlayer;
import pistonmc.techtree.adapter.IPlayerServerSide;

public class PlayerServerSide implements IPlayerServerSide {
    public EntityPlayer entity;

    public PlayerServerSide(EntityPlayer entity) {
        this.entity = entity;
    }

    @Override
    public String getClientPlayerName() {
        return entity.getCommandSenderName();
    }

    @Override
    public void markDirty() {
    }
}
