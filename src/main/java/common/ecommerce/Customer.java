package common.ecommerce;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Customer implements Serializable {
    private int id;
    private String name;
    private double totalPrice;
    private Map<String, Integer> cart;  // Product name: Product count

    public static Map<Integer, Customer> customers = new HashMap<>(){{
        put(0, new Customer(0, "Andy"));
        put(1, new Customer(1, "Beta"));
        put(2, new Customer(2, "Cindy"));
        put(3, new Customer(3, "Ella"));
        put(4, new Customer(4, "Danial"));
    }};

    public static String getNameByID(int id) {
        if (!customers.containsKey(id)) {
            return "Not exist uuid";
        }
        return customers.get(id).getName();
    }

    public static int getIDByName(String name) {
        for (Integer id: customers.keySet()) {
            if (customers.get(id).getName().equals(name)) {
                return id;
            }
        }
        return -1;
    }

    public Customer(int id, String name){
        this.id = id;
        this.name = name;
        this.totalPrice = 0;
        this.cart = new HashMap<>();
    }

    public Customer(int id, String name, double totalPrice, Map<String, Integer> cart) {
        this.id = id;
        this.name = name;
        this.totalPrice = totalPrice;
        this.cart = cart;
    }

    public static Map<Integer, Customer> copyInitCustomersMap() {
        Map<Integer, Customer> copy = new HashMap<>();
        for (Map.Entry<Integer, Customer> entry: customers.entrySet()){
            copy.put(entry.getKey(), entry.getValue().copyCustomer());
        }
        return copy;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return name;
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

    public void setTotalPrice(double totalPrice) {
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

    public Customer copyCustomer() {
        Map<String, Integer> copyCart = new HashMap<>();

        for (Map.Entry<String, Integer> entry: this.cart.entrySet()) {
            copyCart.put(entry.getKey(), entry.getValue());
        }
        Customer copy = new Customer(this.id, this.name, this.totalPrice, copyCart);
        return copy;
    }
}
