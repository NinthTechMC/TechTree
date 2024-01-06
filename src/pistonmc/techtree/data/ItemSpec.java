package pistonmc.techtree.data;

import pistonmc.techtree.ModMain;
import pistonmc.techtree.adapter.IDeserializer;
import pistonmc.techtree.adapter.ISerializer;

/**
 * Specification of an item
 *
 * This data means "the modid:name item, with any meta in the union"
 */
public class ItemSpec {
    public static ItemSpec EMPTY = new ItemSpec("minecraft", "air", IntervalUnion.parse("*"));

	public static ItemSpec parse(String input) {
		String[] parts = input.split(":");
		if (parts.length < 2) {
			ModMain.log.error("Invalid item spec: " + input);
			return null;
		}
		String modid = parts[0].trim();
		String name = parts[1].trim();
        IntervalUnion meta;
		if (parts.length > 2) {
            meta = IntervalUnion.parse(parts[2]);
            if (meta == null) {
                ModMain.log.error("Invalid item spec: " + input);
                return null;
            }
		} else {
            meta = IntervalUnion.parse("*");
        }
        return new ItemSpec(modid, name, meta);
	}

	private String modid;
	private String name;
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
}
