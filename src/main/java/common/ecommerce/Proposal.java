package common.ecommerce;


import java.io.Serializable;


public class Proposal implements Serializable {
    private static long serialUID = 0L;
    private long id;
    private Request request;

    public Proposal(long id, Request request) {
        this.id = id;
        this.request = request;
    }

    public long getId() {
        return id;
    }

    public Request getRequest() {
        return request;
    }

    public static synchronized Proposal generateProposal(Request request){
        long timestamp = System.nanoTime();
        Proposal proposal = new Proposal(timestamp, request);
        try{
            Thread.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return proposal;
    }
}
