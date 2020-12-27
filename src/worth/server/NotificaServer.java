package worth.server;

import worth.client.NotificaClient;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotificaServer extends Remote {
    // metodo per registrarsi al publish-subscribe
    public void register(NotificaClient NotificaClient) throws RemoteException;
    // metodo per deregistrarsi al publish-subscribe
    public void unregister(NotificaClient NotificaClient) throws RemoteException;
}
