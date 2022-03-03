package ProjectIteration2;

public class PeerMessage {
    int timeStamp;
    String message;
    String address;
    int port;

    public PeerMessage(String message, String address, int port) {
        this.timeStamp = -1;
        this.message = message;
        this.address = address;
        this.port = port;
    }

    public PeerMessage(int timeStamp, String message, String address, int port) {
        this.timeStamp = timeStamp;
        this.message = message;
        this.address = address;
        this.port = port;
    }
}
