package common.ecommerce;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Customer {
    private UUID id;
    private String name;
    private int totalPrice;
    private Map<String, Integer> cart;  // Product name: Product count

    public Customer(UUID id, String name){
        this.id = id;
        this.name = name;
        this.totalPrice = 0;
        this.cart = new HashMap<>();
    }

    public static synchronized Customer generateCustomer(String name){
        UUID id = UUID.randomUUID();
        return new Customer(id, name);
    }

    public Map<String, Integer> getCart(){
        return this.cart;
    }

    public void addItem(String productName, int productCount){
        if (this.cart.containsKey(productName)){
            int count = this.cart.get(productName);
            int updateCount = count + productCount;
            this.cart.put(productName, updateCount);
        } else{
            this.cart.put(productName, productCount);
        }
    }

    public void deleteItem(String productName, int productCount){
        if (this.cart.containsKey(productName)){
            int count = this.cart.get(productName);
            int updateCount = count - productCount;
            if (updateCount <= 0){
                this.cart.remove(productName);
            } else {
                this.cart.put(productName, updateCount);
            }
        }
    }

    public void resetCart() {
        this.cart = new HashMap<>();
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String printCart() {
        if (this.cart.size() == 0) return "Empty Cart";
        StringBuilder sb = new StringBuilder();
        for (String productName: this.cart.keySet()) {
            sb.append(productName).append(" ").append(this.cart.get(productName)).append("; ");
        }
        return sb.toString();
    }
}
