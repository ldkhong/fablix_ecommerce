public class Pair {
    private Integer qty = 0;
    private String title = "";

    public Pair(String title, Integer qty) {
        this.title = title;
        this.qty = qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Integer getQty() {
        return qty;
    }

    public String getTitle() {
        return title;
    }
}
