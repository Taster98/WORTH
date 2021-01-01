package worth.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Interfaccia per la callback delle notifiche delle liste utenti (online e offline).
public interface NotificaClient extends Remote {
    public void notifyUsers(String lista) throws RemoteException;
}
