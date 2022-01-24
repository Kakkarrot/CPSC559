package Project;

import java.io.*;
import java.net.Socket;

import Project.ProjectConstants;

public class ProjectClient {
    private Socket socket;
    private BufferedReader readSocket;
    private BufferedWriter writeSocket;

    private void connectToServer(String serverUrl) throws IOException {
        socket = new Socket(serverUrl, ProjectConstants.SERVER_PORT);
        readSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writeSocket = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    private void disconnectFromServer() throws IOException {
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

    private void handleProjectIteration1Logic() throws IOException {
        for (String message = getServerResponse(); message != null; message = getServerResponse()) {
            System.out.println(message);
        }
    }

    public void handleTeamNameRequest() {

    }

    public void handleCodeRequest() {

    }

    public void handleRecieveRequest() {

    }

    public void handleReportRequest(){

    }

    public void handleCloseRequest() {

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
