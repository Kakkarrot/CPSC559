package ProjectIteration2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ReceivePeerMessagesThread extends Thread {
    private DatagramSocket socket;
    public ConcurrentHashMap<String, MyPeer> peers;
    public CopyOnWriteArrayList<PeerMessage> snipMessages;
    public CopyOnWriteArrayList<String> peersMessagesReceived;
    public AtomicInteger timeStamp;
    public volatile boolean isRunning = true;
    public HashSet<MyPeer> sources;

    public ReceivePeerMessagesThread(DatagramSocket socket, ConcurrentHashMap<String, MyPeer> peers, AtomicInteger timeStamp) {
        this.socket = socket;
        this.peers = peers;
        this.timeStamp = timeStamp;
        sources = new HashSet<>();
        snipMessages = new CopyOnWriteArrayList<>();
        peersMessagesReceived = new CopyOnWriteArrayList<>();
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

    private void handleStop(PeerMessage message) {
        System.out.println("stop");
//        if (message.address == ProjectConstants.REGISTRY_URL &&
//                message.port == ProjectConstants.REGISTRY_PORT)
        {
            isRunning = false;
        }
    }

    private void changeMessageToSnip(PeerMessage message) {
        String[] temp = message.message.split(" ");
        message.timeStamp = Integer.parseInt(temp[0].substring(4));
        message.message = message.message.substring(temp[0].length() + 1);
    }

    private void handleSnip(PeerMessage message) {
        changeMessageToSnip(message);
        if (message.timeStamp > timeStamp.get()) {
            timeStamp.set(message.timeStamp);
        }
        String peerKey = message.address + ":" + message.port;
        MyPeer peer = peers.getOrDefault(peerKey, new MyPeer(message.address, message.port));
        peer.messagesReceived.add(message);
//        System.out.println("Snip: " + peerKey);
        peers.putIfAbsent(peerKey, peer);
        snipMessages.add(new PeerMessage(timeStamp.get(), message.message, message.address, message.port));
        System.out.println(message.message);

        if (message.message.contains("once_upon_a_time")) {
            isRunning = false;
        }
    }

    public String getSnipMessages() {
        StringBuilder report = new StringBuilder(snipMessages.size() + "\n");
        for (PeerMessage message : snipMessages) {
            report.append(message.timeStamp).append(" ");
            report.append(message.message).append(" ");
            report.append(message.address).append(":").append(message.port);
            report.append("\n");
        }
        return report.toString();
    }

    private void handlePeer(PeerMessage message) {
        peers.putIfAbsent(message.address + ":" + message.port, new MyPeer(message.address, message.port));
        String[] messageBody = message.message.split(":");
        peers.putIfAbsent(message.message.substring(4),
                new MyPeer(messageBody[0].substring(4),
                        Integer.parseInt(messageBody[1])));
        sources.add(new MyPeer(message.address, message.port));
        peersMessagesReceived.add(message.message.substring(4) + " "
                + message.address + ":" + message.port + " "
                + LocalDateTime.now());
    }

    public String getPeerMessageReceived(){
        StringBuilder report = new StringBuilder(peersMessagesReceived.size() + "\n");
        for (String message : peersMessagesReceived) {
            report.append(message).append("\n");
        }
        return report.toString();
    }

    private void processPeerMessage(PeerMessage message) {
        System.out.println(message.message);
        try {
            String type = message.message.substring(0, 4);
            switch (type) {
                case "stop" -> handleStop(message);
                case "snip" -> handleSnip(message);
                case "peer" -> handlePeer(message);
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
            } catch (Exception e) {
                timeStamp.getAndSet(0);
                System.err.println("Failed to run ReceivePeerMessagesThread");
                e.printStackTrace();
            }
        }
    }
}
