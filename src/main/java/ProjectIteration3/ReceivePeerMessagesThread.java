package ProjectIteration3;

import ProjectIteration3.MyPeer;

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
    public ConcurrentHashMap<String, ProjectIteration3.MyPeer> peers;
    public CopyOnWriteArrayList<PeerMessage> snipMessages;
    public CopyOnWriteArrayList<PeerAck> acks;
    public CopyOnWriteArrayList<String> peersMessagesReceived;
    public AtomicInteger timeStamp;
    public volatile boolean isRunning = true;
    public HashSet<ProjectIteration3.MyPeer> sources;
    public String stopUrl;
    public int stopPort;


    public ReceivePeerMessagesThread(DatagramSocket socket, ConcurrentHashMap<String, ProjectIteration3.MyPeer> peers, AtomicInteger timeStamp) {
        this.socket = socket;
        this.peers = peers;
        this.timeStamp = timeStamp;
        sources = new HashSet<>();
        snipMessages = new CopyOnWriteArrayList<>();
        acks = new CopyOnWriteArrayList<>();
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
        {
            stopUrl = message.address;
            stopPort = message.port;
            isRunning = false;
            System.out.println("Stop received from: " + stopUrl + ":" + stopPort);
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
        ProjectIteration3.MyPeer peer = peers.getOrDefault(peerKey, new ProjectIteration3.MyPeer(message.address, message.port));
        peer.messagesReceived.add(message);
//        System.out.println("Snip: " + peerKey);
        peers.putIfAbsent(peerKey, peer);
        snipMessages.add(new PeerMessage(timeStamp.get(), message.message, message.address, message.port));
        try {
            PeerAckSender.sendPeerAck(message);
        } catch (IOException e) {
            System.out.println("Failed to send ack");
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

    public String getAcks() {
        StringBuilder report = new StringBuilder(acks.size() + "\n");
        for (PeerAck ack : acks) {
            report.append(ack.timeStamp).append(" ");
            report.append(ack.address).append(":").append(ack.port);
            report.append("\n");
        }
        return report.toString();
    }

    private void handlePeer(PeerMessage message) {
        peers.putIfAbsent(message.address + ":" + message.port, new ProjectIteration3.MyPeer(message.address, message.port));
        String[] messageBody = message.message.split(":");
        peers.putIfAbsent(message.message.substring(4),
                new ProjectIteration3.MyPeer(messageBody[0].substring(4),
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
                case "ack " -> handleAck(message);
            }
        } catch (Exception e) {
            System.err.println("Could not parse " + message);
            e.printStackTrace();
        }
    }

    private void handleAck(PeerMessage message){
        try {
            acks.add(new PeerAck(
                    Integer.parseInt(message.message.substring(4)),
                    message.address,
                    message.port
            ));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Bad peer ack");
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
