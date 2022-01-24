package Quiz2;

import java.io.*;
import java.net.Socket;

public class Quiz02Client {
    private static final String URL = "136.159.5.22";
    public static final int PORT = Quiz02Server.DEFAULT_PORT_NUMBER;

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

    private String getServerResponse() throws IOException {
        String message = readSocket.readLine();
        System.out.println(message);
        return message;
    }

    private void sendServerMessage(String message) throws IOException {
        writeSocket.write(message);
        writeSocket.flush();
    }

    private void handleQuiz2Logic() throws IOException {
        String name = "Qi Feng Li\n";
        String id = "30019770\n";
        for (String message = getServerResponse(); message != null; message = getServerResponse()) {
            if (message.isEmpty()) {
                break;
            }
            switch (message) {
                case "get name" -> sendServerMessage(name);
                case "get id" -> sendServerMessage(id);
            }
        }
    }

    public static void main(String[] args){
        Quiz02Client client = new Quiz02Client();
        try {
            client.connectToServer();
            client.handleQuiz2Logic();
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
