package ProjectIteration2;

import ProjectIteration1.Peer;
import Project.ProjectConstants;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArraySet;

public class ProjectIteration2Client {
    public static final String DIRECTORY = "/Users/willieli/CPSC559/src/main/java/ProjectIteration2";

    private Socket socket;
    private BufferedReader readSocket;
    private BufferedWriter writeSocket;
    private CopyOnWriteArraySet<Peer> peers;

    public void connectToServer(String serverUrl) throws IOException {
        socket = new Socket(serverUrl, ProjectConstants.SERVER_PORT);
        readSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writeSocket = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        peers = new CopyOnWriteArraySet<>();
    }

    public void disconnectFromServer() throws IOException {
        readSocket.close();
        writeSocket.close();
        socket.close();
    }

    private String getServerResponse() throws IOException {
        return readSocket.readLine();
    }

    private void sendServerMessage(String message) throws IOException {
        writeSocket.write(message);
        writeSocket.flush();
    }

    public void handleProjectIteration2Logic() throws IOException {
        for (String message = getServerResponse(); message != null; message = getServerResponse()) {
            System.out.println(message);
            switch (message) {
                case "get team name" -> sendServerMessage(getTeamNameRequestMessage());
                case "get code" -> sendServerMessage(getCodeRequestMessage());
                case "receive peers" -> processRecieveRequest();
                case "get report" -> sendServerMessage(getReportRequestMessage());
                case "get location" -> sendServerMessage(getLocationMessageRequest());
                case "close" -> {
                    return;
                }
            }
        }
    }

    private String getLocationMessageRequest() {
        return socket.getLocalAddress().toString().substring(1) + ":" + socket.getLocalPort() + '\n';
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
        String numberOfPeers = getServerResponse();
        for (int i = 0; i < Integer.parseInt(numberOfPeers); i++) {
            String response = getServerResponse();
            String[] temp = response.split(":");
            Peer peer = new Peer(temp[0], Integer.parseInt(temp[1]));
            peers.add(peer);
        }
    }

    private String getSingleSourceForInteration1() {
        return "1\n"
                + ProjectConstants.SERVER_URL
                + ':'
                + ProjectConstants.SERVER_PORT
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
            client.connectToServer(testUrl);
            client.handleProjectIteration2Logic();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.disconnectFromServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
