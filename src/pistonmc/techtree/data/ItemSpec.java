package pistonmc.techtree.data;

import libpiston.ParseException;
import libpiston.adapter.IDeserializer;
import libpiston.adapter.ISerializer;
import libpiston.item.ParsedItem;
import libpiston.util.IntervalUnion;

/**
 * Specification of an item
 *
 * This data means "the modid:name item, with any meta in the union"
 */
public class ItemSpec {
    public static ItemSpec EMPTY = new ItemSpec("minecraft", "air", IntervalUnion.any());

    /**
     * Parse an item spec from a string like modid:item:meta
     * @throws ParseException
     */
	public static ItemSpec parse(String input) throws ParseException {
        ParsedItem parsed = ParsedItem.parse(input);
        return new ItemSpec(parsed.modid, parsed.name, parsed.meta);
	}

	public final String modid;
	public final String name;
	private IntervalUnion meta;

	public ItemSpec(String modid, String name, IntervalUnion meta) {
		this.modid = modid;
		this.name = name;
		this.meta = meta;
	}

    public ItemSpec copy() {
        return new ItemSpec(modid, name, meta.copy());
    }

    public String getNamespacedId() {
        return modid + ":" + name;
    }

    public IntervalUnion getMeta() {
        return meta;
    }

    public void mergeMetaWith(ItemSpec other) {
        meta.union(other.meta);
    }

    public boolean contains(ItemSpecSingle other) {
        if (!this.getNamespacedId().equals(other.getNamespacedId())) {
            return false;
        }
        return meta.contains(other.meta);
    }

    public String toString() {
        return this.getNamespacedId() + ":" + meta.toString();
    }

    public void writeTo(ISerializer serializer) {
        serializer.writeString(modid);
        serializer.writeString(name);
        meta.writeTo(serializer);
    }

    public static ItemSpec readFrom(IDeserializer deserializer) {
        String modid = deserializer.readString();
        String name = deserializer.readString();
        IntervalUnion meta = IntervalUnion.readFrom(deserializer);
        return new ItemSpec(modid, name, meta);
    }

    public ItemSpecSingle toSingle() {
        return new ItemSpecSingle(modid, name, meta.anyValue());
    }
}
