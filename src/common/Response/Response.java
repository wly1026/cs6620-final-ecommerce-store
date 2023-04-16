package common.Response;

import common.KVOperation;

import java.io.Serializable;

public class Response implements Serializable {
    private ResultState state;
    private KVOperation operation;
    private String responseValue;
    private String key;

    public Response(){
    }

    public Response(KVOperation operation, String key){
        this.operation = operation;
        this.key = key;
    }

    public void setResponseValue(String value){
        this.responseValue = value;
    }


    public String getMessage(){
        String message = "";
        if (this.state == null){
            return "FAILED: There has no response";
        }
        switch (state) {
            case CONSENSUS_NOT_REACH -> message = "FAILED: Consensus is not reached among the acceptors";
            case KEY_NOT_FOUND -> message = String.format("KEY_NOT_FOUND: Key at %s has not found.", this.key);
            case SUCCESS -> {
                switch (operation) {
                    case Put -> message = String.format("SUCCESS: Put operation done for key %s", this.key);
                    case Get -> message = String.format("SUCCESS: Get operation done for key %s, value is %s", this.key, this.responseValue);
                    case Delete -> message = String.format("SUCCESS: Delete operation done for key %s", this.key);
                    default -> message = "FAILED: There has something wrong response";
                }
            }
            default -> message = "FAILED: There has no response";
        }

        return message;
    }

    public void setState(ResultState newState){
        this.state = newState;
    }
}
