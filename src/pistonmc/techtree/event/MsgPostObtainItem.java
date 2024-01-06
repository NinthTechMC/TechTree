package pistonmc.techtree.event;

import pistonmc.techtree.ModMain;
import pistonmc.techtree.adapter.IDeserializer;
import pistonmc.techtree.adapter.IPlayerServerSide;
import pistonmc.techtree.adapter.ISerializer;
import pistonmc.techtree.data.ItemSpecSingle;

/**
 * Message sent to server to notify item obtained
 */
public class MsgPostObtainItem extends Msg {
    public ItemSpecSingle item;
    public MsgPostObtainItem() {}
    public MsgPostObtainItem(ItemSpecSingle item) {
        this.item = item;
    }

    @Override
    public void writeTo(ISerializer serializer) {
        this.item.writeTo(serializer);
    }

    @Override
    public void readFrom(IDeserializer deserializer) {
        this.item = ItemSpecSingle.readFrom(deserializer);
    }

    @Override
    public Msg handleAtServer(IPlayerServerSide sender) {
        return ModMain.getServer().getProgress().processObtainItem(sender, this.item);
    }

    public static byte id;
    @Override
    public byte getId() {
        return id;
    }
}
