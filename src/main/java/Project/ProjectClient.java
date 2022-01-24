package Project;

import java.io.*;
import java.net.Socket;

public class ProjectClient {

    private static final String URL = "136.159.5.22";
    public static final int PORT = 55921;

    private Socket socket;
    private BufferedReader readSocket;
    private BufferedWriter writeSocket;

    private void connectToServer() throws IOException {
        socket = new Socket(URL, PORT);
        readSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writeSocket = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    private void disconnectFromServer() throws IOException {
        readSocket.close();
        writeSocket.close();
        socket.close();
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

    }
}
