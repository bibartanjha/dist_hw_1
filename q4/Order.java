public class Order {
    String orderId;
    String productName;
    int quantity;
    String userName;

    public Order(String orderId, String productName, int quantity, String userName) {
        this.orderId = orderId;
        this.productName = productName;
        this.quantity = quantity;
        this.userName = userName;
    }
}
