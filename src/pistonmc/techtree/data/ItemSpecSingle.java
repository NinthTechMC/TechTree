package pistonmc.techtree.data;

import pistonmc.techtree.adapter.IDeserializer;
import pistonmc.techtree.adapter.ISerializer;

/**
 * Specification of an item with a single meta
 */
public class ItemSpecSingle {
    public static ItemSpecSingle EMPTY = new ItemSpecSingle("minecraft", "air", 0);

	private String modid;
	private String name;
	public final int meta;

	public ItemSpecSingle(String modid, String name, int meta) {
		this.modid = modid;
		this.name = name;
		this.meta = meta;
	}

    public ItemSpecSingle copy() {
        return new ItemSpecSingle(modid, name, meta);
    }

    public String getNamespacedId() {
        return modid + ":" + name;
    }

    public String toString() {
        return "ItemSpecSingle(" + getNamespacedId() + ":" + meta + ")";
    }

    public void writeTo(ISerializer serializer) {
        serializer.writeString(modid);
        serializer.writeString(name);
        serializer.writeInt(meta);
    }

    public static ItemSpecSingle readFrom(IDeserializer deserializer) {
        String modid = deserializer.readString();
        String name = deserializer.readString();
        int meta = deserializer.readInt();
        return new ItemSpecSingle(modid, name, meta);
    }
}
