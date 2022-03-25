package ProjectIteration3;

import Project.ProjectConstants;

import java.io.*;
import java.net.DatagramSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ProjectIteration3Client {
    public static final String DIRECTORY = "/Users/willieli/CPSC559/src/main/java/ProjectIteration3";

    private Socket registrySocket;
    private BufferedReader readSocket;
    private BufferedWriter writeSocket;
    private ConcurrentHashMap<String, ProjectIteration3.MyPeer> peers;
    private ReceivePeerMessagesThread receivePeerMessagesThread;
    private SendPeersMessageThread sendPeersMessageThread;
    private AtomicInteger timeStamp;

    private DatagramSocket receivePeerMessagesSocket;
    private String location;
    private String selfAddress;
    private int selfPort;

    public String getPeersInList() {
        StringBuilder report = new StringBuilder(peers.size() + "\n");
        for (String key : peers.keySet()) {
            report.append(key).append("\n");
        }
        return report.toString();
    }

    public void connectToRegistryFirstTime(String serverUrl) throws IOException {
        registrySocket = new Socket(serverUrl, ProjectConstants.REGISTRY_PORT);
        readSocket = new BufferedReader(new InputStreamReader(registrySocket.getInputStream()));
        writeSocket = new BufferedWriter(new OutputStreamWriter(registrySocket.getOutputStream()));
        peers = new ConcurrentHashMap<>();
    }

    public void connectToRegistrySecondTime(String serverUrl) throws IOException {
        registrySocket = new Socket(serverUrl, ProjectConstants.REGISTRY_PORT);
        readSocket = new BufferedReader(new InputStreamReader(registrySocket.getInputStream()));
        writeSocket = new BufferedWriter(new OutputStreamWriter(registrySocket.getOutputStream()));
    }

    public void initializePeerCommunication() throws IOException {
        timeStamp = new AtomicInteger(0);
        receivePeerMessagesSocket = new DatagramSocket();
        System.out.println("RecievePeerMessagesSocket: " + receivePeerMessagesSocket.getLocalPort());

    }

    public void startPeerCommunicationThreads(String registryAddress) {
        System.out.println("Socket"  + receivePeerMessagesSocket.getLocalAddress()
                + " " +receivePeerMessagesSocket.getLocalPort());
        receivePeerMessagesThread = new ReceivePeerMessagesThread(receivePeerMessagesSocket, peers, timeStamp);
        receivePeerMessagesThread.start();
        sendPeersMessageThread = new SendPeersMessageThread(peers, timeStamp, receivePeerMessagesSocket, registryAddress);
        sendPeersMessageThread.start();
    }

    public void closeRegistrySocket() throws IOException {
        readSocket.close();
        writeSocket.close();
        registrySocket.close();
    }

    public void closePeerSocket() {
        receivePeerMessagesSocket.close();
    }

    private String getRegistryResponse() throws IOException {
        return readSocket.readLine();
    }

    private void sendMessageToRegistry(String message) throws IOException {
        writeSocket.write(message);
        writeSocket.flush();
    }

    public void handleCommunicationWithRegistry(String teamName) throws IOException {
        for (String message = getRegistryResponse(); message != null; message = getRegistryResponse()) {
            System.out.println(message);
            switch (message) {
                case "get team name" -> sendMessageToRegistry(getTeamNameRequestMessage(teamName));
                case "get code" -> sendMessageToRegistry(getCodeRequestMessage());
                case "receive peers" -> processReceiveRequest();
                case "get report" -> sendMessageToRegistry(getReportRequestMessage());
                case "get location" -> sendMessageToRegistry(getLocationMessageRequest());
                case "close" -> {
                    closeRegistrySocket();
                    return;
                }
            }
        }
    }

    private String getLocationMessageRequest() {
        selfAddress = registrySocket.getLocalAddress().toString().substring(1);
        selfPort = receivePeerMessagesSocket.getLocalPort();
        location = selfAddress + ":" + selfPort;
        System.out.println("Location: " + location);
        return location + '\n';
    }

    private void addSelfToPeers() {
        peers.putIfAbsent(location, new ProjectIteration3.MyPeer(selfAddress, selfPort));
    }

    private String getFilesAsStringFrom(String path) throws IOException {
        StringBuilder result = new StringBuilder();
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) {
                    result.append(getFileAsString(file.getAbsolutePath())).append('\n');
                }
            }
        }
        return result.toString();
    }

    private String getFileAsString(String path) throws IOException {
        return Files.readString(Path.of(path), StandardCharsets.US_ASCII);
    }

    /**
     * Registry Uses Team Name as Unique Identifier
     * @return Team Name
     * @param teamName my custom identifier
     */
    private String getTeamNameRequestMessage(String teamName) {
        return teamName + '\n';
    }

    private String getCodeRequestMessage() throws IOException {
        String language = "Java\n";
        String endOfCode = "...\n";
        return language +
                getFilesAsStringFrom(ProjectIteration3Client.DIRECTORY) +
                endOfCode;
    }

    private void processReceiveRequest() throws IOException {
        String numberOfPeers = getRegistryResponse();
        System.out.println(numberOfPeers);
        for (int i = 0; i < Integer.parseInt(numberOfPeers); i++) {
            String response = getRegistryResponse();
            System.out.println(response);
            String[] temp = response.split(":");
            ProjectIteration3.MyPeer peer = new ProjectIteration3.MyPeer(temp[0], Integer.parseInt(temp[1]));
            peers.put(response, peer);
        }
    }

    private String getSingleSourceForInteration1() {
        return "1\n"
                + ProjectConstants.REGISTRY_URL
                + ':'
                + ProjectConstants.REGISTRY_PORT
                + '\n'
                + "Date Time: " + LocalDateTime.now() +
                '\n';
    }


    private String getAllPeers() {
        StringBuilder message = new StringBuilder();
        message.append(peers.size());
        message.append('\n');
        for (MyPeer peer : peers.values()) {
            message.append(peer.getAddress());
            message.append(':');
            message.append(peer.getPort());
            message.append('\n');
        }
        return message.toString();
    }

    private String getReportRequestMessage() {
        String message = "";
        message += getAllPeers();
        message += getSingleSourceForInteration1();
        message += getPeersInList();
        message += sendPeersMessageThread.getPeerMessageSent();
        message += receivePeerMessagesThread.getPeerMessageReceived();
        message += receivePeerMessagesThread.getSnipMessages();
        message += receivePeerMessagesThread.getAcks();
        System.out.println(message);
        return message;
    }

    public static void main(String[] args) {
        ProjectIteration3Client client = new ProjectIteration3Client();
        try {
            client.initializePeerCommunication();
//            client.connectToRegistryFirstTime(ProjectConstants.REGISTRY_URL);
            client.connectToRegistryFirstTime("localhost");
            client.handleCommunicationWithRegistry(ProjectConstants.TEAM_NAME);
            client.addSelfToPeers();
//            client.startPeerCommunicationThreads(ProjectConstants.REGISTRY_URL);
            client.startPeerCommunicationThreads("localhost");
            while (client.receivePeerMessagesThread.isRunning) {
                Thread.onSpinWait();
            }
            client.sendPeersMessageThread.isRunning = false;
            client.sendPeersMessageThread.sendRegistryAck(client.receivePeerMessagesThread.stopUrl, client.receivePeerMessagesThread.stopPort);
            System.out.println("Waiting for registry to stop sending acks");
            Thread.sleep(5000);
            client.getReportRequestMessage();
//            client.connectToRegistrySecondTime(ProjectConstants.REGISTRY_URL);
            client.connectToRegistrySecondTime("localhost");
            client.handleCommunicationWithRegistry(ProjectConstants.TEAM_NAME);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                client.closeRegistrySocket();
                client.closePeerSocket();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
