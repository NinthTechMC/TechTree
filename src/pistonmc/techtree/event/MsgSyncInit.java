package pistonmc.techtree.event;

import java.util.ArrayList;
import java.util.List;
import pistonmc.techtree.ModMain;
import pistonmc.techtree.adapter.IDeserializer;
import pistonmc.techtree.adapter.ISerializer;
import pistonmc.techtree.data.ItemSpec;
import pistonmc.techtree.data.ItemUnionPaged;

/**
 * Message sent to client to intialize obtained item union
 */
public class MsgSyncInit extends Msg {
    public long correlation;
    /** the total size of the union */
    public int size;
    /** the items on this page of the union, client only */
    public List<ItemSpec> items;
    /** the page number of the union, server only */
    public int page;
    public ItemUnionPaged union;
    public MsgSyncInit() {}
    public MsgSyncInit(ItemUnionPaged items, long correlation, int page) {
        this.correlation = correlation;
        this.size = items.size();
        this.union = items;
    }

    @Override
    public void writeTo(ISerializer serializer) {
        serializer.writeLong(this.correlation);
        serializer.writeInt(this.size);
        this.union.writePageTo(serializer, this.page);
    }

    @Override
    public void readFrom(IDeserializer deserializer) {
        this.correlation = deserializer.readLong();
        this.size = deserializer.readInt();
        int length = deserializer.readInt();
        this.items = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            this.items.add(ItemSpec.readFrom(deserializer));
        }
    }

    @Override
    public void handleAtClient() {
        ModMain.getClient().getProgress().onInitObtainedItems(this.items, this.correlation, this.size);
    }

    public static byte id;
    @Override
    public byte getId() {
        return id;
    }
}
