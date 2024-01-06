package pistonmc.techtree.event;

import pistonmc.techtree.ModMain;
import pistonmc.techtree.adapter.IDeserializer;
import pistonmc.techtree.adapter.IPlayerServerSide;
import pistonmc.techtree.adapter.ISerializer;

/**
 * Message sent to the server to request initialization of progress on the client side
 */
public class MsgGetProgress extends Msg {

    @Override
    public void writeTo(ISerializer serializer) { }

    @Override
    public void readFrom(IDeserializer deserializer) { }

    @Override
    public Msg handleAtServer(IPlayerServerSide ctx) {
        ModMain.getServer().getProgress().sendProgressTo(ctx);
        return null;
    }

    public static byte id;
    @Override
    public byte getId() {
        return id;
    }
}
