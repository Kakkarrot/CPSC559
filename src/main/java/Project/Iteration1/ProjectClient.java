package Project.Iteration1;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProjectClient {
    public static final String DIRECTORY = "/Users/willieli/CPSC559/src/main/java/Project/Iteration1";

    private Socket socket;
    private BufferedReader readSocket;
    private BufferedWriter writeSocket;

    public void connectToServer(String serverUrl) throws IOException {
        socket = new Socket(serverUrl, ProjectConstants.SERVER_PORT);
        readSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writeSocket = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void disconnectFromServer() throws IOException {
        readSocket.close();
        writeSocket.close();
        socket.close();
    }

    private String getServerResponse() throws IOException {
        String message = readSocket.readLine();
        return message;
    }

    private void sendServerMessage(String message) throws IOException {
        writeSocket.write(message);
        writeSocket.flush();
    }

    public void handleProjectIteration1Logic() throws IOException {
        for (String message = getServerResponse(); message != null; message = getServerResponse()) {
            System.out.println(message);
            switch (message) {
                case "get team name" -> sendServerMessage(getTeamNameRequestMessage());
                case "get code" -> sendServerMessage(getCodeRequestMessage());
            }
        }
    }

    private String getFilesAsStringFrom(String path) throws IOException {
        StringBuilder result = new StringBuilder();
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                result.append(getFileAsString(file.getAbsolutePath())).append('\n');
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
                getFilesAsStringFrom(DIRECTORY) +
                endOfCode;
    }

    private void handleRecieveRequest() {

    }

    private void handleReportRequest() {

    }

    private void handleCloseRequest() {

    }

    public static void main(String[] args) {
        ProjectClient client = new ProjectClient();
        try {
            client.connectToServer("localhost");
            client.handleProjectIteration1Logic();
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
