package api;

import common.Response.Response;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Cart extends Remote{
    Response checkout(int customerId) throws RemoteException;

    Response add(int customerId, String productName, int count) throws RemoteException;

    Response delete(int customerId, String productName, int count) throws RemoteException;

    Response remove(int customerId, String productName) throws RemoteException;

    Response get(int customerId) throws RemoteException;
}
