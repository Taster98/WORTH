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

    public TCPClient() {}

    public void startConnection() throws IOException {
        if (clientSocket == null)
            clientSocket = new Socket(Constants.LOCALHOST_IP, Constants.TCP_PORT);
        if (out == null)
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        if (in == null)
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public String loginUser(String nickname, String password) throws IOException {
        out.println("login " + nickname + " " + password);
        return in.readLine();
    }

    public String logoutUser() throws IOException {
        out.println("logout");
        return in.readLine();
    }

    private String listUsers(String str) throws IOException {
        if(str == null){
            out.println("users");
        }else{
            out.println("online");
        }
        return in.readLine();
    }

    /* @REQUIRE: la registrazione va fatta prima di ogni cosa; se si prova a registrarsi quando si è loggati, si viene automaticamente sloggati dall
    account corrente */
    public void commandInterpreter(String cmd) {
        if (cmd == null) throw new NullPointerException();
        if (cmd.equals("")) throw new IllegalArgumentException();
        String[] exec = cmd.split(" ", 2);
        // Inizio a interpretare il comando
        // in data[0] c'è il nickname, in data[1] c'è password
        // Specifico che l'host deve essere il localhost
        String[] data;
        switch (exec[0]) {
            case "help":
                System.out.println(Constants.ANSI_YELLOW +Constants.HELP+ Constants.ANSI_RESET);
                break;
            case "register":
                try {
                    this.startConnection();
                    logoutUser();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                data = exec[1].split(" ", 2);
                try {
                    Registry registry = LocateRegistry.getRegistry(30001);
                    RegistrationInterface stub = (RegistrationInterface) registry.lookup("RegistrationInterface");
                    int response = stub.register(data[0], data[1]);
                    if (response == 7)
                        System.out.println(Constants.ANSI_GREEN +"Registration done!\nWelcome " + data[0] + ". You should login now."+Constants.ANSI_RESET);
                    else
                        System.out.println(Constants.ANSI_RED +"Nickname " + data[0] + " already exist. Try another one." + Constants.ANSI_RESET);
                } catch (Exception e) {
                    System.err.println(Constants.ANSI_RED +"Client exception: " + e.toString() + Constants.ANSI_RESET);
                    e.printStackTrace();
                }
                break;
            case "login":
                data = exec[1].split(" ", 2);
                try {
                    this.startConnection();
                    String res = loginUser(data[0], data[1]);
                    System.out.println(res);
                    if(res.contains("Login success!")) {
                        // Gestisco callback
                        Registry registry = LocateRegistry.getRegistry(5000);
                        String name = "Server";
                        NotificaServer server = (NotificaServer) registry.lookup(name);
                        NotificaClient callbackObj = new NotificaImpl();
                        NotificaClient stub = (NotificaClient) UnicastRemoteObject.exportObject(callbackObj, 0);
                        server.register(stub);
                    }
                } catch (IOException | NotBoundException e) {
                    e.printStackTrace();
                }
                break;
            case "clear":
                System.out.print("\033[H\033[2J");
                System.out.flush();
                break;
            case "logout":
                try {
                    this.startConnection();
                    System.out.println(logoutUser());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "users":
                try {
                    this.startConnection();
                    String toPrint = listUsers(null);
                    toPrint = Constants.ANSI_GREEN+"Lista utenti aggiornata:\n"+toPrint.replace("?","\n")+Constants.ANSI_RESET;
                    System.out.println(toPrint);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "online":
                try {
                    this.startConnection();
                    String toPrint = listUsers("online");
                    toPrint = Constants.ANSI_GREEN+"Lista utenti online aggiornata:\n"+toPrint.replace("?","\n")+Constants.ANSI_RESET;
                    System.out.println(toPrint);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "exit":
                System.exit(0);
                break;

        }

    }

    public static void main(String[] args) {
        System.out.println(Constants.ANSI_GREEN +"Welcome to WORTH. To login, just type 'login username password'.\nif you need help on what you can do, just type 'help'."+Constants.ANSI_RESET);
        String inputText = "";
        TCPClient client = new TCPClient();
        while (!inputText.equals("exit")) {
            System.out.print(Constants.ANSI_GREEN + "> "+Constants.ANSI_RESET);
            Scanner sc = new Scanner(System.in);
            inputText = sc.nextLine();
            client.commandInterpreter(inputText);
        }

    }

}
