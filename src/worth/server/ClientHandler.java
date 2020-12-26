package worth.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class ClientHandler implements Runnable{
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Scanner sc;
    // riferimento al database
    private DatabaseUsers userDb;

    public ClientHandler(Socket socket){
        this.clientSocket = socket;
        sc = new Scanner(System.in);
        this.userDb = new DatabaseUsers();
        synchronized (this){
            userDb.readDb();
        }

    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine = in.readLine(); //qui ho nick e psw
            String[] data = inputLine.split(" ",2); //in data[0] ho il nickname, in data[1] ho la psw
            String psw = Crittografia.hashMe(data[1]);
            // Creo l'utente
            User usr = new User();
            usr.setNickName(data[0]);
            usr.setPassword(psw);
            // cerco l'utente se è presente nel db
            if(userDb.cercaUtente(usr)){
                //prelevo l'utente
                int index_found = userDb.getUserDb().indexOf(usr);
                User found = userDb.getUserDb().get(index_found);
                // devo controllare la password
                if(found.passwordMatch(usr.getPassword())){
                    // SONO QUI E HO FATTO BENE
                    out.println("Login effettuato con successo! Bentornato "+usr.getNickName());
                }else{
                    // PASSWORD ERRATA
                    out.println("La password che hai inserito è errata.");
                    return;
                }
            }else{
                // UTENTE NON ESISTE
                out.println("L'utente inserito non esiste.");
                return;
            }
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

}
