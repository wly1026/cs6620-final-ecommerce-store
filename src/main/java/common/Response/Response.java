package common.Response;

import common.ecommerce.CartOperation;

import java.io.Serializable;
import java.util.UUID;

public class Response implements Serializable {
    private int customerId;
    private ResultState state;
    private CartOperation operation;
    private String additionalInfo;
    private String productName;

//    public Response(int customerId){
//        this.customerId = customerId;
//    }
//
//    public Response(int customerId, ResultState state) {
//        this.customerId = customerId;
//        this.state = state;
//    }
//
//    public Response(int customerId, CartOperation operation) {
//        this.customerId = customerId;
//        this.operation = operation;
//    }

    public Response(int customerId, CartOperation operation, String product){
        this.customerId = customerId;
        this.operation = operation;
        this.productName = product;
    }

    // case 1. checkout -> fail: insufficient stock item
    // case 2. checkout -> success: total price
    // case 2. get -> current cart item list
    // case 3. add, delete -> updated item count
    public void setAdditionalInfo(String info){
        this.additionalInfo = info;
    }

    public String getMessage(){
        String message = "";
        if (this.state == null){
            return String.format("customer %s | FAILED: There has no response", this.customerId);
        }
        switch (state) {
            case INSUFFICIENT_STOCK -> message = String.format("customer %s | FAILED: Item %s has insufficient stock", this.customerId, this.additionalInfo);
            case CUSTOMER_NOT_FOUND -> message = String.format("customer %s | FAILED: Customer is not found", this.customerId);
            case CONSENSUS_NOT_REACH -> message = String.format("customer %s | FAILED: Consensus is not reached among the acceptors", this.customerId);
            case PRODUCT_NOT_FOUND -> message = String.format("customer %s | PRODUCT_NOT_FOUND: product at %s has not found.", this.customerId, this.productName);
            case SUCCESS -> {
                switch (operation) {
                    case Add -> message = String.format("customer %s | SUCCESS: Add operation done for product %s, updated count: %s", this.customerId, this.productName, this.additionalInfo);
                    case Get -> message = String.format("customer %s | SUCCESS: Get operation done, cart: %s",this.customerId, this.additionalInfo);
                    case Delete -> message = String.format("customer %s | SUCCESS: Delete operation done for product %s", this.customerId, this.productName);
                    case Remove -> message = String.format("customer %s | SUCCESS: Remove operation done for product %s", this.customerId, this.productName);
                    case CheckOut-> message = String.format("customer %s | SUCCESS: Checkout operation done, %s", this.customerId, this.additionalInfo);
                    default -> message = String.format("customer %s | FAILED: There has something wrong response", this.customerId);
                }
            }
            default -> message = String.format("customer %s | FAILED: There has no response", this.customerId);
        }

        return message;
    }
    public void setState(ResultState newState){
        this.state = newState;
    }
}
