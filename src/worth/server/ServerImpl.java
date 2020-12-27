package worth.server;

import worth.client.ClientInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServerImpl extends RemoteObject implements ServerInterface {
    private List<ClientInterface> clients;

    public ServerImpl() throws RemoteException{
        super();
        clients = new ArrayList<>();
    }
    @Override
    public synchronized void registerForCallback(ClientInterface ClientInterface) throws RemoteException {
        if(!clients.contains(ClientInterface)){
            clients.add(ClientInterface);
            System.out.println("New client registered");
        }
    }

    @Override
    public synchronized void unregisterForCallback(ClientInterface ClientInterface) throws RemoteException {
        if(clients.remove(ClientInterface)){
            System.out.println("Client unregistered");
        }else{
            System.out.println("Not working");
        }
    }

    public void update(int val) throws RemoteException{
        doCallBacks(val);
    }

    private synchronized void doCallBacks(int val) throws RemoteException{
        System.out.println("Starting callback");
        Iterator i = clients.iterator();
        while(i.hasNext()){
            ClientInterface client = (ClientInterface) i.next();
            client.notifyUserList(val);
        }
        System.out.println("Callback completed");
    }
    public static void main(String[] args){
        try{
            ServerImpl server = new ServerImpl();
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(server, 39000);
            String name = "Server";
            LocateRegistry.createRegistry(5000);
            Registry registry = LocateRegistry.getRegistry(5000);
            registry.bind(name, stub);
            while(true){
                int val = (int) (Math.random()*1000);
                System.out.println("new update "+val);
                server.update(val);
                Thread.sleep(1500);
            }
        }catch (Exception e){
            System.out.println(e.toString());
        }
    }
}
