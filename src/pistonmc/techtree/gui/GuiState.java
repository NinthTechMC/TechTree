package pistonmc.techtree.gui;

import pistonmc.techtree.adapter.IMapReader;
import pistonmc.techtree.adapter.IMapWriter;

public class GuiState {
    /** If debug mode is enabled */
    public final boolean isDebug;
    /** Id of the page currently displaying */
    private String currentPage;
    /** Index of the first item to display in the categories list */
    public int indexListStart;
    /** Index of the first item to display in the items or categories list */
    public int currentListStart;
    /** Page of the content currently displaying */
    public int currentContentPage;

    public GuiState(boolean isDebug) {
        this.isDebug = isDebug;
    }

    public void handleListPrevPage() {
        if (this.isOnCategoryList()) {
            this.currentListStart = Math.max(0, this.currentListStart - GuiConstants.CATEGORIES_PER_PAGE);
            return;
        }
        this.currentListStart = Math.max(0, this.currentListStart - GuiConstants.ITEMS_PER_PAGE);
    }

    public void handleListNextPage(int listSize, int increment) {
        if (this.currentListStart + increment >= listSize) {
            return;
        }
        this.currentListStart += increment;
    }

    public void ensureListStartRange(int listSize) {
        this.currentListStart = Math.max(0, Math.min(this.currentListStart, listSize - 1));
        this.indexListStart = Math.max(0, Math.min(this.indexListStart, listSize - 1));
    }

    public void ensureContentPageRange(int listSize) {
        this.currentContentPage = Math.max(0, Math.min(this.currentContentPage, listSize - 1));
    }

    public void handleContentPrevPage() {
        this.currentContentPage = Math.max(0, this.currentContentPage - 1);
    }

    public void handleContentNextPage(int listSize) {
        this.currentContentPage = Math.min(this.currentContentPage + 1, listSize - 1);
    }

    public boolean isOnCategoryList() {
        return this.currentPage == null || this.currentPage.equals("index");
    }

    public String getCurrentPage() {
        return this.currentPage == null ? "index" : this.currentPage;
    }

    public void handleReturnToIndex() {
        this.currentPage = "index";
        this.currentListStart = this.indexListStart;
    }

    public void handleEnterCategory(String id) {
        this.currentPage = id;
        this.indexListStart = this.currentListStart;
        this.currentListStart = 0;
        this.currentContentPage = 0;
    }

    public void handleEnterItem(String id) {
        this.currentPage = id;
        this.currentContentPage = 0;
    }

    public void writeToMap(IMapWriter writer) {
        writer.writeString("currentPage", this.getCurrentPage());
        writer.writeInt("indexListStart", this.indexListStart);
        writer.writeInt("currentListStart", this.currentListStart);
        writer.writeInt("currentContentPage", this.currentContentPage);
    }

    public void readFromMap(IMapReader reader) {
        this.currentPage = reader.readString("currentPage");
        this.indexListStart = reader.readInt("indexListStart");
        this.currentListStart = reader.readInt("currentListStart");
        this.currentContentPage = reader.readInt("currentContentPage");
    }
}
