package coordinator;

import api.CartPaxosServer;
import common.Response.*;
import api.CoordinatorInterface;
import api.CartPaxosServer;
import common.*;
import common.ecommerce.Proposal;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Logger;

public class Coordinator extends UnicastRemoteObject implements CoordinatorInterface {
    private static final Logger LOG = Logger.getLogger("coordinator.class");
    private Set<Map.Entry<String, Integer>> servers;
    private ArrayList<CartPaxosServer> acceptors;

    public Coordinator() throws RemoteException {
        super();
        this.servers = new HashSet<>();
    }

    @Override
    public Response execute(Proposal proposal) throws RemoteException {
        this.acceptors = new ArrayList<>();
        int customerId = proposal.getRequest().getCustomerId();
        Response response = new Response(customerId, proposal.getRequest().getOperation(), proposal.getRequest().getProductName());
        for (Map.Entry<String, Integer> server: this.servers) {
            try {
                CartPaxosServer acceptor = (CartPaxosServer) Naming.lookup("rmi://" + server.getKey() + ":" + server.getValue() + "/kvServer");
                acceptors.add(acceptor);
            } catch (Exception e) {
                // fail to connect ot server
                LOG.warning(String.format("Fail to connect to server at port %d, error: %s", server.getValue(), e.getMessage()));
                continue;
            }
        }

        // phase 1a -- proposer send PREPARE message
        if (!this.isPrepared(proposal)){
            response.setState(ResultState.CONSENSUS_NOT_REACH);
            return response;
        }

        // phase 2a -- proposer send ACCEPT_REQUEST message to acceptor
        if (!this.isAccepted(proposal)){
            // consensus not reached
            response.setState(ResultState.CONSENSUS_NOT_REACH);
            return response;
        }

        // phase 3 -- learn the message
        for (CartPaxosServer acceptor: acceptors) {
            try {
                response =  acceptor.learn(proposal);
            } catch (Exception e) {
                LOG.warning(e.getMessage());
                continue;
            }
        }

        return response;
    }

    private int getMajority(){
        return Math.floorDiv(this.acceptors.size(), 2) + 1;
    }

    // phase 1b -- get replied PROMISE message from acceptors
    private boolean isPrepared(Proposal proposal) throws RemoteException{
        int promised = 0;
        Message message;
        for (CartPaxosServer acceptor: acceptors){
            try {
                message = acceptor.promise(proposal);
                if (message == null) {
                    LOG.info(String.format("RESPONSE: Acceptor at port %d has NO RESPONSE for id %d.", acceptor.getPort(), proposal.getId()));
                } else if (message.getState().equals(PaxosState.PROMISE) | message.getState().equals(PaxosState.ACCEPT)) {
                    promised++;
                    LOG.info(String.format("RESPONSE: Acceptor at port %d has PROMISE for id %d.", acceptor.getPort(), proposal.getId()));
                } else if (message.getState().equals(PaxosState.REJECT)) {
                    LOG.info(String.format("RESPONSE: Acceptor at port %d has REJECT for id %d.", acceptor.getPort(), proposal.getId()));
                } else {
                    LOG.info(String.format("RESPONSE: Acceptor at port %d has something wrong.", acceptor.getPort()));
                }
            }catch (Exception e){
                LOG.info(String.format("RESPONSE: Acceptor at port %d has something wrong.", acceptor.getPort()));
                continue;
            }
        }

        return promised >= getMajority();
    }

    // phase 2b -- announce: get replied ACCEPT message from acceptors and send it to learner
    private boolean isAccepted(Proposal proposal) throws RemoteException {
        int accepted = 0;
        Message response;
        for (CartPaxosServer acceptor: acceptors){
            try {
                response = acceptor.accept(proposal);
                if (response == null) {
                    LOG.info(String.format("RESPONSE: Acceptor at port %d has NO RESPONSE for id %d.", acceptor.getPort(), proposal.getId()));
                } else if (response.getState().equals(PaxosState.ACCEPT)) {
                    accepted++;
                } else if (response.getState().equals(PaxosState.REJECT)) {
                    LOG.info(String.format("RESPONSE: Acceptor at port %d has REJECT for id %d.", acceptor.getPort(), proposal.getId()));
                } else {
                    LOG.info(String.format("RESPONSE: Acceptor at port %d has something wrong.", acceptor.getPort()));
                }
            }catch (Exception e){
                LOG.info(String.format("RESPONSE: Acceptor at port %d has something wrong.", acceptor.getPort()));
                continue;
            }
        }

        return accepted >= getMajority();
    }


    @Override
    public void addAcceptor(int port, String hostname) throws RemoteException {
        this.servers.add(Map.entry(hostname, port));
    }
}
