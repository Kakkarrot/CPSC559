package Project.Iteration1;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Peer {
	String address;
	int port;
	String teamName;
	List<Peer> peersSent;

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

	public Peer(String address, int port) {
		setAddress(address);
		setPort(port);
		setTeamName("");
		peersSent = new ArrayList<>();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Peer peer = (Peer) o;
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
