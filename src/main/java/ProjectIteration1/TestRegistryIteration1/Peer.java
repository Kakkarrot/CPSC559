package ProjectIteration1.TestRegistryIteration1;

public class Peer {
	String address;
	int port;
	String teamName;
	
	Peer[] peersSent = null;
	
	String key() {
		return teamName;
	}
	public String toString() {
		return key() + " " + address + ":" + port;
	}
}
