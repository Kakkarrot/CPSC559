package Project.Iteration1.SubmissionRegistryServer;

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
