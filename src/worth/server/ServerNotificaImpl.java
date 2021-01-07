package worth.server;

import worth.client.NotificaClient;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.concurrent.CopyOnWriteArrayList;

//Questa classe implementa l'interfaccia per la callback lato server
public class ServerNotificaImpl extends RemoteObject implements NotificaServer {
    // Lista di utenti registrati
    CopyOnWriteArrayList<String> userList;
    // lista dei client associati agli utenti
    CopyOnWriteArrayList<NotificaClient> clientList;

    public ServerNotificaImpl(){
        super();
        clientList = new CopyOnWriteArrayList<>();
        userList = new CopyOnWriteArrayList<>();
    }

    //Utente e client in questa classe sono visti come "entità unica", questo per permettere il riconoscimento e la
    //deregistrazione di uno stesso client, al momento del logout.
    @Override
    public synchronized void register(NotificaClient client, String nick) throws RemoteException {
        if(!userList.contains(nick)){
            userList.add(nick);
            clientList.add(client);
            //System.out.println("Client registered"); DEBUG PRINT
        }
    }

    @Override
    public synchronized void unregister(NotificaClient client, String nick) throws RemoteException {
        if(userList.contains(nick)){
            int i = userList.indexOf(nick);
            userList.remove(nick);
            clientList.remove(i);
            //System.out.println("Client unregistered successfully"); DEBUG PRINT
        }else{
            System.out.println("Unable to unregister client");
        }
    }

    public void update(String usrs) throws RemoteException{
        compute(usrs);
    }

    private synchronized void compute(String usrs) throws RemoteException{
        for(NotificaClient c : clientList){
            c.notifyUsers(usrs);
        }
    }

}
