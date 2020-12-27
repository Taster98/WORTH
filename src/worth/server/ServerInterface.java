package worth.server;

import worth.client.ClientInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    // Interfaccia usata dal client per registrarsi per la notifica
    /* registrazione per la callback */
    public void registerForCallback(ClientInterface ClientInterface) throws RemoteException;
    /* cancella registrazione per la callback */
    public void  unregisterForCallback  (ClientInterface ClientInterface) throws RemoteException;
}
