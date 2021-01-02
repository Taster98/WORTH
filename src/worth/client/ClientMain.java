package worth.client;

import worth.Constants;
import worth.RegistrationInterface;
import worth.server.NotificaServer;

import java.io.*;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

//Client che gestisce le connessioni al server TCP.
public class ClientMain {
    //Socket per il collegamento con il server
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    //flag per la gestione di alcune operazioni in locale ammesse solamente dopo il login.
    public static boolean logged;

    public ClientMain() {
        logged = false;
    }

    //La connessione viene avviata da un comando a scelta del client, per cui viene creata solo se è null la prima volta.
    public void startConnection() throws IOException {
        if (clientSocket == null)
            clientSocket = new Socket(Constants.LOCALHOST_IP, Constants.TCP_PORT);
        if (out == null)
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        if (in == null)
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    //La connessione viene chiusa solamente quando si digita il comando 'exit', solo se era stata precedentemente avviata.
    public void stopConnection() throws IOException {
        if (in != null)
            in.close();
        if (out != null)
            out.close();
        if (clientSocket != null)
            clientSocket.close();
    }

    //Di seguito è definita una serie di metodi privati ausiliari per la gestione dell'output e dell'input al/dal server.

    //Metodo per l'invio al server dei dati del login
    private String loginUser(String nickname, String password) throws IOException {
        out.println("login " + nickname + " " + password);
        return in.readLine();
    }

    //Metodo per l'invio al server dei dati del logout
    private String logoutUser(String nickname) throws IOException {
        out.println("logout " + nickname);
        return in.readLine();
    }

    //Metodo per la richiesta di lettura del file della lista di utenti (tutti o solo online)
    //Questo metodo non invia nulla al server poichè è locale al client.
    private String listUsers(String str) throws IOException {
        File myFile = new File(Constants.userListPath);
        Scanner sc = new Scanner(myFile);
        String output = "";
        if (str == null) {
            //Leggo tutti gli utenti
            while (sc.hasNextLine()) {
                String data = sc.nextLine();
                output += data + "\n";
            }
        } else {
            //Leggo solo gli utenti online
            while (sc.hasNextLine()) {
                String data = sc.nextLine();
                if (data.contains("online"))
                    output += data + "\n";
            }
        }
        sc.close();
        return output;
    }
    private void updateUsers() throws IOException{
        out.println("update");
    }
    //Metodo per l'invio al server dei dati della creazione del progetto
    private String createProject(String projectName) throws IOException {
        out.println("createProject " + projectName);
        return in.readLine();
    }

    //Metodo per l'invio al server dei dati per la ricezione della lista dei progetti
    private String listProjects() throws IOException {
        out.println("listProjects");
        return in.readLine();
    }

    //Metodo per l'invio al server dei dati per la ricezione dei membri di un progetto
    private String showMembers(String projectName) throws IOException {
        out.println("showMembers " + projectName);
        return in.readLine();
    }

    //Metodo per l'invio al server dei dati per l'aggiunta di un utente a un progetto
    private String addMember(String projectName, String nickUser) throws IOException {
        out.println("addMember " + projectName + " " + nickUser);
        return in.readLine();
    }

    //Metodo per l'invio al server dei dati per la ricezione della lista delle cards associate ad un progetto
    private String showCards(String projName) throws IOException {
        out.println("showCards " + projName);
        return in.readLine();
    }

    //Metodo per l'invio al server dei dati per la ricezione delle informazioni di una card associata a un progetto
    private String showCard(String projName, String cardName) throws IOException {
        out.println("showCard " + projName + " " + cardName);
        return in.readLine();
    }

    //Metodo per l'invio al server dei dati per l'aggiunta di una card al progetto, con la sua descrizione.
    private String addCard(String projectName, String cardName, String description) throws IOException {
        out.println("addCard " + projectName + " " + cardName + " " + description);
        return in.readLine();
    }

    //Metodo per l'invio al server dei dati per lo spostamento di una card di un progetto da una lista a un'altra
    private String moveCard(String projName, String cardName, String srcList, String destList) throws IOException {
        out.println("moveCard " + projName + " " + cardName + " " + srcList + " " + destList);
        return in.readLine();
    }

    //Metodo per l'invio al server dei dati per la ricezione della history di una card di un progetto
    private String getHistory(String projName, String cardName) throws IOException {
        out.println("getHistory " + projName + " " + cardName);
        return in.readLine();
    }

    //Metodo per l'invio al server dei dati per la cancellazione di un progetto
    private String cancelProject(String projName) throws IOException {
        out.println("cancelProject " + projName);
        return in.readLine();
    }

    //Metodo per l'invio al server dei dati per leggere gli aggiornamenti/messaggi udp.
    //Anche se i messaggi e gli aggiornamenti sono gestiti da una connessione udp, devo comunque mandare al
    //server i dati per far sì che esso aggiunga questo client alla lista di riceventi dei messaggi.
    private String readChat(String projName) throws IOException {
        out.println("readChat " + projName);
        return in.readLine();
    }

    //Metodo per l'invio al server dei dati per l'invio di messaggi. Come sopra, è il server tcp che delega il server
    //udp per svolgere le azioni.
    private String sendMessage(String projName, String message) throws IOException {
        out.println("sendMessage " + projName + " " + message);
        return in.readLine();
    }

    //Metodi ausiliari per avviare/fermare i thread relativi al server udp dei singoli progetti
    Thread chat;

    private void avviaChat(String ip) {
        UDPThread uth = new UDPThread(ip);
        chat = new Thread(uth);
        chat.start();
    }

    private void fermaChat() {
        if (chat != null)
            chat.interrupt();
    }


    /*
     @REQUIRE: il nome del progetto non deve avere spazi
     @REQUIRE: l'input non deve contenere il carattere speciale £, e i parametri dei comandi (ad eccezione di message, e cardDescription)
     NON ammettono spazi.
     @REQUIRE: i progetti possono essere al massimo 255.
     */
    //Metodo ausiliario per l'interpretazione dei comandi ricevuti dall'utente
    public void commandInterpreter(String cmd) throws IOException, NotBoundException {
        //Se un utente premesse invio senza nulla o con un solo spazio, non faccio nulla
        if (cmd == null) return;
        if (cmd.equals("")) return;

        //In exec, alla prima posizione, ci sarà sempre il comando usato.
        //Nel caso di comandi con una sola keyword, l'array exec avrà dimensione 1.
        String[] exec = cmd.split(" ", 2);
        //Array di appoggio per la registrazione
        String[] data;
        //La callback la registro sulla porta 5000 sul localhost, per impedire di andare in conflitto con altre porte usate
        //per la connessione tcp, udp e l'altra RMI di registrazione.
        Registry registryCB = LocateRegistry.getRegistry(5000);
        String name = "Server";
        NotificaServer server = (NotificaServer) registryCB.lookup(name);
        NotificaClient callbackObj = new NotificaImpl();
        NotificaClient stubCB = (NotificaClient) UnicastRemoteObject.exportObject(callbackObj, 0);
        //Il pattern dei comandi è sempre il solito: Controllo la lunghezza dell'array, se occorre controllo
        //se si è loggati. Al resto degli errori ci pensa il server tcp.
        //Il comando non è case sensitive
        switch (exec[0]) {
            case "help":
                if (exec.length == 1) {
                    System.out.println(Constants.ANSI_YELLOW + Constants.HELP + Constants.ANSI_RESET);
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'help'" + Constants.ANSI_RESET);
                }
                break;
            case "version":
                if (exec.length == 1) {
                    System.out.println(Constants.VERSION);
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'version'" + Constants.ANSI_RESET);
                }
                break;
            case "register":
                if (exec.length == 2) {
                    if (!logged) {
                        data = exec[1].split(" ", 2);
                        if (data.length == 2) {
                            try {
                                //La registrazione avviene tramite RMI
                                Registry registry = LocateRegistry.getRegistry(30001);
                                RegistrationInterface stub = (RegistrationInterface) registry.lookup("RegistrationInterface");
                                int response = stub.register(data[0], data[1]);
                                if (response == 7)
                                    System.out.println(Constants.ANSI_GREEN + "Registration done!\nWelcome " + data[0] + ". You should login now." + Constants.ANSI_RESET);
                                else
                                    System.out.println(Constants.ANSI_RED + "Nickname " + data[0] + " already exist. Try another one." + Constants.ANSI_RESET);
                            } catch (Exception e) {
                                System.err.println(Constants.ANSI_RED + "Client exception: " + e.toString() + Constants.ANSI_RESET);
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'register [nickname] [password]'" + Constants.ANSI_RESET);
                        }
                    } else {
                        System.out.println(Constants.ANSI_RED + "You must logout in order to register a new account!" + Constants.ANSI_RESET);
                    }
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'register [nickname] [password]'" + Constants.ANSI_RESET);
                }
                break;
            case "login":
                if (exec.length == 2) {
                    data = exec[1].split(" ", 2); // "user pass" -> {"user", "pass"}
                    if (data.length == 2) {
                        try {
                            this.startConnection();
                            String res = loginUser(data[0], data[1]);
                            System.out.println(res);
                            if (res.contains("Login success!")) {
                                logged = true;
                                server.register(stubCB, data[0]); // mi registro alla callback
                                updateUsers();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'login [nickname] [password]'" + Constants.ANSI_RESET);
                    }
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'login [nickname] [password]'" + Constants.ANSI_RESET);
                }
                break;
            case "clear":
                if (exec.length == 1) {
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'clear'" + Constants.ANSI_RESET);
                }
                break;
            case "logout":
                if (exec.length == 2) {
                    String nick = exec[1];
                    try {
                        this.startConnection();
                        String res = logoutUser(nick);
                        System.out.println(res);
                        if (res.contains("Logout successful!")) {
                            logged = false;
                            server.unregister(stubCB, nick); //mi deregistro dalla callback
                            fermaChat(); //fermo la chat, poiché a questo punto non ha senso ricevere altri aggiornamenti.
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'logout [nickname]'" + Constants.ANSI_RESET);
                }
                break;
            case "users":
                if (exec.length == 1) {
                    if (logged) {
                        try {
                            String toPrint = listUsers(null);
                            System.out.println(Constants.ANSI_GREEN + toPrint + Constants.ANSI_RESET);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println(Constants.ANSI_RED + "You must login to perform this action!" + Constants.ANSI_RESET);
                    }
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'users'" + Constants.ANSI_RESET);
                }
                break;
            case "online":
                if (exec.length == 1) {
                    if (logged) {
                        try {
                            String toPrint = listUsers("online");
                            System.out.println(Constants.ANSI_GREEN + toPrint + Constants.ANSI_RESET);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println(Constants.ANSI_RED + "You must login to perform this action!" + Constants.ANSI_RESET);
                    }
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'online'" + Constants.ANSI_RESET);
                }
                break;
            case "createproject":
                if (exec.length == 2) {
                    String projectName = exec[1];
                    //il nome del progetto non può contenere spazi
                    if (!projectName.equals("") && !projectName.contains(" ")) {
                        try {
                            this.startConnection();
                            String toPrint = createProject(projectName);
                            System.out.println(toPrint);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println(Constants.ANSI_RED + "Wrong projectName. Try to use a projectName without spaces." + Constants.ANSI_RESET);
                    }
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'createProject [projectName]'" + Constants.ANSI_RESET);
                }
                break;
            case "listprojects":
                if (exec.length == 1) {
                    this.startConnection();
                    String toPrint = listProjects();
                    if (!toPrint.contains(Constants.ANSI_RED))
                        System.out.println(Constants.ANSI_GREEN + "Your project list:");
                    System.out.println(toPrint.replace("£", "\n") + Constants.ANSI_RESET);
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'listProjects'" + Constants.ANSI_RESET);
                }
                break;
            case "showmembers":
                if (exec.length == 2) {
                    this.startConnection();
                    String toPrint = showMembers(exec[1]);
                    if (!toPrint.contains(Constants.ANSI_RED))
                        System.out.println(Constants.ANSI_GREEN + "Members of " + exec[1] + " project:");
                    System.out.println(toPrint.replace("£", "\n") + Constants.ANSI_RESET);
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'showMembers [projectName]'" + Constants.ANSI_RESET);
                }
                break;
            case "addmember":
                if (exec.length == 2) {
                    String[] cmds = exec[1].split(" ", 2);
                    if (cmds.length == 2) {
                        this.startConnection();
                        String toPrint = addMember(cmds[0], cmds[1]);
                        System.out.println(toPrint);
                    } else {
                        System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'addMember [projectName] [nickUser]'" + Constants.ANSI_RESET);
                    }
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'addMember [projectName] [nickUser]'" + Constants.ANSI_RESET);
                }
                break;
            case "showcards":
                if (exec.length == 2) {
                    this.startConnection();
                    String toPrint = showCards(exec[1]);
                    System.out.println(toPrint.replace("£", "\n"));
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'showCards [projectName]'" + Constants.ANSI_RESET);
                }
                break;
            case "showcard":
                if (exec.length == 2) {
                    String[] cmds = exec[1].split(" ", 2);
                    if (cmds.length == 2) {
                        this.startConnection();
                        String toPrint = showCard(cmds[0], cmds[1]);
                        System.out.println(toPrint.replace("£", "\n"));
                    } else {
                        System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'showCard [projectName] [cardName]'" + Constants.ANSI_RESET);
                    }
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'showCard [projectName] [cardName]'" + Constants.ANSI_RESET);
                }
                break;
            case "addcard":
                if (exec.length == 2) {
                    String[] cmds = exec[1].split(" ", 3);
                    if (cmds.length == 3) {
                        this.startConnection();
                        String toPrint = addCard(cmds[0], cmds[1], cmds[2]);
                        System.out.println(toPrint);
                    } else {
                        System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'addCard [projectName] [cardName] [description]'" + Constants.ANSI_RESET);
                    }
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'addCard [projectName] [cardName] [description]'" + Constants.ANSI_RESET);
                }
                break;
            case "movecard":
                if (exec.length == 2) {
                    String[] cmds = exec[1].split(" ", 4);
                    if (cmds.length == 4) {
                        this.startConnection();
                        String toPrint = moveCard(cmds[0], cmds[1], cmds[2], cmds[3]);
                        System.out.println(toPrint);
                    } else {
                        System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'moveCard [projectName] [cardName] {todoList, progressList, revisedList, doneList} {todoList, progressList, revisedList, doneList}'" + Constants.ANSI_RESET);
                    }
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'moveCard [projectName] [cardName] {todoList, progressList, revisedList, doneList} {todoList, progressList, revisedList, doneList}'" + Constants.ANSI_RESET);
                }
                break;
            case "gethistory":
                if (exec.length == 2) {
                    String[] cmds = exec[1].split(" ", 2);
                    if (cmds.length == 2) {
                        this.startConnection();
                        String toPrint = getHistory(cmds[0], cmds[1]);
                        if (!toPrint.contains(Constants.ANSI_RED))
                            System.out.println("History of " + cmds[1] + ":");
                        System.out.println(toPrint.replace("£", "\n"));
                    } else {
                        System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'getHistory [projectName] [cardName]'" + Constants.ANSI_RESET);
                    }
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'getHistory [projectName] [cardName]'" + Constants.ANSI_RESET);
                }
                break;
            case "cancelproject":
                if (exec.length == 2) {
                    this.startConnection();
                    String toPrint = cancelProject(exec[1]);
                    System.out.println(toPrint);
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'cancelProject [projectName]'" + Constants.ANSI_RESET);
                }
                break;
            case "readchat":
                if (exec.length == 2) {
                    if (logged) {
                        this.startConnection();
                        String toPrint = readChat(exec[1]);
                        if (toPrint.contains("yes"))
                            avviaChat(toPrint.substring(3));
                        else
                            System.out.println(toPrint);
                    } else {
                        System.out.println(Constants.ANSI_RED + "You can't read messages without login first!'" + Constants.ANSI_RESET);
                    }
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'readChat [projectName]'" + Constants.ANSI_RESET);
                }
                break;
            case "sendmsg":
                if (exec.length == 2) {
                    if (logged) {
                        String[] cmds = exec[1].split(" ", 2);
                        if (cmds.length == 2) {
                            this.startConnection();
                            String toPrint = sendMessage(cmds[0], cmds[1]);
                            if (!toPrint.equals("")) {
                                System.out.println(toPrint);
                            }
                        } else {
                            System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'sendMsg [projectName] [message]'" + Constants.ANSI_RESET);
                        }
                    } else {
                        System.out.println(Constants.ANSI_RED + "You must login in order to send messages." + Constants.ANSI_RESET);
                    }
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'sendMsg [projectName] [message]'" + Constants.ANSI_RESET);
                }
                break;
            case "exit":
                if (exec.length == 1) {
                    this.stopConnection();
                    System.exit(0);
                }
                break;
            default:
                System.out.println(Constants.ANSI_PURPLE + "'" + cmd + "'" + Constants.ANSI_RESET + Constants.ANSI_RED + " is not a valid command. Type 'help' to see the usage." + Constants.ANSI_RESET);
        }
    }

    public static void main(String[] args) {
        System.out.println(Constants.ANSI_GREEN + "Welcome to WORTH. To login, just type 'login username password'.\nif you need help on what you can do, just type 'help'." + Constants.ANSI_RESET);
        String inputText = "";
        ClientMain client = new ClientMain();
        //Ascolto input fintanto che non ricevo exit.
        while (!inputText.equals("exit")) {
            System.out.print(Constants.ANSI_GREEN + "> " + Constants.ANSI_RESET);
            Scanner sc = new Scanner(System.in);
            inputText = sc.nextLine();
            //Visto che il comando non è case sensitive, lo converto sempre in lower case.
            String[] broken = inputText.split(" ", 2);
            if (broken.length == 2)
                inputText = broken[0].toLowerCase() + " " + broken[1];
            else
                inputText = broken[0].toLowerCase();
            try {
                //Questo carattere speciale non è ammesso.
                if (inputText.contains("£")) {
                    System.out.println("Input must not contain special character '£'.");
                } else {
                    client.commandInterpreter(inputText);
                }
            } catch (NotBoundException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
