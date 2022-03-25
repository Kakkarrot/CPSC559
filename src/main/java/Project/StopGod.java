package Project;

import ProjectIteration3.MyPeer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class StopGod {
    private void sendStop(DatagramSocket socket, String address, int port) throws IOException, InterruptedException {
        String message = "stop";
        byte[] packet = message.getBytes();
        DatagramPacket dp = new DatagramPacket(packet, packet.length, InetAddress.getByName(address), port);
        socket.send(dp);
    }

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();
            String address = "localhost";
            int port = 52444;
            StopGod god = new StopGod();
            god.sendStop(socket, address, port);
        } catch (Exception e) {
            System.out.println("Failed to send stop");
            e.printStackTrace();
        }
    }
}
