package pistonmc.techtree.event;

import libpiston.adapter.IDeserializer;
import libpiston.adapter.ISerializer;
import pistonmc.techtree.ModMain;
import pistonmc.techtree.data.ItemSpecSingle;

/**
 * Message sent to client to notify new obtained item
 */
public class MsgSyncObtainItem extends Msg {
    public ItemSpecSingle item;
    public MsgSyncObtainItem() {}
    public MsgSyncObtainItem(ItemSpecSingle item) {
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
    public void handleAtClient() {
        ModMain.getClient().getProgress().onObtainItem(this.item);
    }

    public static byte id;
    @Override
    public byte getId() {
        return id;
    }
}
