package ProjectIteration2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class MyPeer {
	String address;
	int port;
	String teamName;
	CopyOnWriteArraySet<MyPeer> peersSent;
	CopyOnWriteArrayList<PeerMessage> messagesReceived;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public MyPeer(String address, int port) {
		setAddress(address);
		setPort(port);
		setTeamName("");
		peersSent = new CopyOnWriteArraySet<>();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MyPeer peer = (MyPeer) o;
		return port == peer.port && address.equals(peer.address) && teamName.equals(peer.teamName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(address, port, teamName);
	}

	String key() {
		return teamName;
	}
	public String toString() {
		return key() + " " + address + ":" + port;
	}
}
