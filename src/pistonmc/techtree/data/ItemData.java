package pistonmc.techtree.data;

/**
 * An item (node) in the Tech Tree
 */
public class ItemData {
    /** full id (category.item) */
    public final String id;
    public final DataEntry data;
    public final boolean isHiddenDependency;
    private TechTree tree;
    private ItemData[] dependencies;
    private ItemData[] descendants;

    public ItemData(String id, TechTree tree, DataEntry data, ItemData[] dependencies, boolean isHiddenDependency) {
        this.id = id;
        this.tree = tree;
        this.data = data;
        this.dependencies = dependencies;
        this.isHiddenDependency = isHiddenDependency;
    }

    void setDescendants(ItemData[] descendants) {
        this.descendants = descendants;
    }

    public ItemData[] getDescendants() {
        return this.descendants;
    }

    public ItemData[] getDependencies() {
        return this.dependencies;
    }

    public String getCategoryId() {
        return this.id.substring(0, id.indexOf('.'));
    }

    public CategoryData getCategory() {
        return this.tree.getCategory(this.getCategoryId());
    }

    public String[][] getText() {
        return this.tree.getText(this.id);
    }

    public String toString() {
        return "Item(" + id + ")";
    }
}
