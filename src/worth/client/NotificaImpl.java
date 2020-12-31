package worth.client;

import worth.Constants;

import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

public class NotificaImpl extends RemoteObject implements NotificaClient {
    public NotificaImpl()throws RemoteException{
        super();
    }

    @Override
    public void notifyUsers(String lista) throws RemoteException {
        // Splitto la lista:
        lista = lista.replace("?", "\n");
        //System.out.print(Constants.ANSI_GREEN + "Lista utenti aggiornata:\n"+lista + "\n>" + Constants.ANSI_RESET);
        //Non devo stamparla ma devo salvarla nel file userList
        try {
            FileWriter myWriter = new FileWriter("userList.txt");
            myWriter.write(lista);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
