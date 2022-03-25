package ProjectIteration3;

import Project.ProjectConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class PeerAckSender {
    public static DatagramSocket socket;

    public static void sendPeerAck(PeerMessage message) throws IOException {
        if (socket == null) {
            socket = new DatagramSocket();
        }
        String ack = "ack " + message.timeStamp;
        byte[] packet = ack.getBytes();
        DatagramPacket dp = new DatagramPacket(packet, packet.length,
                InetAddress.getByName(message.address), message.port);
        socket.send(dp);
        System.out.println("Sent ack to peer: " + message.address + ":" + message.port);
    }

    static public void sendRegistryAck(String address, int port) {
        String message = "ack" + ProjectConstants.TEAM_NAME;
        byte[] packet = message.getBytes();
        try {
            DatagramPacket dp = new DatagramPacket(packet, packet.length,
                    InetAddress.getByName(address), port);
            socket.send(dp);
            System.out.println("Sent ack to registry: " + address + ":" + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
