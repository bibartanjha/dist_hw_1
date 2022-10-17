import java.util.ArrayList;

public class ProductService {
    private ArrayList<Product> table = new ArrayList<>();
    public int getProductIndex(String productName) {
        for (int i = 0; i < table.size(); i ++) {
            if (table.get(i).productName.equals(productName)) {
                return i;
            }
        } return -1;
    }

    public void addQuantity(int productIndex, int quantityChange) {
        int newQuantity = table.get(productIndex).getQuantity() + quantityChange;
        table.get(productIndex).setQuantity(newQuantity);
    }
    public ArrayList<Product> getTable() { return table; }

    public void addToTable(String productName, int quantity) {
        table.add(new Product(productName, quantity));
    }
}
