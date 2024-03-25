package pistonmc.techtree.data;

import java.io.File;
import java.util.HashMap;
import libpiston.util.IntervalUnion;
import pistonmc.techtree.ModMain;

/**
 * A set of (modid:item:meta)
 */
public class ItemUnionUnpaged implements ItemUnion {
    /** (modid:item) to (meta) */
    private HashMap<String, IntervalUnion> items = new HashMap<>();

    public ItemUnionUnpaged() {
    }

    @Override
    public void clear() {
        items.clear();
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public IntervalUnion getMetaUnionForItem(String namespacedId) {
        return this.items.get(namespacedId);
    }

    @Override
    public void setMetaUnionForItem(String namespacedId, IntervalUnion meta) {
        this.items.put(namespacedId, meta);
    }

    @Override
    public void writeTo(File file) {
        ModMain.log.error("Unpaged item union is used on the client and should not be saved!");
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void readFrom(File file) {
        ModMain.log.error("Unpaged item union is used on the client and should not be saved!");
        throw new UnsupportedOperationException();
        
    }
}
