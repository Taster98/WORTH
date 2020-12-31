package worth.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import worth.Constants;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientHandler implements Runnable{
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Scanner sc;
    // riferimento al database
    private DatabaseUsers userDb;
    // riferimento alla lista progetti
    private CopyOnWriteArrayList<String> projects;
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
        this.projects = new CopyOnWriteArrayList<>();
    }
    //funzione che scrive nel file con la lista di progetti
    private synchronized void readProjects(){
        Gson gson = new Gson();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(Constants.fileProgettiPath));
            Type type = new TypeToken<CopyOnWriteArrayList<String>>() {
            }.getType();
            projects = gson.fromJson(br, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    //funzione che legge dal file la lista dei progetti
    public synchronized int writeProjects() {
        Writer writer;
        try {
            writer = new FileWriter(Constants.fileProgettiPath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(projects, writer);
            writer.flush();
            writer.close();
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
    //funzione che aggiunge un progetto
    public synchronized boolean addProject(String prj) {
        if(projects == null)
            projects = new CopyOnWriteArrayList<>();
        return projects.addIfAbsent(prj);
    }

    private void actionHandler(String action){
        // Qui posso avere diverse azioni da voler fare;
        if(action == null) {
            return; //se l'azione è vuota esco
        }
        if(action.equals("")) throw new IllegalArgumentException();
        // Ora che sono certo che la stringa non sia vuota:
        String[] data = action.split(" ", 2); // in data[0] ho il comando
        try {
            switch (data[0]) {
                case "login":
                    // Il comando deve avere 2 argomenti: se ne ha uno solo, o solo due, stampo wrong usage
                    if(data.length == 2) {
                        // Se c'è già un utente loggato non devo fare nulla
                        if (!logged) {
                            userDb.readDb(); //<-- FIXARE QUESTO
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
                                        out.println(Constants.ANSI_RED + "User is already connected!" + Constants.ANSI_RESET);
                                        return;
                                    } else {
                                        // SONO QUI E HO FATTO BENE
                                        out.println(Constants.ANSI_GREEN + "Login success! Welcome back " + utente.getNickName() + Constants.ANSI_RESET);
                                        // Devo aggiornare il database
                                        userDb.setStatus(utente, "online");
                                        userDb.writeDb();
                                        serverCB.update(userDb.getListStatus());
                                        // Setto il flag logged
                                        logged = true;
                                    }

                                } else {
                                    // PASSWORD ERRATA
                                    out.println(Constants.ANSI_RED + "Password is wrong!" + Constants.ANSI_RESET);
                                    break;
                                }
                            } else {
                                // UTENTE NON ESISTE
                                out.println(Constants.ANSI_RED + "This username doesn't exist!" + Constants.ANSI_RESET);
                                return;
                            }
                        } else {
                            // SONO GIÀ COLLEGATO CON UN ACCOUNT
                            out.println(Constants.ANSI_RED + "You must logout from current account in order to login with another one!" + Constants.ANSI_RESET);
                            return;
                        }
                    }else{
                        out.println(Constants.ANSI_RED + "Wrong usage: try with login [nickname] [password]"+ Constants.ANSI_RESET);
                        return;
                    }
                    break;
                case "logout":
                    if(data.length == 2) {
                        if (logged) {
                            userDb.readDb();
                            String nick = data[1];
                            User usr = new User();
                            usr.setNickName(nick);

                            if (!nick.equals("") && userDb.cercaUtente(usr)) {
                                if(usr.getNickName().equals(utente.getNickName())) {
                                    usr = userDb.getUtente(usr);
                                    userDb.setStatus(usr, "offline");
                                    logged = false;
                                    userDb.writeDb();
                                    serverCB.update(userDb.getListStatus());
                                    out.println(Constants.ANSI_GREEN + "Logout successful!" + Constants.ANSI_RESET);
                                }else{
                                    // NICKNAME NON GIUSTO!
                                    out.println(Constants.ANSI_RED + "Invalid nickname!" + Constants.ANSI_RESET);
                                    return;
                                }
                            }else{
                                // NICKNAME NON GIUSTO!
                                out.println(Constants.ANSI_RED + "Invalid nickname!" + Constants.ANSI_RESET);
                                return;
                            }
                        } else {
                            // NON SONO LOGGATO!
                            out.println(Constants.ANSI_RED + "You can't logout without login first!" + Constants.ANSI_RESET);
                            return;
                        }
                    }else{
                        out.println(Constants.ANSI_RED + "Wrong usage: try with logout [nickname]"+ Constants.ANSI_RESET);
                        return;
                    }
                    break;
                case "createProject":
                    if(data.length == 2){
                        if(logged){
                            //leggo i progetti dal file
                            readProjects();
                            //aggiungo il progetto al file
                            if(addProject(data[1])){
                                //Se invece il progetto con quel nome non esisteva ed è quindi andato a buon fine, posso continuare.
                                //Devo quindi creare un oggetto di tipo Project
                                Project p = new Project(data[1]);
                                //Aggiungo l'utente creatore alla lista di utenti del progetto
                                p.addUser(utente.getNickName());
                                //Creo la directory
                                String dir = p.createDir(data[1]);
                                //Creo i 5 file dentro la directory appena creata, che rappresentano le 5 liste
                                if(dir != null){
                                    if(p.generateLists(dir)){
                                        //Qui dentro ho finito, quindi salvo la lista corrente di tutti i progetti e in più stampo risultato positivo.
                                        writeProjects();
                                        //Scrivo anche l'utente creatore nella lista utenti
                                        if(p.writeUserList(dir)){
                                            utente.setStato("online");
                                            if(userDb.addProject(utente,data[1])){
                                                userDb.readDb();
                                                out.println(Constants.ANSI_GREEN + "Project '"+data[1]+"' created successfully!" + Constants.ANSI_RESET);
                                            }else{
                                                out.println(Constants.ANSI_RED + "Error while updating users file. Please try again." +Constants.ANSI_RESET);
                                            }
                                        }else{
                                            out.println(Constants.ANSI_RED + "Error while writing users file. Please try again." +Constants.ANSI_RESET);
                                        }

                                    }else{
                                        out.println(Constants.ANSI_RED + "Error while creating files. Please try again." +Constants.ANSI_RESET);
                                    }
                                }else{
                                    out.println(Constants.ANSI_RED + "Error while creating directory. Please try again." +Constants.ANSI_RESET);
                                }
                            }else{
                                // PROGETTO GIÀ ESISTENTE
                                out.println(Constants.ANSI_RED + "Error while creating project. Try using a different name." + Constants.ANSI_RESET);
                            }
                        }else{
                            // NON SONO LOGGATO!
                            out.println(Constants.ANSI_RED + "You can't perform this without login first!" + Constants.ANSI_RESET);
                        }
                    }
                    break;
                case "listProjects":
                    if(data.length == 1) {
                        if (logged) {
                            userDb.readDb();
                            out.println(Constants.ANSI_GREEN + userDb.getUserProjectList(utente) + Constants.ANSI_RESET);
                        } else {
                            // NON SONO LOGGATO!
                            out.println(Constants.ANSI_RED + "You can't perform this without login first!" + Constants.ANSI_RESET);
                        }
                    }
                    break;
                case "showMembers":
                    if(data.length == 2){
                        if(logged){
                            userDb.readDb();
                            //deve essere membro del progetto per poter mostrare i membri del progetto
                            if(userDb.isMember(data[1],utente)){
                                //qui allora posso leggere la lista di membri
                                out.println(Constants.ANSI_GREEN + userDb.getMemberList(data[1]) + Constants.ANSI_RESET);
                            }else{
                                // NON AMMESSO!
                                out.println(Constants.ANSI_RED + "You don't have access to this project!" + Constants.ANSI_RESET);
                            }
                        }else{
                            // NON SONO LOGGATO!
                            out.println(Constants.ANSI_RED + "You can't perform this without login first!" + Constants.ANSI_RESET);
                        }
                    }
                    break;
                case "addMember":
                    if(data.length == 2){
                        if(logged){
                            userDb.readDb();
                            String[] cmds = data[1].split(" ",2);
                            if(cmds.length == 2) {
                                if (userDb.isMember(cmds[0], utente)) {
                                    User usr = new User();
                                    usr.setNickName(cmds[1]);
                                    if (userDb.addMemberToList(cmds[0], usr)) {
                                        // MEMBER ADDED
                                        out.println(Constants.ANSI_GREEN + usr.getNickName() + " added successfully!" + Constants.ANSI_RESET);
                                    } else {
                                        // ALREADY IN
                                        out.println(Constants.ANSI_RED + "The user is already inside this project!" + Constants.ANSI_RESET);
                                    }
                                } else {
                                    // NON AMMESSO!
                                    out.println(Constants.ANSI_RED + "You don't have access to this project!" + Constants.ANSI_RESET);
                                }
                            }else{
                                out.println(Constants.ANSI_RED + "Invalid command!" + Constants.ANSI_RESET);
                            }
                        }else{
                            // NON SONO LOGGATO!
                            out.println(Constants.ANSI_RED + "You can't perform this without login first!" + Constants.ANSI_RESET);
                        }
                    }
                    break;
                case "showCards":
                    if(data.length == 2){
                        if(logged){
                            userDb.readDb();
                            if(userDb.isMember(data[1],utente)){
                                // A questo punto posso mostrare le cards
                                Project p = new Project(data[1]);
                                p.readAllLists(Constants.progettiPath+data[1]);
                                String toSend = p.getCardList();
                                out.println(Constants.ANSI_GREEN +toSend+Constants.ANSI_RESET);
                            }else{
                                // NON AMMESSO!
                                out.println(Constants.ANSI_RED + "You don't have access to this project!" + Constants.ANSI_RESET);
                            }
                        }else{
                            // NON SONO LOGGATO!
                            out.println(Constants.ANSI_RED + "You can't perform this without login first!" + Constants.ANSI_RESET);
                        }
                    }
                    break;
                case "showCard":
                    if(data.length == 2){
                        if(logged){
                            userDb.readDb();
                            String[] cmds = data[1].split(" ",2);
                            if(userDb.isMember(cmds[0],utente)){
                                //Ora devo controllare che la card esista da qualche parte
                                Project p = new Project(cmds[0]);
                                p.readAllLists(Constants.progettiPath+cmds[0]);
                                if(p.doCardAlreadyExist(cmds[1])){
                                    // Se la card esiste allora posso accedere alle suee info
                                    String toSend = p.getCardInfo(cmds[1]);
                                    out.println(Constants.ANSI_GREEN +toSend+ Constants.ANSI_RESET);
                                }else{
                                    // NON ESISTE LA CARD!
                                    out.println(Constants.ANSI_RED + "Card "+cmds[1]+" does not exist!"+ Constants.ANSI_RESET);
                                }
                            }else{
                                // NON AMMESSO!
                                out.println(Constants.ANSI_RED + "You don't have access to this project!" + Constants.ANSI_RESET);
                            }
                        }else{
                            // NON SONO LOGGATO!
                            out.println(Constants.ANSI_RED + "You can't perform this without login first!" + Constants.ANSI_RESET);
                        }
                    }
                    break;
                case "addCard":
                    if(data.length == 2){
                        if(logged){
                            userDb.readDb();
                            String[] cmds = data[1].split(" ",3); //projName, cardName, description
                            if(cmds.length == 3){
                                if(userDb.isMember(cmds[0],utente)){
                                    //Devo controllare che la card non esista già
                                    Project p = new Project(cmds[0]);
                                    p.readTodoList(Constants.progettiPath+cmds[0]);
                                    if(!p.doCardAlreadyExist(cmds[1])){
                                        // a questo punto la card può esser creata e aggiunta
                                        Card c = new Card(cmds[1]);
                                        c.setCardDescription(cmds[2]);
                                        if(p.addTodoList(c)) {
                                            //scrivo i risultati
                                            p.writeTodoList(Constants.progettiPath + cmds[0]);
                                            //avviso l'esterno
                                            out.println(Constants.ANSI_GREEN + "Card " + cmds[1] + " added successfully!" + Constants.ANSI_RESET);
                                        }else{
                                            // ESISTE GIÀ
                                            out.println(Constants.ANSI_RED + "The card already exist! Choose another name." + Constants.ANSI_RESET);
                                        }
                                    }else{
                                        // ESISTE GIÀ
                                        out.println(Constants.ANSI_RED + "The card already exist! Choose another name." + Constants.ANSI_RESET);
                                    }
                                }else{
                                    // NON AMMESSO!
                                    out.println(Constants.ANSI_RED + "You don't have access to this project!" + Constants.ANSI_RESET);
                                }
                            }else{
                                out.println(Constants.ANSI_RED + "Invalid command!" + Constants.ANSI_RESET);
                            }
                        }else{
                            // NON SONO LOGGATO!
                            out.println(Constants.ANSI_RED + "You can't perform this without login first!" + Constants.ANSI_RESET);
                        }
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
                if(in != null) {
                    String rdln = in.readLine();
                    actionHandler(rdln);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
