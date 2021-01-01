package worth.client;

import worth.Constants;

import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

//Implementazione dell'interfaccia per la callback per lo stato degli utenti nel sistema.
public class NotificaImpl extends RemoteObject implements NotificaClient {
    public NotificaImpl()throws RemoteException{
        super();
    }

    //Metodo che salva in locale nei vari client la lista aggiornata degli utenti con il loro stato (online/offline).
    @Override
    public void notifyUsers(String lista) throws RemoteException {
        // Splitto la lista:
        lista = lista.replace("£", "\n");
        //La salvo nel file userList.txt nei client registrati.
        try {
            FileWriter myWriter = new FileWriter(Constants.userListPath);
            myWriter.write(lista);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
