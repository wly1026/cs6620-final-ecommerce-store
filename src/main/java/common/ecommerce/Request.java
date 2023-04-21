package common.ecommerce;

import java.io.Serializable;

public class Request implements Serializable {
    private int customerId;
    private CartOperation operation;
    private int count;
    private String productName;

    public Request(int customerId, CartOperation operation, String productName, int count) {
        this.customerId = customerId;
        this.operation = operation;
        this.productName = productName;
        this.count = count;
    }

    public String getProductName(){
        return this.productName;
    }

    public int getCount(){
        return this.count;
    }

    public int getCustomerId(){
        return this.customerId;
    }

    public CartOperation getOperation(){
        return this.operation; }
}
