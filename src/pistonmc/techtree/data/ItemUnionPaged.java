package pistonmc.techtree.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import pistonmc.techtree.ModMain;
import pistonmc.techtree.adapter.ISerializer;

public class ItemUnionPaged implements ItemUnion {
    private static final int PAGE_SIZE = 1024;
    private HashMap<String, Integer> itemToIndex = new HashMap<>();
    private ItemSpec[] items = new ItemSpec[PAGE_SIZE];

    @Override
    public void clear() {
        this.itemToIndex.clear();
    }

    @Override
    public int size() {
        return this.itemToIndex.size();
    }

    @Override
    public IntervalUnion getMetaUnionForItem(String namespacedId) {
        Integer index = this.itemToIndex.get(namespacedId);
        if (index == null) {
            return null;
        }
        return this.items[index].getMeta();
    }

    @Override
    public void setMetaUnionForItem(String namespacedId, IntervalUnion meta) {
        Integer index = this.itemToIndex.get(namespacedId);
        if (index == null) {
            index = this.itemToIndex.size();
        }
        if (index >= this.items.length) {
            ItemSpec[] newItems = new ItemSpec[this.items.length + PAGE_SIZE];
            System.arraycopy(this.items, 0, newItems, 0, this.items.length);
            this.items = newItems;
        }
        int colonIndex = namespacedId.indexOf(':');
        if (colonIndex == -1) {
            ModMain.log.error("Invalid namespaced ID: " + namespacedId);
            return;
        }
        String modid = namespacedId.substring(0, colonIndex);
        String name = namespacedId.substring(colonIndex + 1);
        this.items[index] = new ItemSpec(modid, name, meta);
        this.itemToIndex.put(namespacedId, index);
    }

    public void readFrom(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ItemSpec item = ItemSpec.parse(line);
                if (item == null) {
                    ModMain.log.error("Invalid item spec: " + line);
                    continue;
                }
                this.union(item);
            }
        } catch (IOException e) {
            ModMain.error(e);
        }
    }

    public void writeTo(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            int size = this.size();
            for (int i = 0; i < size; i++) {
                ItemSpec item = this.items[i];
                writer.write(item.toString());
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            ModMain.error(e);
        }
    }

    public int pages() {
        return (this.itemToIndex.size() + PAGE_SIZE - 1) / PAGE_SIZE;
    }

    public void writePageTo(ISerializer serializer, int page) {
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, this.itemToIndex.size());
        int length = end - start;
        serializer.writeInt(length);
        for (int i = start; i < end; i++) {
            ItemSpec item = this.items[i];
            item.writeTo(serializer);
        }
    }

}
