import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread implements Runnable {
	private static final Logger logger = Logger.getLogger(ClientThread.class.getName());
	private final Socket socket;
	private boolean running;
	private ArrayList<DeliveryDrone> drones;

	public ClientThread(Socket socket, ArrayList<DeliveryDrone> drones) {
		this.socket = Objects.requireNonNull(socket, "Client socket must not be null");
		this.drones = drones;
	}

	@Override
	public void run() {
		running = true;
		try {
			InputStream input = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			OutputStream output = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(output, true);
			String text;
			while (running) {
				text = reader.readLine();
				if (text.equals("interrupt")) {
					stop();
					logger.info("Stopping client thread");
				} else if (text.startsWith("?")) { // we are being requested to return the position of a drone
					int id = Integer.valueOf(text.substring(1));
					float[] position = drones.get(id).getPosition();
					writer.println(String.format("%d:%f:%f", id, position[0], position[1]));
				} else { // we are being requested to update the position of a drone
					String[] idAndPosition = text.split(":");
					int id = Integer.valueOf(idAndPosition[0]);
					float[] position = { Float.valueOf(idAndPosition[1]), Float.valueOf(idAndPosition[2]) };
					drones.get(id).setPosition(position);
					logger.info("Position of drone " + String.valueOf(id) + " set to coordenates ("
							+ String.valueOf(position[0]) + ", " + String.valueOf(position[1]) + ")");
				}
			}
			socket.close();
		} catch (NoSuchElementException e) { // When the connection is closed by the peer
			logger.info("Connection reset by peer " + socket.getRemoteSocketAddress());
		} catch (IOException e) { // When the connection is closed by the server
			logger.log(Level.WARNING, String.format("Error handling client %s: ", socket.getRemoteSocketAddress()), e);
		} finally {
			stop();
		}
	}

	public void stop() {
		if (!running)
			return;
		try {
			logger.info("Closing connection to " + socket.getRemoteSocketAddress());
			socket.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, "I/O Error closing client socket", e);
		}
		running = false;
	}

}
