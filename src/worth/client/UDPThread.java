package worth.client;

import worth.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

//Questo thread gestisce la ricezione di messaggi/aggiornamenti di uno specifico progetto.
/*
* INDIRIZZO IP:
* Il progetto è stato testato in locale. Gli indirizzi ip per il multicast sono stati scelti tra quelli
* "Administratively scoped (local) multicast addresses" (da 239.0.0.0 a 239.255.255.255),
* in particolare da 239.0.0.0 a 239.0.0.255 (quindi con un massimo range di 255 ip, ossia 255 progetti).
* */
public class UDPThread implements Runnable{
    String ip;
    public UDPThread(String ip){
        this.ip = ip;
    }

    private void receiveMessage() throws IOException {
        byte[] buffer=new byte[1024];
        MulticastSocket socket=new MulticastSocket(Constants.UDP_PORT);
        InetAddress group=InetAddress.getByName(ip);
        socket.joinGroup(group);
        //Itera fino a che l'utente non fa logout.
        while(!Thread.currentThread().isInterrupted() && ClientMain.logged){
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
            if(ClientMain.logged)
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
