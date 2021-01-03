package worth.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import worth.Constants;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientHandler implements Runnable{
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    // riferimento al database
    private DatabaseUsers userDb;
    // riferimento alla lista progetti
    private CopyOnWriteArrayList<String> projects;
    //riferimento alla lista progetti immutabile
    private CopyOnWriteArrayList<String> auxProj;
    // riferimento alla lista di ip
    private CopyOnWriteArrayList<String> ipAddresses;
    // variabile di controllo per far sì che un client si connetta solo con un user alla volta
    private boolean logged;
    // utente collegato a questo thread
    private User utente;
    // server per il callback
    private ServerNotificaImpl serverCB;
    public ClientHandler(Socket socket, ServerNotificaImpl serverCB){
        this.logged = false;
        this.clientSocket = socket;
        this.userDb = new DatabaseUsers();
        userDb.readDb();
        utente = new User();
        this.serverCB = serverCB;
        this.projects = new CopyOnWriteArrayList<>();
        this.ipAddresses = new CopyOnWriteArrayList<>();
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
            //leggo anche la lista immutabile
            br = new BufferedReader(new FileReader(Constants.auxProjectList));
            auxProj = gson.fromJson(br,type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    //funzione che legge dal file la lista dei progetti
    public synchronized void writeProjects() {
        Writer writer;
        try {
            writer = new FileWriter(Constants.fileProgettiPath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(projects, writer);
            writer.flush();
            writer.close();
            //Scrivo anche nella lista immutabile
            writer = new FileWriter(Constants.auxProjectList);
            gson.toJson(auxProj, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //funzione che legge gli indirizzi ip dal file
    private synchronized void readIp(){
        Gson gson = new Gson();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(Constants.ipAddressPath));
            Type type = new TypeToken<CopyOnWriteArrayList<String>>() {
            }.getType();
            ipAddresses = gson.fromJson(br, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    //funzione che scrive gli indirizzi ip nel file
    public synchronized void writeIp(){
        Writer writer;
        try {
            writer = new FileWriter(Constants.ipAddressPath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(ipAddresses, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //funzione che aggiunge l'ip relativo in lista
    //Suppongo di utilizzare gli indirizzi dal 224.0.0.0 al 239.255.255.255
    public synchronized void addIp(int offset) {
        if(ipAddresses == null)
            ipAddresses = new CopyOnWriteArrayList<>();
        //A seconda di offset, faccio questo:
        String ip = Constants.CHAT_IP_BASE;
        if(offset<256){
            ip = Constants.CHAT_IP_BASE.substring(0,8)+offset;
        }else if(offset<65536){
            //224.0.0.0
            int third = (int)offset/256;
            int fourth = offset-third*256;
            ip = Constants.CHAT_IP_BASE.substring(0,6)+third+"."+fourth;
        }else if(offset<16777216){
            //15000000  224.228.225.192
            //70000 224.1.17.112
            //100000 224.1.134.160
            //16777215 224.255.255.255
            int second = (int)offset/65536;
            int third = offset - second*65536;
            third = third/256;
            int fourth = offset -second*65536-third*256;
            ip = Constants.CHAT_IP_BASE.substring(0,4)+second+"."+third+"."+fourth;
        }else if(offset<268435456){
            //268435455 239.255.255.255
            int first = (int)offset/16777216;
            int realfirst = first+224;
            int second = offset - first*16777216;
            second = second/65536;
            int third = offset - first*16777216 - second*65536;
            third = third/256;
            int fourth = offset - first*16777216 - second*65536 - third*256;
            ip = realfirst + "."+second+"."+third+"."+fourth;
        }
        ipAddresses.addIfAbsent(ip);
    }

    //funzione che aggiunge un progetto in lista
    public synchronized boolean addProject(String prj) {
        if(projects == null)
            projects = new CopyOnWriteArrayList<>();
        if(auxProj == null)
            auxProj = new CopyOnWriteArrayList<>();
        auxProj.add(prj);
        return projects.addIfAbsent(prj);
    }
    //funzione che rimuove un progetto
    public synchronized void removeProject(String prj) {
        if(projects == null)
            projects = new CopyOnWriteArrayList<>();
        projects.remove(prj);
    }

    /*Metodo ausiliario che gestisce le azioni da fare a seconda del comando ricevuto
    @REQUIRE: Un utente viene automaticamente settato come offline all'uscita del programma.
    Pertanto, al riavvio deve fare nuovamente il login
    */
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
                    // Il comando deve avere 2 argomenti: se ne ha uno solo, stampo wrong usage
                    if(data.length == 2) {
                        // Se c'è già un utente loggato non devo fare nulla
                        if (!logged) {
                            userDb.readDb();
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
                                    } else {
                                        // Se sono qui sono loggato correttamente
                                        // Devo aggiornare il database
                                        userDb.setStatus(utente, "online");
                                        userDb.writeDb();
                                        serverCB.update(userDb.getListStatus());
                                        // Setto il flag logged
                                        logged = true;
                                        out.println(Constants.ANSI_GREEN + "Login success! Welcome back " + utente.getNickName() + Constants.ANSI_RESET);
                                    }

                                } else {
                                    // PASSWORD ERRATA
                                    out.println(Constants.ANSI_RED + "Password is wrong!" + Constants.ANSI_RESET);
                                }
                            } else {
                                // UTENTE NON ESISTE
                                out.println(Constants.ANSI_RED + "This username doesn't exist!" + Constants.ANSI_RESET);
                            }
                        } else {
                            // SONO GIÀ COLLEGATO CON UN ACCOUNT
                            out.println(Constants.ANSI_RED + "You must logout from current account in order to login with another one!" + Constants.ANSI_RESET);
                        }
                    }else{
                        // WRONG USAGE
                        out.println(Constants.ANSI_RED + "Wrong usage: try with login [nickname] [password]"+ Constants.ANSI_RESET);
                    }
                    break;
                case "update":
                    userDb.readDb();
                    serverCB.update(userDb.getListStatus());
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
                                }
                            }else{
                                // NICKNAME NON GIUSTO!
                                out.println(Constants.ANSI_RED + "Invalid nickname!" + Constants.ANSI_RESET);
                            }
                        } else {
                            // NON SONO LOGGATO!
                            out.println(Constants.ANSI_RED + "You can't logout without login first!" + Constants.ANSI_RESET);
                        }
                    }else{
                        // WRONG USAGE
                        out.println(Constants.ANSI_RED + "Wrong usage: try with logout [nickname]"+ Constants.ANSI_RESET);
                    }
                    break;
                case "logexit": //prima di chiudere il client devo fare logout
                    if(logged){
                        userDb.readDb();
                        userDb.setStatus(utente,"offline");
                        logged = false;
                        userDb.writeDb();
                        out.println(utente.getNickName());
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
                                        //Aggiungo prima l'ip nella lista
                                        int offset = auxProj.size();
                                        readIp();
                                        addIp(offset); //QUESTO VALE SOLO PER 255 IP DIVERSI OVVIAMENTE
                                        writeIp();
                                        //Scrivo anche l'utente creatore nella lista utenti
                                        if(p.writeUserList(dir)){
                                            utente.setStato("online");
                                            if(userDb.addProject(utente,data[1])){
                                                userDb.readDb();
                                                out.println(Constants.ANSI_GREEN +"Project '"+data[1]+"' created successfully!" + Constants.ANSI_RESET);
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
                                    //DEVO CONTROLLARE CHE L'UTENTE ESISTA
                                    if(userDb.cercaUtente(usr)) {
                                        if (userDb.addMemberToList(cmds[0], usr)) {
                                            // MEMBER ADDED
                                            out.println(Constants.ANSI_GREEN + usr.getNickName() + " added successfully!" + Constants.ANSI_RESET);
                                            readProjects();
                                            readIp();
                                            UDPServer.sendMessage(utente.getNickName() + " added " + usr.getNickName() + " to " + cmds[0], ipAddresses.get(auxProj.indexOf(cmds[0])), Constants.UDP_PORT);
                                        } else {
                                            // ALREADY IN
                                            out.println(Constants.ANSI_RED + "The user is already inside this project!" + Constants.ANSI_RESET);
                                        }
                                    }else{
                                        //NON ESISTE
                                        out.println(Constants.ANSI_RED + "The user does not exist" + Constants.ANSI_RESET);
                                    }
                                } else {
                                    // NON AMMESSO!
                                    out.println(Constants.ANSI_RED + "You don't have access to this project!" + Constants.ANSI_RESET);
                                }
                            }else{
                                // WRONG USAGE
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
                                            readProjects();
                                            readIp();
                                            out.println(Constants.ANSI_GREEN + "Card " + cmds[1] + " added successfully!" + Constants.ANSI_RESET);
                                            UDPServer.sendMessage(utente.getNickName()+" added a new card "+cmds[1]+" to "+cmds[0],ipAddresses.get(auxProj.lastIndexOf(cmds[0])),Constants.UDP_PORT);
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
                case "moveCard":
                    if(data.length == 2){
                        if(logged){
                            userDb.readDb();
                            String[] cmds = data[1].split(" ",4); //projName, cardName, src, dest
                            if(cmds.length == 4){
                                //Controllo che sia membro
                                if(userDb.isMember(cmds[0],utente)){
                                    Project p = new Project(cmds[0]);
                                    p.readAllLists(Constants.progettiPath+cmds[0]);
                                    int res;
                                    if((res = p.moveCard(cmds[1],cmds[2],cmds[3])) == 7){
                                        //Se sono qui sono riuscito, scrivo tutto
                                        //Stampo risultato e invio il messaggio con il server UDP
                                        readProjects();
                                        readIp();
                                        out.println(Constants.ANSI_GREEN + "Card " + cmds[1] + " moved successfully!" + Constants.ANSI_RESET);
                                        UDPServer.sendMessage(utente.getNickName()+" moved card "+cmds[1]+" from "+cmds[2]+" to "+cmds[3]+" in project "+cmds[0],ipAddresses.get(auxProj.lastIndexOf(cmds[0])),Constants.UDP_PORT);
                                    }else{
                                        if(res == -1){
                                            out.println(Constants.ANSI_RED + "Movement not reached! Try again checking carefully lists! Error "+res + Constants.ANSI_RESET);
                                        }else if(res == 0){
                                            out.println(Constants.ANSI_RED + "Movement not reached! Try again checking carefully lists! Error "+res + Constants.ANSI_RESET);
                                        }
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
                case "getHistory":
                    if(data.length == 2){
                        if(logged){
                            userDb.readDb();
                            String[] cmds = data[1].split(" ",2); //projName, cardName
                            if(cmds.length == 2){
                                //Controllo che sia membro
                                if(userDb.isMember(cmds[0],utente)){
                                    Project p = new Project(cmds[0]);
                                    p.readAllLists(Constants.progettiPath+cmds[0]);
                                    //Controllo che la card esista
                                    String output;
                                    if((output = p.getCardHistory(cmds[1])) != null){
                                        //Altrimenti stampo la storia
                                        out.println(Constants.ANSI_GREEN + output +Constants.ANSI_RESET);
                                    }else{
                                        // NON ESISTE LA CARD!
                                        out.println(Constants.ANSI_RED + "Card "+cmds[1]+" does not exist!"+ Constants.ANSI_RESET);
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
                case "readChat":
                    if(data.length == 2){
                        if(logged){
                            userDb.readDb();
                            if(userDb.isMember(data[1],utente)){
                                readProjects();
                                readIp();
                                out.println("yes"+ipAddresses.get(auxProj.lastIndexOf(data[1])));
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
                case "sendMessage":
                    if(data.length == 2){
                        if(logged){
                            userDb.readDb();
                            String[] cmds = data[1].split(" ",2); //projName, message
                            if(cmds.length == 2){
                                if(userDb.isMember(cmds[0],utente)){
                                    //qui sono loggato, appartengo al progetto e posso finalmente inviare il messaggio
                                    readProjects();
                                    readIp();
                                    UDPServer.sendMessage(Constants.ANSI_YELLOW+utente.getNickName()+" sent: "+cmds[1]+Constants.ANSI_RESET,ipAddresses.get(auxProj.lastIndexOf(cmds[0])),Constants.UDP_PORT);
                                    out.println("");
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
                case "cancelProject":
                    if(data.length == 2){
                        if(logged){
                            userDb.readDb();
                            if(userDb.isMember(data[1],utente)){
                                Project p = new Project(data[1]);
                                p.readAllLists(Constants.progettiPath+data[1]);
                                readProjects();
                                //readIp();
                                //Se tutte le card sono done allora sì, sennò no
                                //Se doneList è null e l'altra no, non posso sicuro cancellare
                                if((p.getDoneCards() == null && p.getAllCards() != null) || (p.getAllCards() == null && p.getDoneCards() != null)){
                                    // NON SI CANCELLA SENZA FARE!
                                    out.println(Constants.ANSI_RED + "You can't cancel this project until all its cards are not in doneList!" + Constants.ANSI_RESET);
                                }else{
                                    if((p.getAllCards() == null && p.getDoneCards() == null) || (p.getDoneCards().isEmpty() && p.getAllCards().isEmpty()) || p.getAllCards().size() == p.getDoneCards().size() && p.getAllCards().equals(p.getDoneCards())){
                                        //rimuovo da ogni utente il riferimento al progetto
                                        userDb.removeProject(utente,data[1]);
                                        userDb.writeDb();
                                        //Rimuovo il progetto
                                        removeProject(data[1]);
                                        writeProjects();
                                        //writeIp();
                                        //rimuovo la directory
                                        p.removeDir(new File(Constants.progettiPath+"/"+data[1]));
                                        out.println(Constants.ANSI_GREEN + "Project removed successfully!" +Constants.ANSI_RESET);
                                    }else{
                                        // NON SI CANCELLA SENZA FARE!
                                        out.println(Constants.ANSI_RED + "You can't cancel this project until all its cards are not in doneList!" + Constants.ANSI_RESET);
                                    }
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
                    if(rdln != null && !rdln.contains("£")) {
                        actionHandler(rdln);
                    }else if(rdln != null){
                        System.out.println("Input must not contain special character '£'.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
