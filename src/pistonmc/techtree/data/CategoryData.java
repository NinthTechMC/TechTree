package pistonmc.techtree.data;

import java.util.List;

public class CategoryData {
    public final String id;
    public final DataEntry data;
    public final ItemData[] items;

    public CategoryData(String id, DataEntry data, ItemData[] items) {
        this.id = id;
        this.data = data;
        this.items = items;
    }

    public String toString() {
        return "Category(" + id + ")";
    }

}
