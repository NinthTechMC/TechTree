package pistonmc.techtree.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import pistonmc.techtree.data.CategoryData;
import pistonmc.techtree.data.DataEntry;
import pistonmc.techtree.data.ProgressClient;
import pistonmc.techtree.data.TechTree;

public class GuideBook {
    public final boolean isDebug;

    @SideOnly(Side.CLIENT)
    public ProgressClient progressClient;
    public TechTree tree;

    public GuideBook(boolean isDebug, TechTree tree) {
        this.isDebug = isDebug;
        this.tree = tree;
    }

    @SideOnly(Side.CLIENT)
    public GuideBook setProgressClient(ProgressClient progress) {
        this.progressClient = progress;
        return this;
    }

    @SideOnly(Side.CLIENT)
    public boolean hasNewPages() {
        return this.progressClient != null && this.progressClient.hasNewPages();
    }

    public String getBookTitle() {
        CategoryData index = this.tree.getIndex();
        if (index == null || index.data.title.length == 0) {
            return "Guide Book";
        }
        return index.data.title[0];
    }

    @SideOnly(Side.CLIENT)
    public String getPageName(String id) {
        DataEntry entry = this.tree.getData(id);
        if (entry == null) {
            return null;
        }
        return entry.title.length == 0 ? null : entry.title[0];
    }

    public List<String> getBookSubtitles() {
        CategoryData index = this.tree.getIndex();
        if (index == null || index.data.title.length == 0) {
            return Arrays.asList("Failed to read config!", "See the README on GitHub for how to set this up");
        }
        List<String> subtitles = new ArrayList<>(index.data.title.length - 1);
        for (int i = 1; i < index.data.title.length; i++) {
            subtitles.add(index.data.title[i]);
        }
        return subtitles;
    }

    @SideOnly(Side.CLIENT)
    public boolean reloadClient() {
        boolean result = this.tree.reload();
        this.progressClient.refreshAllItems();
        return result;
    }

    public boolean hasUnfinishedTutorial() {
        return this.progressClient.hasUnfinishedTutorial();
    }

    public boolean isCompleted() {
        return this.progressClient.isCompleted();
    }
}
