package pistonmc.techtree.event;

import java.util.ArrayList;
import java.util.List;
import pistonmc.techtree.ModMain;
import pistonmc.techtree.adapter.IDeserializer;
import pistonmc.techtree.adapter.IPlayerServerSide;
import pistonmc.techtree.adapter.ISerializer;

/**
 * Message sent to server to notify new unread pages
 */
public class MsgPostNewPages extends Msg {
    public List<String> pageIds;
    public MsgPostNewPages() {}
    public MsgPostNewPages(List<String> pageId) {
        this.pageIds = pageId;
    }

    @Override
    public void writeTo(ISerializer serializer) {
        serializer.writeInt(this.pageIds.size());
        for (String pageId : this.pageIds) {
            serializer.writeString(pageId);
        }
    }

    @Override
    public void readFrom(IDeserializer deserializer) {
        int len = deserializer.readInt();
        this.pageIds = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            this.pageIds.add(deserializer.readString());
        }
    }

    @Override
    public Msg handleAtServer(IPlayerServerSide player) {
        ModMain.getServer().getProgress().onNewPages(player, this.pageIds);
        return null;
    }

    public static byte id;
    @Override
    public byte getId() {
        return id;
    }
}
