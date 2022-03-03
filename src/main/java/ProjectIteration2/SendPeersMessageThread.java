package ProjectIteration2;

import Project.ProjectConstants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public class SendPeersMessageThread extends Thread {
    public ConcurrentHashMap<String, MyPeer> peers;
    DatagramSocket socket;
    String MESSAGE_START = "snip";
    String MESSAGE_BODY = ProjectConstants.TEAM_NAME;
    public AtomicInteger timeStamp;
    volatile boolean isRunning = true;

    public SendPeersMessageThread(ConcurrentHashMap<String, MyPeer> peers, AtomicInteger timeStamp) {
        this.peers = peers;
        this.timeStamp = timeStamp;
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
                for (MyPeer peer: peers.values()) {
                    DatagramPacket dp = new DatagramPacket(packet, packet.length, InetAddress.getByName(peer.getAddress()), peer.getPort());
                    socket.send(dp);
                }
                Thread.sleep(10000);
                timeStamp.getAndIncrement();
            } catch (Exception e) {
                System.err.println("Failed to run SendPeersMessageThread");
                e.printStackTrace();
            }
        }
    }
}
