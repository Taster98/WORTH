package worth.client;

import worth.RegistrationInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RegistrationClient {
    private RegistrationClient(){}

    public static void main(String[] args){
        // Specifico che l'host deve essere il localhost
        String host = null;
        // nickname e password li prendo dagli argomenti in linea di comando:
        if(args.length < 2) {
            System.err.println("Usage: ./runclient nickname password");
            return;
        }
        String nickname = args[0];
        String pwd = args[1];
        try{
            Registry registry = LocateRegistry.getRegistry(host);
            RegistrationInterface stub = (RegistrationInterface) registry.lookup("RegistrationInterface");
            String response = stub.register(nickname, pwd);
            System.out.println("Risposta: "+response);
        }catch(Exception e){
            System.err.println("Client exception: "+e.toString());
            e.printStackTrace();
        }
    }
}
