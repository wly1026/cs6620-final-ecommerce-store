package api;

import common.Message;
import common.Response.Response;
import common.ecommerce.CartOperation;
import common.ecommerce.Customer;
import common.ecommerce.Product;
import common.ecommerce.Proposal;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.UUID;

public interface CartPaxosServer extends Cart, Remote {
    // Paxos phase
    Message promise(Proposal proposal) throws RemoteException;
    Message accept(Proposal proposal) throws RemoteException;
    Response learn(Proposal proposal) throws RemoteException;

    // recover kv store
    void recover(int port, String hostname) throws RemoteException;
    Map<Integer, Customer> copyCustomersMap() throws RemoteException;
    Map<String, Product> copyProductsMap() throws RemoteException;;

    void setCoordinator(CoordinatorInterface coordinator) throws RemoteException;

    // retrieve method
    int getPort() throws RemoteException;

    // handle client request for cart operation
    String handleClientRequest(int customerId, CartOperation operation, String key, int value) throws RemoteException;
}
