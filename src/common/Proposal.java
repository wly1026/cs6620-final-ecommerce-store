package common;

import java.io.Serializable;


public class Proposal implements Serializable {
    private static long serialUID = 0L;
    private long id;
    private KVObject kvObject;

    public Proposal(long id, KVObject kvObject){
        this.id = id;
        this.kvObject = kvObject;
    }

    public long getId(){
        return this.id;
    }

    public KVObject getKvObject(){
        return this.kvObject;
    }

    public static synchronized Proposal generateProposal(KVObject kvPair){
        long timestamp = System.nanoTime();
        Proposal proposal = new Proposal(timestamp, kvPair);
        try{
            Thread.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return proposal;
    }
}
