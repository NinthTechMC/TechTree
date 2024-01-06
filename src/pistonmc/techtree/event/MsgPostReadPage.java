package pistonmc.techtree.event;

import pistonmc.techtree.ModMain;
import pistonmc.techtree.adapter.IDeserializer;
import pistonmc.techtree.adapter.IPlayerServerSide;
import pistonmc.techtree.adapter.ISerializer;

/**
 * Message sent to server to notify reading a new page
 */
public class MsgPostReadPage extends Msg {
    public String pageId;
    public MsgPostReadPage() {}
    public MsgPostReadPage(String pageId) {
        this.pageId = pageId;
    }

    @Override
    public void writeTo(ISerializer serializer) {
        serializer.writeString(this.pageId);
    }

    @Override
    public void readFrom(IDeserializer deserializer) {
        this.pageId = deserializer.readString();
    }

    @Override
    public Msg handleAtServer(IPlayerServerSide ctx) {
        ModMain.getServer().getProgress().onReadPage(ctx, this.pageId);
        return null;
    }

    public static byte id;
    @Override
    public byte getId() {
        return id;
    }
}
