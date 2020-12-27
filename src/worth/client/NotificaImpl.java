package worth.client;

import worth.Constants;
import worth.server.DatabaseUsers;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

public class NotificaImpl extends RemoteObject implements NotificaClient {
    DatabaseUsers userDb;
    public NotificaImpl()throws RemoteException{
        super();
        userDb = new DatabaseUsers();
    }
    @Override
    public void notifyUsers(String lista) throws RemoteException {
        // Splitto la lista:
       lista = lista.replace("?", "\n");
        System.out.println(Constants.ANSI_GREEN + "Lista utenti aggiornata:\n"+lista + ">" + Constants.ANSI_RESET);
    }
}
