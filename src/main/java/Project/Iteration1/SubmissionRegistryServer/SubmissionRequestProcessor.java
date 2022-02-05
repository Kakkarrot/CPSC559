package Project.Iteration1.SubmissionRegistryServer;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;

class SubmissionRequestProcessor extends RequestProcessor implements Runnable {
	SubmissionRequestProcessor(Socket aPeer, Registry aSource) {
		super(aPeer, aSource);
	}
	
	@Override
	public void run() {
		try {
			log(Level.INFO, "start");
			Peer addedPeer = addPeer();
			getReport(addedPeer);
			sendPeers();
			addPeer();
			getReport(addedPeer);
			sendPeers();
			getReport(addedPeer);
			closePeer();
		} catch (IOException e) {
			log(Level.WARNING, "Problem processing socket.", e);
		}
	}
	
}