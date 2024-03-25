package pistonmc.techtree.data;

import java.io.File;
import libpiston.util.IntervalUnion;

public interface ItemUnion {
    public void clear();
    public int size();
    public IntervalUnion getMetaUnionForItem(String namespacedId);
    public void setMetaUnionForItem(String namespacedId, IntervalUnion meta);

    public void writeTo(File file);
    public void readFrom(File file);

    public default boolean contains(ItemSpec item) {
        IntervalUnion meta = this.getMetaUnionForItem(item.getNamespacedId());
        if (meta == null) {
            return false;
        }
        return meta.intersects(item.getMeta());
    }

    public default boolean contains(ItemSpecSingle item) {
        IntervalUnion meta = this.getMetaUnionForItem(item.getNamespacedId());
        if (meta == null) {
            return false;
        }
        return meta.contains(item.meta);
    }
    
    public default void union(ItemSpecSingle item) {
        this.union(item.getNamespacedId(), item.meta);
    }

    public default void union(String namespacedId, int meta) {
        IntervalUnion metaUnion = this.getMetaUnionForItem(namespacedId);
        if (metaUnion == null) {
            metaUnion = IntervalUnion.range(meta, meta);
            this.setMetaUnionForItem(namespacedId, metaUnion);
            return;
        }
        metaUnion.union(meta, meta);
    }

    public default void union(ItemSpec item) {
        IntervalUnion metaUnion = this.getMetaUnionForItem(item.getNamespacedId());
        if (metaUnion == null) {
            metaUnion = item.getMeta().copy();
            this.setMetaUnionForItem(item.getNamespacedId(), metaUnion);
            return;
        }
        metaUnion.union(item.getMeta());
    }
}
