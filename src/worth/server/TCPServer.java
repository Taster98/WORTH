package worth.server;
import worth.Constants;
import java.io.IOException;
import java.net.ServerSocket;

//Classe che rappresenta il server TCP.
public class TCPServer {
    private ServerSocket serverSocket;
    private ServerNotImpl serverCB;
    public boolean shutdown;
    public TCPServer(ServerNotImpl serverCB){
        this.serverCB = serverCB;
        this.shutdown = false;
    }
    //Quando viene avviato, resta in ascolto sulla porta TCP_PORT di richieste di connessione da parte di client.
    public void start() throws IOException {
        serverSocket = new ServerSocket(Constants.TCP_PORT);
        while(!shutdown){
            //Per ogni richiesta di connessione viene creato un thread per gestire il client che viene associato a quest'ultimo.
            Thread t = new Thread(new ClientHandler(serverSocket.accept(), serverCB));
            t.start();
        }
    }
    //Chiudo la connessione.
    public void stop() throws IOException {
        serverSocket.close();
    }
}
