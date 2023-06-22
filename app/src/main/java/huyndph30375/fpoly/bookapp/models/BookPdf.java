package huyndph30375.fpoly.bookapp.models;

public class BookPdf {
    private String uid;
    private String id;
    private String title;
    private String des;
    private String categoryId;
    private String url;
    private String price;
    private String count;
    private long timestamp;
    private long viewerCount;
    private long downloadCount;
    private long inventoryCount;
    private long buyCount;
    private long fabCount;
    private boolean checkFab;

    public BookPdf(String uid, String id, String title, String des, String categoryId, String url, String price, String count, long timestamp, long viewerCount, long downloadCount, long inventoryCount, long buyCount, long fabCount, boolean checkFab) {
        this.uid = uid;
        this.id = id;
        this.title = title;
        this.des = des;
        this.categoryId = categoryId;
        this.url = url;
        this.price = price;
        this.count = count;
        this.timestamp = timestamp;
        this.viewerCount = viewerCount;
        this.downloadCount = downloadCount;
        this.inventoryCount = inventoryCount;
        this.buyCount = buyCount;
        this.fabCount = fabCount;
        this.checkFab = checkFab;
    }

    public long getFabCount() {
        return fabCount;
    }

    public void setFabCount(long fabCount) {
        this.fabCount = fabCount;
    }

    public BookPdf() {
    }

    public long getViewerCount() {
        return viewerCount;
    }

    public void setViewerCount(long viewerCount) {
        this.viewerCount = viewerCount;
    }

    public long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public long getInventoryCount() {
        return inventoryCount;
    }

    public void setInventoryCount(long inventoryCount) {
        this.inventoryCount = inventoryCount;
    }

    public long getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(long buyCount) {
        this.buyCount = buyCount;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isCheckFab() {
        return checkFab;
    }

    public void setCheckFab(boolean checkFab) {
        this.checkFab = checkFab;
    }
}
