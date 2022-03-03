package ProjectIteration2;

import ProjectIteration2.registryIteration2WithTimer.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class ReceivePeerMessagesThread extends Thread {
    private DatagramSocket socket;
    public CopyOnWriteArraySet<MyPeer> peers;
    public volatile boolean isRunning = true;

    public ReceivePeerMessagesThread(DatagramSocket socket, CopyOnWriteArraySet<MyPeer> peers) {
        this.socket = socket;
        this.peers = peers;
    }

    private String getPeerMessage() throws IOException {
        byte[] arr = new byte[256];
        DatagramPacket packet = new DatagramPacket(arr, arr.length);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength());
    }

    private void handleStop() {
        isRunning = false;
    }

    private void handleSnip(String[] messages) {
        System.out.println(messages[0] + messages[1]);
    }

    private void handlePeer() {

    }

    private void processPeerMessage(String message) {
        System.out.println(message);
        try {
            String[] messages = message.split(" ");
            String type = messages[0].substring(0, 4);
            switch (type) {
                case "stop" -> handleStop();
                case "snip" -> handleSnip(messages);
                case "peer" -> handlePeer();
            }
        } catch (Exception e) {
            System.err.println("Could not parse " + message);
        }
    }

    public void run() {
        while (isRunning) {
            try {
                processPeerMessage(getPeerMessage());
                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("Failed to run ReceivePeerMessagesThread");
                e.printStackTrace();
            }
        }
    }
}
