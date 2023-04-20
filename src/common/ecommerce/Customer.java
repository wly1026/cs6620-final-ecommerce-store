package common.ecommerce;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Customer {
    private UUID id;
    private String name;
    private double totalPrice;
    private Map<String, Integer> cart;  // Product name: Product count

    public static UUID[] uuids = new UUID[]{UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};
    public static Map<UUID, Customer> customers = new HashMap<>(){{
        put(uuids[0], new Customer(uuids[0], "Andy"));
        put(uuids[1], new Customer(uuids[1], "Beta"));
        put(uuids[2], new Customer(uuids[2], "Cindy"));
        put(uuids[3], new Customer(uuids[3], "Ella"));
        put(uuids[4], new Customer(uuids[4], "Danial"));
    }};

    public static String getNameByUUID(UUID id) {
        if (!customers.containsKey(id)) {
            return "Not exist uuid";
        }
        return customers.get(id).getName();
    }

    public static UUID getUUIDByName(String name) {
        for (UUID id: customers.keySet()) {
            if (customers.get(id).getName().equals(name)) {
                return id;
            }
        }
        return null;
    }

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

    public static Map<UUID, Customer> copyInitCustomersMap() {
        Map<UUID, Customer> copy = new HashMap<>();
        for (Map.Entry<UUID, Customer> entry: customers.entrySet()){
            copy.put(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    public UUID getId() {
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
}
