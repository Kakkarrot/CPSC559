package ProjectIteration3;

public class PeerAck {
    int timeStamp;
    String address;
    int port;

    public PeerAck(int timeStamp, String address, int port) {
        this.timeStamp = timeStamp;
        this.address = address;
        this.port = port;
    }
}
