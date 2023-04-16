package common;

import java.io.Serializable;

public class KVObject implements Serializable {
    private KVOperation operation;
    private String value;
    private String key;

    public KVObject(KVOperation operation, String key, String value) {
        this.operation = operation;
        this.key = key;
        this.value = value;
    }

    public String getKey(){
        return this.key;
    }

    public String getValue(){
        return this.value;
    }

    public KVOperation getOperation(){
        return this.operation;
    }
}
