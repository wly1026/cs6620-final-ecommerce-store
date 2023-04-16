package Server;

import api.CoordinatorInterface;
import api.PaxosServer;
import common.*;
import common.Response.Response;
import common.Response.ResultState;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class KVServer extends UnicastRemoteObject implements PaxosServer {
    private static final Logger LOG = Logger.getLogger("KVServer.class");

    private int port;
    private String hostname;

    private Map<String, String> map = new HashMap();
    private long maxId;
    private Proposal acceptedProposal;
    private CoordinatorInterface coordinator;

    public KVServer(int port, String hostname) throws RemoteException {
        this.maxId = 0;
        this.port = port;
        this.hostname = hostname;
    }

    public int getPort(){
        return this.port;
    }

    @Override
    public String handleClientRequest(KVOperation operation, String key, String value) throws RemoteException {
        KVObject kvPair = new KVObject(operation, key, value);
        Proposal proposal = Proposal.generateProposal(kvPair);
        Response response = this.coordinator.execute(proposal);
        if (response != null){
            return response.getMessage();

        }
        return "FAILED: There has no response";
    }


    @Override
    public Response put(String key, String value) throws RemoteException{
        LOG.info(String.format("server %s perform PUT request for key: %s, value: %s", this.port, value, key));
        Response response = new Response(KVOperation.Put, key);
        this.map.put(key, value);
        response.setState(ResultState.SUCCESS);
        return response;
    }

    @Override
    public Response get(String key) throws RemoteException{
        LOG.info(String.format("server %s perform GET request for key: %s", this.port, key));
        Response response = new Response(KVOperation.Get, key);
        if (!this.map.containsKey(key)){
            response.setState(ResultState.KEY_NOT_FOUND);
        }
        else {
            response.setResponseValue(this.map.get(key));
            response.setState(ResultState.SUCCESS);
        }
        return response;
    }

    @Override
    public Response delete(String key) throws RemoteException{
        LOG.info(String.format("server %s perform DELETE request for key: %s", this.port, key));
        Response response = new Response(KVOperation.Delete, key);
        if (!this.map.containsKey(key)){
            response.setState(ResultState.KEY_NOT_FOUND);
        }
        else {
            this.map.remove(key);
            response.setState(ResultState.SUCCESS);
        }
        return response;
    }

    // phase 1 -- promise
    @Override
    public Message promise(Proposal proposal) throws RemoteException {
        LOG.info(String.format("Acceptor at port %d received PREPARE message for proposal with id %d.", port, proposal.getId()));
        return asyncExecution(PaxosState.PROMISE, proposal);
    }

    // phase 2 -- accept
    @Override
    public Message accept(Proposal proposal) throws RemoteException {
        LOG.info(String.format("Acceptor at port %d received ACCEPT message for proposal with id %d.", port, proposal.getId()));
        return asyncExecution(PaxosState.ACCEPT, proposal);
    }

    private Message asyncExecution(PaxosState phase, Proposal proposal){
        // set random failure
        if (Math.random() < 0.1){
            return null;
        }

        FutureTask<Message> futureTask = new FutureTask<Message>(new Callable<Message>() {
            @Override
            public Message call() {
                // ignore any proposal lower than maxId
                if (proposal.getId() < maxId){
                    return new Message(PaxosState.REJECT, null);
                }else{
                   if (phase.equals(PaxosState.PROMISE)){
                       // promise to ignore any request lower than this proposal id
                       maxId = proposal.getId();
                       // if has accepted anything, reply the accepted proposal with the maxId
                       if (acceptedProposal != null) {
                           return new Message(PaxosState.PROMISE, new Proposal(maxId, acceptedProposal.getKvObject()));
                       } else {
                           //otherwise, reply with promise proposal
                           return new Message(PaxosState.PROMISE, proposal);
                       }
                   } else if(phase.equals(PaxosState.ACCEPT)) {
                       acceptedProposal = proposal;
                       return new Message(PaxosState.ACCEPT, acceptedProposal);
                   }
                }
                return null;
            }
        });

        ExecutorService executor = Executors.newFixedThreadPool(1);
        try {
            executor.submit(futureTask);
            return futureTask.get(10, TimeUnit.SECONDS);
        }catch (Exception e){
            LOG.warning(e.getMessage());
        }
        return null;
    }

    // phase 3 -- learn
    @Override
    public Response learn(Proposal proposal) throws RemoteException {
        LOG.info(String.format("Learner at port %d received ACCEPT message for proposal with id %d.", port, proposal.getId()));
        KVObject kvPair = proposal.getKvObject();
        KVOperation operation = kvPair.getOperation();
        return switch (operation) {
            case Put -> put(kvPair.getKey(), kvPair.getValue());
            case Get -> get(kvPair.getKey());
            case Delete -> delete(kvPair.getKey());
        };
    }


    @Override
    public void recover(int port, String hostname) throws RemoteException {

    }

    @Override
    public void setCoordinator(CoordinatorInterface coordinator) throws RemoteException {
        this.coordinator = coordinator;
    }
}
