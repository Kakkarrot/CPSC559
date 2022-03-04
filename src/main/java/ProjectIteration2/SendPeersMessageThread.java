package ProjectIteration2;

import Project.ProjectConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class SendPeersMessageThread extends Thread {
    public ConcurrentHashMap<String, MyPeer> peers;
    DatagramSocket socket;
    String MESSAGE_START = "snip";
    String PEER_START = "peer";
    String MESSAGE_BODY = ProjectConstants.TEAM_NAME;
    public AtomicInteger timeStamp;
    public CopyOnWriteArrayList<String> peerMessageSent;
    volatile boolean isRunning = true;
    BufferedReader inputStream = new BufferedReader(
            new InputStreamReader(System.in));

    public SendPeersMessageThread(ConcurrentHashMap<String, MyPeer> peers, AtomicInteger timeStamp, DatagramSocket socket) {
        this.peers = peers;
        this.timeStamp = timeStamp;
        try {
            this.socket = socket;
        } catch (Exception e) {
            e.printStackTrace();
        }
        peerMessageSent = new CopyOnWriteArrayList<>();
    }

    @Override
    public void run() {
        System.out.println("Welcome to the chat room! Type anything and press enter to send. ");

        Runnable incrementTimer = () -> {
            while (!interrupted()) {
                timeStamp.getAndIncrement();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable sendPeer = () -> {
            while (!interrupted()) {
                try {
                    sendPeer();
                    Thread.sleep(5000);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread timerThread = new Thread(incrementTimer);
        Thread peerThread = new Thread(sendPeer);
        timerThread.start();
        peerThread.start();


        while (isRunning) {
            try {
                sendSnip();
            } catch (Exception e) {
                System.err.println("Failed to run SendPeersMessageThread");
                e.printStackTrace();
            }
        }
        timerThread.interrupt();
        peerThread.interrupt();
    }

    private void sendPeer() throws InterruptedException, IOException {
        for (MyPeer recipient : peers.values()) {
            for (MyPeer peer : peers.values()) {
                if (peer != recipient) {
                    if (Math.random() > 0.1) {
                        continue;
                    }
                    String message = PEER_START + peer.getAddress() + ":" + peer.getPort();
                    byte[] packet = message.getBytes();
                    DatagramPacket dp = new DatagramPacket(packet, packet.length, InetAddress.getByName(recipient.getAddress()), recipient.getPort());
                    socket.send(dp);
//                    System.out.println("Sent: " + message + " to " + recipient.getAddress() + ":" + recipient.getPort());
                    peerMessageSent.add(message.substring(4) + " "
                            + InetAddress.getByName(recipient.getAddress()) + ":" + peer.getPort() + " "
                            + LocalDateTime.now());
                }
            }
        }
    }

    public String getPeerMessageSent() {
        StringBuilder report = new StringBuilder(peerMessageSent.size() + "\n");
        for (String message : peerMessageSent) {
            report.append(message).append("\n");
        }
        return report.toString();
    }

    private void sendSnip() throws IOException, InterruptedException {
        String messageBody = inputStream.readLine();
        String message = MESSAGE_START + timeStamp + " " + messageBody;
        byte[] packet = message.getBytes();
        for (MyPeer peer : peers.values()) {
            DatagramPacket dp = new DatagramPacket(packet, packet.length, InetAddress.getByName(peer.getAddress()), peer.getPort());
            try {
                socket.send(dp);
            } catch (Exception e) {
                System.out.println("Bad peer");
            }
        }
    }
}
