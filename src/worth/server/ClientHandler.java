package worth.server;

import worth.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class ClientHandler implements Runnable{
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Scanner sc;
    // riferimento al database
    private DatabaseUsers userDb;
    // variabile di controllo per far sì che un client si connetta solo con un user alla volta
    private boolean logged;
    // utente collegato a questo thread
    private User utente;
    // server per il callback
    private ServerNotImpl serverCB;
    public ClientHandler(Socket socket, ServerNotImpl serverCB){
        this.logged = false;
        this.clientSocket = socket;
        sc = new Scanner(System.in);
        this.userDb = new DatabaseUsers();
        userDb.readDb();
        utente = new User();
        this.serverCB = serverCB;
    }
    private void actionHandler(String action){
        // Qui posso avere diverse azioni da voler fare;
        if(action == null) throw new NullPointerException();
        if(action.equals("")) throw new IllegalArgumentException();
        // Ora che sono certo che la stringa non sia vuota:
        String[] data = action.split(" ", 2); // in data[0] ho il comando
        try {
            switch (data[0]) {
                case "login":
                    userDb.readDb();
                    // Se c'è già un utente loggato non devo fare nulla
                    if(!logged) {
                        String[] info = data[1].split(" ", 2); //in info[0] ho il nickname, in info[1] ho la psw
                        String psw = Crittografia.hashMe(info[1]);
                        // Creo l'utente
                        utente.setNickName(info[0]);
                        utente.setPassword(psw);
                        // cerco l'utente se è presente nel db
                        if (userDb.cercaUtente(utente)) {
                            //prelevo l'utente
                            int index_found = userDb.getUserDb().indexOf(utente);
                            User found = userDb.getUserDb().get(index_found);
                            // devo controllare la password
                            if (found.passwordMatch(utente.getPassword())) {
                                //La password è corretta ma ancora non ho finito:
                                // Se è già online non può accedere.
                                if (found.getStato().equals("online")) {
                                    out.println(Constants.ANSI_RED +"User is already connected!"+Constants.ANSI_RESET);
                                    return;
                                } else {
                                    // SONO QUI E HO FATTO BENE
                                    out.println(Constants.ANSI_GREEN +"Login success! Welcome back " + utente.getNickName()+Constants.ANSI_RESET);
                                    // Devo aggiornare il database
                                    userDb.setStatus(utente, "online");
                                    serverCB.update(userDb.getListStatus());
                                    // Setto il flag logged
                                    logged = true;
                                }

                            } else {
                                // PASSWORD ERRATA
                                out.println(Constants.ANSI_RED +"Password is wrong!"+Constants.ANSI_RESET);
                                break;
                            }
                        } else {
                            // UTENTE NON ESISTE
                            out.println(Constants.ANSI_RED +"This username doesn't exist!"+Constants.ANSI_RESET);
                            return;
                        }
                    }else{
                        // SONO GIÀ COLLEGATO CON UN ACCOUNT
                        out.println(Constants.ANSI_RED +"You must logout from current account in order to login with another one!" + Constants.ANSI_RESET);
                        return;
                    }
                    break;
                case "logout":
                    if(logged){
                        if(!utente.getNickName().equals("") && userDb.cercaUtente(utente)){
                            userDb.setStatus(utente,"offline");
                            logged = false;
                            serverCB.update(userDb.getListStatus());
                            out.println(Constants.ANSI_GREEN +"Logout successful!" + Constants.ANSI_RESET);
                        }
                    }else{
                        // NON SONO LOGGATO!
                        out.println(Constants.ANSI_RED +"You can't logout without login first!" + Constants.ANSI_RESET);
                        return;
                    }
                    break;
                case "users":
                    if(logged){
                        String toPrint = userDb.getListStatus();
                        out.println(toPrint);
                    }else{
                        // NON SONO LOGGATO!
                        out.println(Constants.ANSI_RED +"You can't perform this without login first!" + Constants.ANSI_RESET);
                    }
                    break;
                case "online":
                    if(logged){
                        out.println(Constants.ANSI_GREEN+userDb.getOnlineListStatus()+Constants.ANSI_RESET);
                    }else{
                        // NON SONO LOGGATO!
                        out.println(Constants.ANSI_RED +"You can't perform this without login first!" + Constants.ANSI_RESET);
                    }
                    break;
            }
        } catch (NoSuchAlgorithmException | IOException e){
            e.printStackTrace();
        }
        // Se per qualche motivo un utente prova a rifare la registrazione mentre è loggato, devo dirglielo che non può
    }
    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while(true) {
                if(in != null)
                    actionHandler(in.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
