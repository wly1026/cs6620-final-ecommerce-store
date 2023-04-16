package api;


import common.Response.Response;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface KVStore extends Remote {
    Response put(String key, String value) throws RemoteException;

    Response get(String key) throws RemoteException;

    Response delete(String key) throws RemoteException;
}
