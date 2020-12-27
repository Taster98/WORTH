package worth.server;

import worth.Constants;
import java.io.IOException;
import java.net.ServerSocket;

public class TCPServer {
    private ServerSocket serverSocket;
    private ServerNotImpl serverCB;
    public TCPServer(ServerNotImpl serverCB){
        this.serverCB = serverCB;
    }
    public void start() throws IOException {
        serverSocket = new ServerSocket(Constants.TCP_PORT);
        while(true){
            Thread t = new Thread(new ClientHandler(serverSocket.accept(), serverCB));
            t.start();
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    public static void main(String[] args){
        TCPServer server = new TCPServer(null);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
