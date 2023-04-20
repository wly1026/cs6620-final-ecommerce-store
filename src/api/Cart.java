package api;

import common.Response.Response;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface Cart extends Remote{
    Response checkout(UUID customerId) throws RemoteException;

    Response add(UUID customerId, String productName, int count) throws RemoteException;

    Response delete(UUID customerId, String productName, int count) throws RemoteException;

    Response remove(UUID customerId, String productName) throws RemoteException;

    Response get(UUID customerId) throws RemoteException;
}
