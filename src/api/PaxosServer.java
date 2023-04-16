package api;

import common.KVOperation;
import common.Message;
import common.Proposal;
import common.Response.Response;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PaxosServer extends KVStore, Remote {
    // Paxos phase
    Message promise(Proposal proposal) throws RemoteException;
    Message accept(Proposal proposal) throws RemoteException;
    Response learn(Proposal proposal) throws RemoteException;

    // recover kv store
    void recover(int port, String hostname) throws RemoteException;
    void setCoordinator(CoordinatorInterface coordinator) throws RemoteException;

    // retrieve method
    int getPort() throws RemoteException;

    // handle client request for KV operation
    String handleClientRequest(KVOperation operation, String key, String value) throws RemoteException;
}
