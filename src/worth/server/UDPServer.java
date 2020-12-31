package worth.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {
    public static void sendMessage(String message, String ipAddress, int port) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName(ipAddress);
        byte[] msg = message.getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length, group, port);
        socket.send(packet);
        socket.close();
    }

    public static void main(String[] args) throws IOException {
        sendMessage("This is a multicast messge", "239.0.0.1", 4321);
        sendMessage("This is the second multicast messge", "239.0.0.1", 4321);
        sendMessage("This is the third multicast messge", "239.0.0.1", 4321);
        sendMessage("OK", "239.0.0.1", 4321);
    }
}
