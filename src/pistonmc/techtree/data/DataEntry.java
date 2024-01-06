package pistonmc.techtree.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import pistonmc.techtree.ModMain;

public class DataEntry {
    private static final int LINES_PER_PAGE = 14;

    static enum ReadState {
        TITLE,
        ICON,
        ITEMS,
        AFTER,
        TUTORIAL,
    }

    static class WithText {
        DataEntry data;
        String[][] text;
    }

    /**
     * Read an entry from a file, excluding the text section
     *
     * Returns null on error
     */
    public static DataEntry readFrom(File file) {
        WithText dataWithText = readFromInternal(file, false);
        if (dataWithText == null) {
            return null;
        }
        return dataWithText.data;
    }

    /**
     * Read an entry from a file, including the text section
     *
     * Returns null on error
     */
    public static WithText readFromWithText(File file) {
        return readFromInternal(file, true);
    }

    private static WithText readFromInternal(File file, boolean includeText) {
        ReadState state = ReadState.TITLE;
        ArrayList<String> title = new ArrayList<>();
        ItemSpec icon = null;
        ArrayList<ItemSpec> items = new ArrayList<>();
        ArrayList<String> after = new ArrayList<>();
        boolean isTutorial = false;
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            outer: while ((line = reader.readLine()) != null) {
                // Skip comments
                int hashIndex = line.indexOf('#');
                if (hashIndex != -1) {
                    line = line.substring(0, hashIndex).trim();
                } else {
                    line = line.trim();
                }
                if (line.isEmpty()) {
                    continue;
                }
                switch (line) {
                    case "[title]":
                        state = ReadState.TITLE;
                        continue;
                    case "[icon]":
                        state = ReadState.ICON;
                        continue;
                    case "[items]":
                        state = ReadState.ITEMS;
                        continue;
                    case "[after]":
                        state = ReadState.AFTER;
                        continue;
                    case "[tutorial]":
                        isTutorial = true;
                        state = ReadState.TUTORIAL;
                        continue;
                    case "[text]":
                        break outer;
                }
                switch (state) {
                    case TITLE:
                        title.add(line);
                        break;
                    case ICON:
                        icon = ItemSpec.parse(line);
                        break;
                    case ITEMS:
                        ItemSpec item = ItemSpec.parse(line);
                        if (item != null) {
                            items.add(item);
                        }
                        break;
                    case AFTER:
                        after.add(line);
                        break;
                    case TUTORIAL:
                        break;
                }
            }
            String[][] text = null;
            if (includeText) {
                text = readTextSection(reader);
            }
            String[] titleArr = title.toArray(new String[title.size()]);
            ItemSpec[] itemsArr = items.toArray(new ItemSpec[items.size()]);
            String[] afterArr = after.toArray(new String[after.size()]);
            WithText withText = new WithText();
            withText.data = new DataEntry(titleArr, icon, itemsArr, afterArr, isTutorial);
            withText.text = text;
            return withText;
        } catch(IOException e) {
            ModMain.error(e);
        }
        return null;
    }

    /** Read only the text section of a file */
    public static String[][] readTextFrom(File file) {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("[text]")) {
                    break;
                }
            }
            return readTextSection(reader);
        } catch(IOException e) {
            ModMain.error(e);
        }
        return new String[0][];
    }

    /** 
     * Read text section from a reader 
     *
     * The reader should be positioned at the beginning of the text section, excluding the header
     *
     * Upon exception, what's read so far will be returned
     */
    private static String[][] readTextSection(BufferedReader reader) {
        ArrayList<String[]> pages = new ArrayList<>();
        ArrayList<String> currentPage = new ArrayList<>(); 
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (currentPage.size() >= LINES_PER_PAGE || line.trim().equals("---")) {
                    String[] page = currentPage.toArray(new String[currentPage.size()]);
                    pages.add(page);
                    currentPage.clear();
                    continue;
                }
                currentPage.add(line);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
		if (!currentPage.isEmpty()) {
			String[] page = currentPage.toArray(new String[currentPage.size()]);
			pages.add(page);
		}
        return pages.toArray(new String[pages.size()][]);
    }


    /** Data in the title section (non-null) */
    public final String[] title;
    /** Data in the icon section (non-null) */
    public final ItemSpec icon;
    /** Data in the items section (non-null) */
    public final ItemSpec[] items;
    /** Data in the after section (non-null) */
    public final String[] after;
    /** Whether this entry is a tutorial */
    public final boolean isTutorial;

    private DataEntry(String[] title, ItemSpec icon, ItemSpec[] items, String[] after, boolean isTutorial) {
        this.title = title;
        this.icon = icon == null ? ItemSpec.EMPTY : icon;
        this.items = items;
        this.after = after;
        this.isTutorial = isTutorial;
    }



}
