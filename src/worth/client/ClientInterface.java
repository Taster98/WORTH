package worth.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {
    // interfaccia usata dal server per mandare notifiche al client.
    public void notifyUserList(int val) throws RemoteException;
}
