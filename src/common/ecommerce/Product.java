package common.ecommerce;

import java.util.HashMap;
import java.util.Map;

public class Product {
    private String name;
    private int stock;
    private double price;

    public static Map<String, Product> products = new HashMap<>(){{
        put("apple", new Product("apple", 5, 0.99));
        put("shirt", new Product("shirt", 99, 12.99));
        put("book", new Product("book", 99, 15.99));
        put("watermelon", new Product("watermelon", 99, 5.99));
        put("juice", new Product("juice", 99, 2.99));
        put("milk", new Product("milk", 99, 1.99));
        put("egg", new Product("egg", 99, 0.99));
        put("coffee", new Product("coffee", 99, 3.99));
    }};

    public Product(String name, int stock, double price) {
        this.name = name;
        this.stock = stock;
        this.price = price;
    }

    public static String printProducts() {
        StringBuilder sb = new StringBuilder();
        sb.append("The Products are listed as follows: ").append("\n");
        for (String productName: products.keySet()) {
            sb.append(productName).append(": $").append(products.get(productName).getPrice()).append("\n");
        }
        return sb.toString();
    }

    public static Map<String, Product> copyInitProductsMap() {
        Map<String, Product> copy = new HashMap<>();
        for (Map.Entry<String, Product> entry: products.entrySet()){
            copy.put(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    public int getStock() {
        return stock;
    }

    public double getPrice() {
        return price;
    }

    public void deductStock(Integer count) {
        this.stock -= count;
    }
}
