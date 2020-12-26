package worth.server;

import worth.Constants;
import java.io.IOException;
import java.net.ServerSocket;

public class TCPServer {
    private ServerSocket serverSocket;

    public void start() throws IOException {
        serverSocket = new ServerSocket(Constants.TCP_PORT);
        int clients_count = 1;
        while(true){
            Thread t = new Thread(new ClientHandler(serverSocket.accept()));
            t.start();
            System.out.println("Client "+clients_count+" connected");
            clients_count++;
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    public static void main(String[] args){
        TCPServer server = new TCPServer();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
