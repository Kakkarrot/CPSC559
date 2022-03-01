package ProjectIteration2;

import ProjectIteration1.Peer;
import Project.ProjectConstants;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArraySet;

public class ProjectIteration2Client {
    public static final String DIRECTORY = "/Users/willieli/CPSC559/src/main/java/ProjectIteration2";

    private Socket registrySocket;
    private BufferedReader readSocket;
    private BufferedWriter writeSocket;
    private CopyOnWriteArraySet<Peer> peers;

    private DatagramSocket peerSocket;


    public void connectToRegistry(String serverUrl) throws IOException {
        registrySocket = new Socket(serverUrl, ProjectConstants.REGISTRY_PORT);
        readSocket = new BufferedReader(new InputStreamReader(registrySocket.getInputStream()));
        writeSocket = new BufferedWriter(new OutputStreamWriter(registrySocket.getOutputStream()));
        peers = new CopyOnWriteArraySet<>();
    }

    public void initializePeerCommunication() throws IOException {
        peerSocket = new DatagramSocket();
        System.out.println(peerSocket.getPort() + " " + peerSocket.getLocalPort());
    }

    public void closeRegistrySocket() throws IOException {
        readSocket.close();
        writeSocket.close();
        registrySocket.close();
    }

    public void closePeerSocket() {
        peerSocket.close();
    }

    private String getRegistryResponse() throws IOException {
        return readSocket.readLine();
    }

    private void sendMessageToRegistry(String message) throws IOException {
        writeSocket.write(message);
        writeSocket.flush();
    }

    public void handleCommunicationWithRegistry() throws IOException {
        for (String message = getRegistryResponse(); message != null; message = getRegistryResponse()) {
            System.out.println(message);
            switch (message) {
                case "get team name" -> sendMessageToRegistry(getTeamNameRequestMessage());
                case "get code" -> sendMessageToRegistry(getCodeRequestMessage());
                case "receive peers" -> processRecieveRequest();
                case "get report" -> sendMessageToRegistry(getReportRequestMessage());
                case "get location" -> sendMessageToRegistry(getLocationMessageRequest());
                case "close" -> {
                    return;
                }
            }
        }
    }

    public String getMessageFromPeers() throws IOException {
        //currently stopped here because no peer messages
        byte[] arr = new byte[256];
        DatagramPacket packet = new DatagramPacket(arr, arr.length);
        peerSocket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength());
    }

    public void sendMessageToPeers(String message) throws IOException {
        //I need to receive messages from my port
        DatagramPacket packet = new DatagramPacket(message.getBytes(),
                message.getBytes().length,
                registrySocket.getLocalAddress(),
                peerSocket.getLocalPort());
        peerSocket.send(packet);
    }

    public void handleCommunicationWithPeers() throws IOException {
        sendMessageToPeers("Testing");
        for (String message = getMessageFromPeers(); true; message = getMessageFromPeers()) {
            System.out.println(message);
        }
    }

    private String getLocationMessageRequest() {
        return registrySocket.getLocalAddress().toString().substring(1) + ":" + peerSocket.getLocalPort() + '\n';
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

    private String getTeamNameRequestMessage() {
        return ProjectConstants.TEAM_NAME + '\n';
    }

    private String getCodeRequestMessage() throws IOException {
        String language = "Java\n";
        String endOfCode = "...\n";
        return language +
                getFilesAsStringFrom(ProjectIteration2Client.DIRECTORY) +
                endOfCode;
    }

    private void processRecieveRequest() throws IOException {
        String numberOfPeers = getRegistryResponse();
        System.out.println(numberOfPeers);
        for (int i = 0; i < Integer.parseInt(numberOfPeers); i++) {
            String response = getRegistryResponse();
            System.out.println(response);
            String[] temp = response.split(":");
            Peer peer = new Peer(temp[0], Integer.parseInt(temp[1]));
            peers.add(peer);
        }
    }

    private String getSingleSourceForInteration1() {
        return "1\n"
                + ProjectConstants.REGISTRY_URL
                + ':'
                + ProjectConstants.REGISTRY_PORT
                + '\n'
                + LocalDateTime.now() +
                '\n';
    }

    private String getAllPeers() {
        StringBuilder message = new StringBuilder();
        message.append(peers.size());
        message.append('\n');
        for (Peer peer : peers) {
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
        message += getAllPeers();
        return message;
    }

    public static void main(String[] args) {
        ProjectIteration2Client client = new ProjectIteration2Client();
        String testUrl = "localhost";
        try {
            client.initializePeerCommunication();
            client.connectToRegistry(testUrl);
//            client.connectToRegistry(ProjectConstants.REGISTRY_URL);
            client.handleCommunicationWithRegistry();
            client.handleCommunicationWithPeers();
//            client.handleCommunicationWithRegistry();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.closeRegistrySocket();
                client.closePeerSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
