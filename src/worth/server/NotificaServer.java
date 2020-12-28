package worth.server;

import worth.client.NotificaClient;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotificaServer extends Remote {
    // metodo per registrarsi al publish-subscribe
    public void register(NotificaClient NotificaClient, String nick) throws RemoteException;
    // metodo per deregistrarsi al publish-subscribe
    public void unregister(NotificaClient NotificaClient, String nick) throws RemoteException;
}
