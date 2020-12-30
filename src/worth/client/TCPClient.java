package worth.client;

import worth.Constants;
import worth.RegistrationInterface;
import worth.server.NotificaServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class TCPClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean logged;

    public TCPClient() {
        this.logged = false;
    }

    public void startConnection() throws IOException {
        if (clientSocket == null)
            clientSocket = new Socket(Constants.LOCALHOST_IP, Constants.TCP_PORT);
        if (out == null)
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        if (in == null)
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void stopConnection() throws IOException {
        if (in != null)
            in.close();
        if (out != null)
            out.close();
        if (clientSocket != null)
            clientSocket.close();
    }

    public String loginUser(String nickname, String password) throws IOException {
        out.println("login " + nickname + " " + password);
        return in.readLine();
    }

    public String logoutUser(String nickname) throws IOException {
        out.println("logout " + nickname);
        return in.readLine();
    }

    private String listUsers(String str) throws IOException {
        if (str == null) {
            out.println("users");
        } else {
            out.println("online");
        }
        return in.readLine();
    }

    private String createProject(String projectName) throws IOException {
        out.println("createProject "+projectName);
        return in.readLine();
    }

    private String listProjects() throws IOException{
        out.println("listProjects");
        return in.readLine();
    }

    private String showMembers(String projectName) throws IOException{
        out.println("showMembers "+projectName);
        return in.readLine();
    }

    private String addMember(String projectName, String nickUser) throws IOException{
        out.println("addMember "+projectName+" "+nickUser);
        return in.readLine();
    }
    /*
     @REQUIRE: la registrazione va fatta prima di ogni cosa; se si prova a registrarsi quando si è loggati, si viene automaticamente sloggati dall
     account corrente
     @REQUIRE: il nome del progetto non deve avere spazi
     */
    public void commandInterpreter(String cmd) throws IOException, NotBoundException {
        if (cmd == null) return;
        if (cmd.equals("")) return;
        String[] exec = cmd.split(" ", 2); // "login user pass"  {"login", "user pass"}
        // Inizio a interpretare il comando
        // in data[0] c'è il nickname, in data[1] c'è password
        // Specifico che l'host deve essere il localhost
        String[] data;
        // Roba per la callback
        // Gestisco callback
        Registry registryCB = LocateRegistry.getRegistry(5000);
        String name = "Server";
        NotificaServer server = (NotificaServer) registryCB.lookup(name);
        NotificaClient callbackObj = new NotificaImpl();
        //NotificaImpl callbackObj = new NotificaImpl();
        NotificaClient stubCB = (NotificaClient) UnicastRemoteObject.exportObject(callbackObj, 0);
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
                                server.register(stubCB, data[0]);
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
                            server.unregister(stubCB, nick);
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
                    try {
                        this.startConnection();
                        String toPrint = listUsers(null);
                        toPrint = Constants.ANSI_GREEN + "Lista utenti aggiornata:\n" + toPrint.replace("?", "\n") + Constants.ANSI_RESET;
                        System.out.println(toPrint);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'users'" + Constants.ANSI_RESET);
                }
                break;
            case "online":
                if (exec.length == 1) {
                    try {
                        this.startConnection();
                        String toPrint = listUsers("online");
                        toPrint = Constants.ANSI_GREEN + "Lista utenti online aggiornata:\n" + toPrint.replace("?", "\n") + Constants.ANSI_RESET;
                        System.out.println(toPrint);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'online'" + Constants.ANSI_RESET);
                }
                break;
            case "createProject":
                if(exec.length == 2){
                    String projectName = exec[1];
                    if(!projectName.equals("") && !projectName.contains(" ")) {
                        try {
                            this.startConnection();
                            String toPrint = createProject(projectName);
                            System.out.println(toPrint);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else{
                        System.out.println(Constants.ANSI_RED + "Wrong projectName. Try to use a projectName without spaces." + Constants.ANSI_RESET);
                    }
                }else{
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'createProject [projectName]'" + Constants.ANSI_RESET);
                }
                break;
            case "listProjects":
                if(exec.length == 1){
                    this.startConnection();
                    String toPrint = listProjects();
                    toPrint = Constants.ANSI_GREEN + "Your project list:\n" + toPrint.replace("?","\n")+Constants.ANSI_RESET;
                    System.out.println(toPrint);
                }else{
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'listProjects'" + Constants.ANSI_RESET);
                }
                break;
            case "showMembers":
                if(exec.length == 2){
                    this.startConnection();
                    String toPrint = showMembers(exec[1]);
                    toPrint = Constants.ANSI_GREEN + "Members of "+exec[1]+" project:\n"+toPrint.replace("?","\n")+Constants.ANSI_RESET;
                    System.out.println(toPrint);
                }else{
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'showMembers [projectName]'" + Constants.ANSI_RESET);
                }
                break;
            case "addMember":
                if(exec.length == 2){
                    String[] cmds = exec[1].split(" ",2);
                    if(cmds.length == 2) {
                        this.startConnection();
                        String toPrint = addMember(cmds[0], cmds[1]);
                        System.out.println(toPrint);
                    }else{
                        System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'addMember [projectName] [nickUser]'" + Constants.ANSI_RESET);
                    }
                }else{
                    System.out.println(Constants.ANSI_RED + "Wrong usage. Type 'addMember [projectName] [nickUser]'" + Constants.ANSI_RESET);
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
        TCPClient client = new TCPClient();
        while (!inputText.equals("exit")) {
            System.out.print(Constants.ANSI_GREEN + "> " + Constants.ANSI_RESET);
            Scanner sc = new Scanner(System.in);
            inputText = sc.nextLine();
            try {
                client.commandInterpreter(inputText);
            } catch (NotBoundException | IOException e) {
                e.printStackTrace();
            }
        }

    }
}
