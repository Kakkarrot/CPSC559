package ProjectIteration2;

import Project.ProjectConstants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.CopyOnWriteArraySet;

public class SendPeersMessageThread extends Thread {
    public CopyOnWriteArraySet<MyPeer> peers;
    DatagramSocket socket;
    String MESSAGE_START = "snip";
    String MESSAGE_BODY = ProjectConstants.TEAM_NAME;
    int timeStamp;
    volatile boolean isRunning = true;

    public SendPeersMessageThread(CopyOnWriteArraySet<MyPeer> peers) {
        this.peers = peers;
        this.timeStamp = 0;
        try {
            socket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                String message = MESSAGE_START + timeStamp + " " + MESSAGE_BODY;
                byte[] packet = message.getBytes();
                for (MyPeer peer: peers) {
                    DatagramPacket dp = new DatagramPacket(packet, packet.length, InetAddress.getByName(peer.getAddress()), peer.getPort());
                    socket.send(dp);
                }
                Thread.sleep(5000);
                timeStamp += 5;
            } catch (Exception e) {
                System.err.println("Failed to run SendPeersMessageThread");
            }
        }
    }
}
