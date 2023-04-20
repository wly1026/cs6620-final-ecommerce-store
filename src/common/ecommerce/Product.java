package common.ecommerce;

public class Product {
    private String name;
    private int stock;
    private int price;

    public Product(String name, int stock, int price) {
        this.name = name;
        this.stock = stock;
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public int getPrice() {
        return price;
    }

    public void deductStock(Integer count) {
        this.stock -= count;
    }
}
