package pistonmc.techtree.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import pistonmc.techtree.adapter.IGuiHost;
import pistonmc.techtree.data.ItemSpecSingle;

public class GuiPageBuilder {
    public static final int TEXT_LINE_WIDTH = 142;
    public static final int ITEM_LINE_WIDTH = 6; // how many items max per line
    public static final int ITEM_SIZE = 16;
    public static final int TEXT_LINE_HEIGHT = GuiPage.TEXT_LINE_HEIGHT;
    public static final int ITEM_XOFF = -2;
    public static final int ITEM_YOFF = (TEXT_LINE_HEIGHT * 2 - ITEM_SIZE) / 2; // offset of items from top of line
    public static final int ITEM_SPACING = 6;
    public static final int PAGE_HEIGHT = 165;
    /** Object is either String or List<GuiItem> */
    private List<Object> lines;
    /**
     * For String line, the pixel width
     * For List<GuiItem> line, the number of items
     */
    private int lastLineWidth;

    /** Host for doing calculations */
    private IGuiHost host;

    public GuiPageBuilder(IGuiHost host) {
        this.host = host;
        this.lines = new ArrayList<>();
        this.lastLineWidth = 0;
    }

    /**
     * Parse a list of paragraphs into a list of pages
     *
     * A newline is added at the end of each paragraph.
     *
     * A paragraph equals to `---` will be treated as a page break.
     *
     * Item slots are represented by `<|>modid:name:meta stacksize<|>` and cannot be
     * on the same line as text. Multiple item slots can be on the same line though.
     */
    public List<GuiPage> buildPages(String[] paragraphs) {
        List<GuiPage> pages = new ArrayList<>();
        this.buildPagesInternal(paragraphs, 0, pages);
        return pages;
    }

    private void buildPagesInternal(String[] paragraphs, int i, List<GuiPage> output) {
        while (i < paragraphs.length) {
            String paragraph = paragraphs[i];
            if (paragraph.equals("---")) {
                // page break
                this.buildPagesFromLines(output);
            } else {
                this.populateLines(paragraph);
                this.newLine();
            }
            i++;
        }
        this.buildPagesFromLines(output);
    }

    @SuppressWarnings("unchecked")
    private void buildPagesFromLines(List<GuiPage> output) {
        // trim trailing empty lines
        while (!this.lines.isEmpty() && this.isEmptyLine(this.lines.get(this.lines.size() - 1))) {
            this.lines.remove(this.lines.size() - 1);
        }
        int height = 0;
        GuiPage current = new GuiPage(this.host);
        for (Object o: this.lines) {
            if (o instanceof String) {
                if (height + TEXT_LINE_HEIGHT > PAGE_HEIGHT) {
                    // page full
                    output.add(current);
                    current = new GuiPage(this.host);
                    height = 0;
                }
                height += TEXT_LINE_HEIGHT;
                current.lines.add((String) o);
            } else {
                if (height + TEXT_LINE_HEIGHT * 2> PAGE_HEIGHT) {
                    // page full
                    output.add(current);
                    current = new GuiPage(this.host);
                    height = 0;
                }
                current.lines.add("");
                current.lines.add("");
                List<GuiItem> items = (List<GuiItem>) o;
                int count = items.size();
                // (x, y) here are relative to the top-left corner of gui
                int x = GuiPage.PAGE_X + (TEXT_LINE_WIDTH - ITEM_SIZE * count - ITEM_SPACING * (count - 1)) / 2 + ITEM_XOFF;
                int y = GuiPage.PAGE_Y + height + ITEM_YOFF;
                for (int i = 0; i < count; i++) {
                    GuiItem item = items.get(i);
                    item.x = x;
                    item.y = y;
                    current.items.add(item);
                    x += ITEM_SIZE + ITEM_SPACING;
                }
                height += TEXT_LINE_HEIGHT * 2;
            }
        }
        if (!current.isEmpty()) {
            output.add(current);
        }
        this.lines.clear();
        this.lastLineWidth = 0;
    }

    private boolean isEmptyLine(Object o) {
        if (o instanceof String) {
            return ((String) o).isEmpty();
        } else {
            return ((List<?>) o).isEmpty();
        }
    }

    private void populateLines(String paragraph) {
        // process item embed syntax first
        List<Object> words = this.processEmbeddedItems(paragraph);
        for (Object o: words) {
            if (o instanceof GuiItem) {
                this.addItem((GuiItem) o);
            } else {
                this.addWords((String) o);
            }
        }
    }

    private List<Object> processEmbeddedItems(String paragraph) {
        List<Object> words = new ArrayList<>();
        String[] parts = paragraph.split("<\\|>");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String s = parts[i];
            int spaceIndex = parts[i].indexOf(' ');
            ItemSpecSingle spec = null;
            int sizeInt = -1;
            if (spaceIndex > 0) {
                String item = s.substring(0, spaceIndex);
                String size = s.substring(spaceIndex + 1);
                spec = ItemSpecSingle.parse(item);
                if (spec != null) {
                    try {
                        sizeInt = Integer.parseInt(size);
                    } catch (NumberFormatException e) {
                        sizeInt = -1;
                    }
                }
            } else {
                spec = ItemSpecSingle.parse(s);
                sizeInt = 1;
            }
            if (spec != null && sizeInt > 0) {
                if (builder.length() > 0) {
                    words.add(builder.toString());
                    builder.setLength(0);
                }
                words.add(new GuiItem(spec, sizeInt));
            } else {
                if (builder.length() > 0) {
                    builder.append("<|>");
                }
                builder.append(s);
            }
        }
        if (builder.length() > 0) {
            words.add(builder.toString());
        }
        return words;
    }

    private void addWords(String s) {
        StringBuilder word = new StringBuilder();
        boolean isFindingWordEnd = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (isFindingWordEnd) {
                if (canStartWord(c)) {
                    isFindingWordEnd = false;
                    if (word.length() > 0) {
                        this.addWord(word.toString());
                        word.setLength(0);
                    }
                }
            } else {
                if (canBreakWord(c)) {
                    if (canStartWord(c)) {
                        if (word.length() > 0) {
                            this.addWord(word.toString());
                            word.setLength(0);
                        }
                    } else {
                        isFindingWordEnd = true;
                    }
                }
            }
            word.append(c);
        }
        if (word.length() > 0) {
            this.addWord(word.toString());
        }
    }

    private void addWord(String word) {
        if (!this.isWordAllowedOnCurrentLine()) {
            this.newLine();
        }
        if (this.lastLineWidth == 0) {
            this.prepareEmptyNewLine(false);
        }
        int wordWidth = this.host.getStringWidth(word);
        if (wordWidth > TEXT_LINE_WIDTH) {
            while (wordWidth > TEXT_LINE_WIDTH) {
                // break word
                String splited = this.splitWordAtWidth(word, TEXT_LINE_WIDTH);
                this.lines.add(splited);
                word = word.substring(splited.length());
                wordWidth = this.host.getStringWidth(word);
            }
            this.lines.add(word);
            this.lastLineWidth = wordWidth;
            return;
        }
        if (this.lastLineWidth + wordWidth > TEXT_LINE_WIDTH) {
            // put word on a new line
            this.lines.add(word);
            this.lastLineWidth = wordWidth;
            return;
        }
        String lastLine = (String) this.lines.get(this.lines.size() - 1);
        this.lines.set(this.lines.size() - 1, lastLine + word);
        this.lastLineWidth += wordWidth;
    }

    /**
     * Add an item to the page
     */
    @SuppressWarnings("unchecked")
    private void addItem(GuiItem item) {
        if (!this.isItemAllowedOnCurrentLine()) {
            this.newLine();
        }
        if (this.lastLineWidth == 0) {
            this.prepareEmptyNewLine(true);
        }
        if (this.lastLineWidth + 1 > ITEM_LINE_WIDTH) {
            // put item on a new line
            ArrayList<GuiItem> line = new ArrayList<>();
            line.add(item);
            this.lines.add(line);
            this.lastLineWidth = 1;
            return;
        }
        ((List<GuiItem>) this.lines.get(this.lines.size() - 1)).add(item);
        this.lastLineWidth += 1;
    }

    /** Can current line add word? */
    private boolean isWordAllowedOnCurrentLine() {
        if (this.lines.isEmpty()) {
            return false;
        }
        if (this.lastLineWidth == 0) {
            return true;
        }
        Object lastLine = this.lines.get(this.lines.size() - 1);
        return !this.isItemLine(lastLine);
    }

    private boolean isItemAllowedOnCurrentLine() {
        if (this.lines.isEmpty()) {
            return false;
        }
        if (this.lastLineWidth == 0) {
            return true;
        }
        Object lastLine = this.lines.get(this.lines.size() - 1);
        return this.isItemLine(lastLine);
    }

    private boolean isItemLine(Object line) {
        return line instanceof List;
    }

    private String splitWordAtWidth(String word, int maxWidth) {
        StringBuilder splitPart = new StringBuilder(word.substring(0, 1));
        int width = this.host.getCharWidth(word.charAt(0));
        for (int i = 1; i < word.length(); i++) {
            char c = word.charAt(i);
            int cWidth = this.host.getCharWidth(c);

            if (width + cWidth > maxWidth) {
                return splitPart.toString();
            }

            splitPart.append(c);
            width += cWidth;
        }
        return word;

    }

    /**
     * Add a newline
     */
    private void newLine() {
        this.lines.add("");
        this.lastLineWidth = 0;
    }

    /**
     * Convert an empty line to either add a new item or a new word
     */
    private void prepareEmptyNewLine(boolean forItem) {
        Object lastLine = this.lines.get(this.lines.size() - 1);
        if (forItem) {
            if (!this.isItemLine(lastLine)) {
                this.lines.set(this.lines.size() - 1, new ArrayList<>());
            }
        } else {
            if (this.isItemLine(lastLine)) {
                this.lines.set(this.lines.size() - 1, "");
            }
        }
    }

    /** Check if c can be the end of a word */
    private static boolean canStartWord(char c) {
        // @formatter:off
        return c != ' ' && c != ',' && c != '.' && c != '!' && c != '?'
        // other forms of question mark
        && c != '\uFF1F' && c != '\uFE16' && c != '\uFE56'
        // other forms of exclamation mark
        && c != '\uFF01' && c != '\uFE15' && c != '\uFE57'
        // other forms of comma
        && c != '\uFF0C' && c != '\uFE10' && c != '\uFE50'
        // other forms of period
        && c != '\uFF0E' && c != '\uFE52' && c != '\u3002'
        && c != ')' && c != ']' && c != '}';
        // @formatter:on
    }

    /** Return if a word can stop when the next character is c */
    private static boolean canBreakWord(char c) {
        return !Character.isAlphabetic(c);
    }
}
