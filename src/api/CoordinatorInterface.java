package api;

import common.Proposal;
import common.Response.Response;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CoordinatorInterface extends Remote{
    Response execute(Proposal proposal) throws RemoteException;
    void addAcceptor(int port, String hostname) throws RemoteException;
}
