package worth.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import worth.Constants;
import worth.RegistrationInterface;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/*Questa è la classe che rappresenta il server principale, che si occupa di registrare la RMI per la registrazione di un utente
e l'avvio di un'istanza di server TCP.
 */
public class ServerMain implements RegistrationInterface {
    DatabaseUsers userDb;
    public ServerMain() {
        userDb = new DatabaseUsers();
    }

    // Ritorna 7 se la registrazione è andata a buon fine
    @Override
    public synchronized int register(String nickUtente, String password) throws RemoteException {
        boolean andata = false;
        // Nickname e password non devono essere nè null nè vuoti
        if (nickUtente == null || password == null) throw new NullPointerException();
        if (nickUtente.equals("") || password.equals("")) throw new IllegalArgumentException();

        //La password va hashata, per non memorizzarla in chiaro
        try {
            userDb.readDb();
            String newPwd = Crittografia.hashMe(password);
            // Creo ora un utente
            User user = new User();
            user.setNickName(nickUtente);
            user.setPassword(newPwd);

            // Ora che ho creato l'utente, devo inserirlo nel database
            andata = userDb.addUser(user);
            // la scrittura non è atomica, va fatta in mutua esclusione
            userDb.writeDb();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if(andata) return 7;
        else return 0;
    }
    public static void restore(){
        File f1 = new File(Constants.fileProgettiPath);
        File f2 = new File(Constants.auxProjectList);
        ArrayList<String> ips, auxs;
        try {
            //Copio il contenuto di f1 in f2
            Files.copy(f1.toPath(),f2.toPath(), StandardCopyOption.REPLACE_EXISTING);
            //Leggo i contenuti di f2 ed f3
            Gson gson = new Gson();
            BufferedReader br;
            br = new BufferedReader(new FileReader(Constants.auxProjectList));
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            auxs = gson.fromJson(br, type);
            br = new BufferedReader(new FileReader(Constants.ipAddressPath));
            ips = gson.fromJson(br, type);
            List<String> newips = null;
            if(ips != null) {
                newips = ips.subList(0, auxs.size());
            }
            //Ora posso riscrivere la lista di ip:
            Writer writer = new FileWriter(Constants.ipAddressPath);
            gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(newips, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void cleanUp(){
        //Svuoto le liste
        File f1 = new File(Constants.auxProjectList);
        PrintWriter writer;
        try {
            writer = new PrintWriter(f1);
            writer.print("");
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        //Restore dello stato del server
        restore();
        try {
            ServerMain srv = new ServerMain();
            // Creo lo stub per la registrazione:
            RegistrationInterface stub = (RegistrationInterface) UnicastRemoteObject.exportObject(srv, 0);

            // faccio il bind dell'oggetto remoto nel registry
            LocateRegistry.createRegistry(30001);
            Registry registry = LocateRegistry.getRegistry(30001);
            registry.rebind("RegistrationInterface", stub);

            // Gestione callback notifiche
            ServerNotificaImpl serverCB = new ServerNotificaImpl();
            NotificaServer stubCB = (NotificaServer) UnicastRemoteObject.exportObject(serverCB, 39000);
            String name = "Server";
            LocateRegistry.createRegistry(5000);
            Registry registryCB = LocateRegistry.getRegistry(5000);
            registryCB.bind(name, stubCB);
            // Ora voglio lanciare un thread a cui passo il mio serverCB che gestirà la callback
            System.err.println(Constants.ANSI_CYAN+"Server ready."+Constants.ANSI_RESET);

            // Avvio server TCP, passandogli il callback server
            TCPServer server = new TCPServer(serverCB);

            //Catturo il sigint CTRL+C
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        Thread.sleep(200);
                        //Pulisco i file
                        cleanUp();
                        //chiudo la connessione
                        server.shutdown = true;
                        try {
                            server.stop();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //Ultima print
                        System.out.println("\nServer stopped.");

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }
                }
            });
            //Avvio il server
            server.start();
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
