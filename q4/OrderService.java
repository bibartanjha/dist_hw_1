import java.util.ArrayList;

public class OrderService {
    ArrayList<Order> orders = new ArrayList<>();

    public void addOrder(Order order) { orders.add(order); }

    public int getOrderIndex(String orderId) {
        for (int i = 0; i < orders.size(); i ++) {
            if (orders.get(i).orderId.equals(orderId)) {
                return i;
            }
        }
        return -1;
    }

    public void removeOrder(int orderIndex) {
        orders.remove(orderIndex);
    }

    public ArrayList<Order> getOrders() { return orders; }
}
