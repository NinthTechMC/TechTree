package pistonmc.techtree.event;

import pistonmc.techtree.ModMain;
import pistonmc.techtree.adapter.IDeserializer;
import pistonmc.techtree.adapter.ISerializer;

/**
 * Message sent to client to notify new unread page
 */
public class MsgSyncNewPage extends Msg {
    public String pageId;
    public MsgSyncNewPage() {}
    public MsgSyncNewPage(String pageId) {
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
    public void handleAtClient() {
        ModMain.getClient().getProgress().onNewPage(this.pageId);
    }

    public static byte id;
    @Override
    public byte getId() {
        return id;
    }
}
