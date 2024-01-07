package pistonmc.techtree.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pistonmc.techtree.ModMain;
import pistonmc.techtree.adapter.IPlayerData;

/**
 * Tech tree progress of a player
 */
public class PlayerProgress<TUnion extends ItemUnion> {
    /** Pages that are completable/completed but never opened */
    private HashSet<String> newPages = new HashSet<>();
    /** Items that are ever obtained by this player */
    private TUnion obtained;

    public PlayerProgress(TUnion obtained) {
        this.obtained = obtained;
    }

    public boolean isAlreadyObtained(ItemSpec item) {
        return this.obtained.contains(item);
    }

    public TUnion getObtained() {
        return this.obtained;
    }

    public void addNewPages(List<String> pageId) {
        this.newPages.addAll(pageId);
    }

    public boolean removeNewPage(String pageId) {
        return this.newPages.remove(pageId);
    }

    public void clearNewPages() {
        this.newPages.clear();
    }

    public boolean hasNewPage() {
        return !this.newPages.isEmpty();
    }

    public Set<String> getNewPages() {
        return this.newPages;
    }

    public void loadFrom(IPlayerData data) {
        this.newPages.clear();
        File newPagesFile = data.getDataFile("newpages");
        if (!newPagesFile.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(newPagesFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                this.newPages.add(line.trim());
            }
        } catch (IOException e) {
            ModMain.error(e);
            ModMain.log.error("Failed to load new pages");
        }
        this.obtained.clear();
        File obtainedItemsFile = data.getDataFile("obtained");
        if (!obtainedItemsFile.exists()) {
            return;
        }
        this.obtained.readFrom(obtainedItemsFile);
    }

    public void saveTo(IPlayerData data) {
        File newPagesFile = data.getDataFile("newpages");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(newPagesFile))) {
            for (String page: this.newPages) {
                writer.write(page);
                writer.newLine();
            }
            writer.flush();
            
        } catch (IOException e) {
            ModMain.error(e);
            ModMain.log.error("Failed to save new pages");
        }
        File obtainedItemsFile = data.getDataFile("obtained");
        this.obtained.writeTo(obtainedItemsFile);
    }
}
