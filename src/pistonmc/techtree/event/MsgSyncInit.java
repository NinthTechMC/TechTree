package pistonmc.techtree.event;

import java.util.ArrayList;
import java.util.Collections;
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
    public List<String> initEntryPages;
    public MsgSyncInit() {}
    public MsgSyncInit(ItemUnionPaged items, long correlation, int page, List<String> initEntryPages) {
        this.correlation = correlation;
        this.size = items.size();
        this.union = items;
        this.initEntryPages = initEntryPages;
    }

    @Override
    public void writeTo(ISerializer serializer) {
        serializer.writeLong(this.correlation);
        if (this.initEntryPages != null) {
            serializer.writeInt(this.initEntryPages.size());
            for (String pageId : this.initEntryPages) {
                serializer.writeString(pageId);
            }
        } else {
            serializer.writeInt(0);
        }
        serializer.writeInt(this.size);
        if (this.size > 0) {
            this.union.writePageTo(serializer, this.page);
        }
    }

    @Override
    public void readFrom(IDeserializer deserializer) {
        this.correlation = deserializer.readLong();
        int initEntryPagesLength = deserializer.readInt();
        this.initEntryPages = new ArrayList<>(initEntryPagesLength);
        for (int i = 0; i < initEntryPagesLength; i++) {
            this.initEntryPages.add(deserializer.readString());
        }
        this.size = deserializer.readInt();
        if (size > 0) {
            int length = deserializer.readInt();
            this.items = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                this.items.add(ItemSpec.readFrom(deserializer));
            }
        } else {
            this.items = Collections.emptyList();
        }
    }

    @Override
    public void handleAtClient() {
        ModMain.getClient().getProgress().onInit(this.items, this.correlation, this.size, this.initEntryPages);
    }

    public static byte id;
    @Override
    public byte getId() {
        return id;
    }
}
