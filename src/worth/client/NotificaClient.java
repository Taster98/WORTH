package worth.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotificaClient extends Remote {
    public void notifyUsers(String lista) throws RemoteException;
}
