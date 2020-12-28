package worth.server;

import worth.Constants;
import worth.RegistrationInterface;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;

public class RegistrationServer implements RegistrationInterface {
    DatabaseUsers userDb;

    public RegistrationServer() {
        userDb = new DatabaseUsers();
        // la lettura non è atomica, va fatta in mutua esclusione
        userDb.readDb();
    }

    // Ritorna 7 se la registrazione è andata a buon fine
    @Override
    public int register(String nickUtente, String password) throws RemoteException {
        boolean andata = false;
        // Nickname e password non devono essere nè null nè vuoti
        if (nickUtente == null || password == null) throw new NullPointerException();
        if (nickUtente.equals("") || password.equals("")) throw new IllegalArgumentException();

        //La password va hashata, per non memorizzarla in chiaro
        try {
            String newPwd = Crittografia.hashMe(password);
            // Creo ora un utente
            User user = new User();
            user.setNickName(nickUtente);
            user.setPassword(newPwd);

            // Ora che ho creato l'utente, devo inserirlo nel database
            andata = userDb.addUser(user);
            // la scrittura non è atomica, va fatta in mutua esclusione
            userDb.writeDb();
            userDb.readDb();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if(andata) return 7;
        else return 0;
    }


    public static void main(String[] args) {
        try {
            RegistrationServer srv = new RegistrationServer();
            // Creo lo stub per la registrazione:
            RegistrationInterface stub = (RegistrationInterface) UnicastRemoteObject.exportObject(srv, 0);

            // faccio il bind dell'oggetto remoto nel registry
            LocateRegistry.createRegistry(30001);
            Registry registry = LocateRegistry.getRegistry(30001);
            registry.rebind("RegistrationInterface", stub);

            // Gestione callback notifiche
            ServerNotImpl serverCB = new ServerNotImpl();
            NotificaServer stubCB = (NotificaServer) UnicastRemoteObject.exportObject(serverCB, 39000);
            String name = "Server";
            LocateRegistry.createRegistry(5000);
            Registry registryCB = LocateRegistry.getRegistry(5000);
            registryCB.bind(name, stubCB);
            // Ora voglio lanciare un thread a cui passo il mio serverCB che gestirà la callback
            System.err.println(Constants.ANSI_CYAN+"Server ready."+Constants.ANSI_RESET);

            // Avvio server TCP, passandogli il callback server
            TCPServer server = new TCPServer(serverCB);
            server.start();
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }

    }
}
