package worth.client;

import worth.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class UDPThread implements Runnable{
    String ip;
    public UDPThread(String ip){
        this.ip = ip;
    }

    private void receiveMessage() throws IOException {
        byte[] buffer=new byte[1024];
        MulticastSocket socket=new MulticastSocket(4321);
        InetAddress group=InetAddress.getByName(ip);
        socket.joinGroup(group);
        while(!Thread.currentThread().isInterrupted()){
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
            System.out.print(">> " + msg + "\n" + Constants.ANSI_GREEN + ">" + Constants.ANSI_RESET);
        }
        socket.leaveGroup(group);
        socket.close();
    }
    @Override
    public void run() {
        try{
            receiveMessage();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
