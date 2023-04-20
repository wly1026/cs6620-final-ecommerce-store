package common.ecommerce;

import common.KVOperation;

import java.util.UUID;

public class Request {
    private UUID customerId;
    private CartOperation operation;
    private int count;
    private String productName;

    public Request(CartOperation operation){
        this.operation = operation;
    }

    public Request(UUID customerId, CartOperation operation, String key, int value) {
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

    public UUID getCustomerId(){
        return this.customerId;
    }

    public CartOperation getOperation(){
        return this.operation; }
}
