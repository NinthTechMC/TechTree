package pistonmc.techtree.gui;

public class GuiState {
    /** If debug mode is enabled */
    public boolean isDebug;
    /** Id of the page currently displaying */
    private String currentPage;
    /** Index of the first item to display in the categories list */
    public int indexListStart;
    /** Index of the first item to display in the items or categories list */
    public int currentListStart;
    /** Page of the content currently displaying */
    public int currentContentPage;


    public void handleListPrevPage() {
        if (this.isOnCategoryList()) {
            this.currentListStart = Math.max(0, this.currentListStart - GuiTechTree.CATEGORES_PER_PAGE);
            return;
        }
        this.currentListStart = Math.max(0, this.currentListStart - GuiTechTree.ITEMS_PER_PAGE);
    }

    public void handleListNextPage(int listSize, int increment) {
        if (this.currentListStart + increment >= listSize) {
            return;
        }
        this.currentListStart += increment;
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
}
