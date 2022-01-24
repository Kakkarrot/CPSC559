package Quiz2;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

public class Quiz02Server {
	/** Number of threads allowed in the system.  Each thread manages communication with a client */
	public static final int THREAD_POOL_SIZE = 10;
	/** If no port number is provided when running, this port number will be used. */
	public static final int DEFAULT_PORT_NUMBER = 55921;

	protected final static Logger LOGGER = Logger.getLogger(Quiz02Server.class.getName());
	
	/** port number that this server is running on */
	private int portNumber;
	
	/**
	 * Create registry to run at specified port number	
	 * @param aPortNumber port number to attempt running this registry at.
	 */
	public Quiz02Server(int aPortNumber) {
		portNumber = aPortNumber;
	}
	
	/**
	 * Starts this registry and accepts connection requests from peers.  For each
	 * connection request, a RequestProcessor object is created and provided to the
	 * thread pool.
	 * @throws IOException if there are problems starting this registry server or if there
	 * are problems communication alonger a connection with a peer.
	 */
	public void start() throws IOException {
		ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		try {
			ServerSocket server = new ServerSocket(portNumber);
			System.out.println("Server started at " + 
					server.getInetAddress().getLocalHost().getHostAddress() +
					":" + portNumber);
			while (true) {
				Socket sock = server.accept();
				executor.execute(() -> getInfo(sock));				
			}
		} catch (BindException be) {
			LOGGER.log(Level.SEVERE, "Unable to start Quiz2Server at port " + portNumber + be.getMessage());
		}
		executor.shutdown();
	}
	
	private void getInfo(Socket sock) {
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			Random rand = new Random();
			boolean idFirst = rand.nextBoolean();
			String id = null;
			String name = null;
			if (idFirst) {
				id = getID(out, in);
				name = getName(out, in);
			} else {
				name = getName(out, in);
				id = getID(out, in);
			}
			log(Level.INFO, id, name, sock);
			out.close();
			in.close();
			sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/*
	 * Get the id of the student that connected for this quiz  Communication protocol:
	 * 
	 * send: 'get id\n'
	 * receive: <id of student><new line>
	 */
	private String getID(BufferedWriter out, BufferedReader in) throws IOException {
		String id = null;
		out.write("get id\n");
		out.flush();
		id = in.readLine();
		return id;
	}
	
	/*
	 * Get the name of the student that connected for this quiz  Communication protocol:
	 * 
	 * send: 'get name\n'
	 * receive: <name of student><new line>
	 */
	private String getName(BufferedWriter out, BufferedReader in) throws IOException {
		String name = null;
		out.write("get name\n");
		out.flush();
		name = in.readLine();
		return name;
	}
	
	/**
	 * Format logging message to include client information
	 * @param level severity level of log message
	 * @param message data for log message
	 */
	private void log(Level level, String id, String name, Socket sock) {
		String logMessage = "#" + id + ",5,#," + name + "," + ((sock != null) ? sock.getRemoteSocketAddress().toString() : "");
		LOGGER.log(level, logMessage);
	}


	
	

	/**
	 * Starts this quiz 02 server. If a port number is provided as a runtime argument, 
	 * it will be used to start the server.
	 * Otherwise, the port number provided as an argument will be used. 
	 * <p>
	 * If we can't start the server, the stack trace for the exception will
	 * be printed and the program ended.
	 * 
	 * @param args optional port number as a first argument.
	 */
	public static void main(String[] args)  {
		LOGGER.setLevel(Level.INFO);
		// logging format is: <log message>,<date in format yyyy-mm-dd>,<time in format hh-mm-ss>
		System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s,%1$tF,%1$tT%n");
		try {
			FileHandler fh = new FileHandler("Quiz02.log", true);
			fh.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fh);
		} catch (Exception e) {
			System.out.println("Failed to setup logging file.  Logging to console instead.");
			e.printStackTrace();
		}		

		int portNumber = DEFAULT_PORT_NUMBER;
		if (args.length > 0) {
			try {
				portNumber = Integer.parseInt(args[0]);
			} catch (NumberFormatException nfe) {
				System.out.println("Expected first argument to be a port number.  Argument ignored.");
			}
		}
		Quiz02Server qs = new Quiz02Server(portNumber);
		Thread t = new Thread() { 
			public void run() {
				try {
					qs.start();
				} catch (IOException e) {
					// Show that an error occurred with exception info
					e.printStackTrace();
					// end program with a error code
					System.exit(1);
				}
			} 
		};
		t.start();
	}
}
