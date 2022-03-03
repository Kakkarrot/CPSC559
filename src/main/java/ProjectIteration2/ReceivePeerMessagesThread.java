package ProjectIteration2;

import ProjectIteration2.registryIteration2WithTimer.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public class ReceivePeerMessagesThread extends Thread {
    private DatagramSocket socket;
    public ConcurrentHashMap<String, MyPeer> peers;
    public AtomicInteger timeStamp;
    public volatile boolean isRunning = true;

    public ReceivePeerMessagesThread(DatagramSocket socket, ConcurrentHashMap<String, MyPeer> peers, AtomicInteger timeStamp) {
        this.socket = socket;
        this.peers = peers;
        this.timeStamp = timeStamp;
    }

    private PeerMessage getPeerMessage() throws IOException {
        byte[] arr = new byte[256];
        DatagramPacket packet = new DatagramPacket(arr, arr.length);
        socket.receive(packet);
        PeerMessage message = new PeerMessage(
                new String(packet.getData(), 0, packet.getLength()),
                packet.getAddress().getHostAddress(),
                packet.getPort()
        );
        return message;
    }

    private void handleStop() {
        isRunning = false;
    }

    private void handleSnip(PeerMessage message) {
        if (message.timeStamp > timeStamp.get()) {
            timeStamp.set(message.timeStamp);
        }
        String peerKey = message.address + ":" + message.port;
        MyPeer peer = peers.getOrDefault(peerKey, new MyPeer(message.address, message.port));
        peer.messagesReceived.add(message);
        peers.putIfAbsent(peerKey, peer);
    }

    private void handlePeer() {

    }

    private void processPeerMessage(PeerMessage message) {
        System.out.println(message.message);
        try {
            String type = message.message.substring(0, 4);
            switch (type) {
                case "stop" -> handleStop();
                case "snip" -> handleSnip(message);
                case "peer" -> handlePeer();
            }
        } catch (Exception e) {
            System.err.println("Could not parse " + message);
            e.printStackTrace();
        }
    }

    public void run() {
        while (isRunning) {
            try {
                processPeerMessage(getPeerMessage());
                Thread.sleep(1000);
            } catch (Exception e) {
                timeStamp.getAndSet(0);
                System.err.println("Failed to run ReceivePeerMessagesThread");
                e.printStackTrace();
            }
        }
    }
}
