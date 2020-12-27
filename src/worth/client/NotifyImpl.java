package worth.client;

import worth.server.ServerInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;

public class NotifyImpl extends RemoteObject implements ClientInterface {

    public NotifyImpl( ) throws RemoteException { super( ); }

    @Override
    public void notifyUserList(int val) throws RemoteException {
        String returnMessage = "Update event received: " + val;
        System.out.println(returnMessage);
    }

    public static void main(String[] args){
        try{
            System.out.println("Cerco il Server");
            Registry registry = LocateRegistry.getRegistry(5000);
            String name = "Server";
            ServerInterface server =(ServerInterface) registry.lookup(name);
            /* si registra per la callback */
            System.out.println("Registering for callback");
            ClientInterface callbackObj =  new NotifyImpl();
            ClientInterface stub = (ClientInterface) UnicastRemoteObject.exportObject(callbackObj, 0);
            server.registerForCallback(stub);
            Thread.sleep(20000);
            System.out.println("Unregistering for callback");
            server.unregisterForCallback(stub);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
