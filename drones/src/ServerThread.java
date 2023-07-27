import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread responsible of client connection handling
 */
public class ServerThread implements Runnable {
	private static final Logger logger = Logger.getLogger(ServerThread.class.getName());
	private ThreadPoolExecutor pool;
	private ServerSocket serverSocket;
	private boolean running;
	private ArrayList<DeliveryDrone> drones;

	public ServerThread(int port, ArrayList<DeliveryDrone> drones) throws IOException {
		this.pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		this.drones = drones;
		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(1000);
		logger.info("Server listening on port " + serverSocket.getLocalPort());
	}

	/**
	 * Returns the number of currently running client threads.
	 */
	public int getClientCount() {
		return pool.getActiveCount();
	}

	@Override
	public void run() {
		running = true;
		while (running) {
			try {
				Socket socket = serverSocket.accept();
				logger.info(String.format("Connection established: %s", socket.getRemoteSocketAddress()));
				ClientThread clientThread = new ClientThread(socket, drones);
				pool.execute(clientThread);
			} catch (SocketTimeoutException e) {
				// Check if the current thread has been interrupted
				if (Thread.currentThread().isInterrupted()) {
					stop();
				}
			} catch (IOException e) {
				if (running) {
					logger.log(Level.WARNING, "I/O error when handling client connection request", e);
				}
			}
		}
	}

	public void stop() {
		if (!running)
			return;
		try {
			logger.info("Shutting down server...");
			running = false;
			serverSocket.close();
			pool.shutdown(); // Disable new tasks from being submitted
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(100, TimeUnit.MILLISECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(100, TimeUnit.MILLISECONDS))
					logger.severe("Pool did not terminate");
			}
			logger.info("Server shutdown complete");
		} catch (IOException | InterruptedException e) {
			logger.log(Level.WARNING, "Server interrupted while shutting down", e);
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	// FUNCIONES ÚTILES

	/**
	 * Comparator used to decide which drone is closer to a position
	 */
	class SorterByDistance implements Comparator<GenericDrone> {

		float[] position = new float[2];

		public SorterByDistance(float[] position) {
			this.position = position;
		}

		public int compare(GenericDrone a, GenericDrone b) {
			return (int) (100000000 * (a.distanceTo(position) - b.distanceTo(position)));
		}
	}

	public float[] getDronePosition(String id) {
		for (DeliveryDrone drone : drones) {
			if (drone.getSerialNumber().equals(id)) {
				return drone.getPosition();
			}
		}
		return null;
	}

}
