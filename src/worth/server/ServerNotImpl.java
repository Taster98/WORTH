package worth.server;

import worth.client.NotificaClient;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerNotImpl extends RemoteObject implements NotificaServer {
    // Lista di utenti registrati
    CopyOnWriteArrayList<NotificaClient> userList;

    public ServerNotImpl(){
        super();
        userList = new CopyOnWriteArrayList<>();
    }
    @Override
    public synchronized void register(NotificaClient client) throws RemoteException {
        if(userList.addIfAbsent(client)) System.out.println("Client registered");
    }

    @Override
    public synchronized void unregister(NotificaClient client) throws RemoteException {
        if(userList.remove(client)){
            System.out.println("Client unregistered successfully");
        }else{
            System.out.println("Unable to unregister client");
        }
    }

    public void update(String usrs) throws RemoteException{
        compute(usrs);
    }

    private synchronized void compute(String usrs) throws RemoteException{
        System.out.println("Starting callback");
        for(NotificaClient c : userList){
            c.notifyUsers(usrs);
        }
    }

}
