import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread responsible of monitoring drone positions
 */
public class MonitoringThread {
	private String hostname = "localhost";
	private int port;
	private static final Logger logger = Logger.getLogger(MonitoringThread.class.getName());
	private Socket socket;;

	public MonitoringThread(int port) {
		this.port = port;
	}

	public void start() {
		try {
			this.socket = new Socket(hostname, port);
		} catch (UnknownHostException ex) {
			logger.log(Level.SEVERE, "Server not found: " + ex.getMessage());
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "I/O error: " + ex.getMessage());
		}
	}

	public float[] queryPosition(String id) {
		try {
			OutputStream output = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(output, true);
			writer.println("?" + id);
			InputStream input = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			String text = reader.readLine();
			String[] idAndPosition = text.split(":");
			float[] position = { Float.valueOf(idAndPosition[1]), Float.valueOf(idAndPosition[2]) };
			return position;

		} catch (UnknownHostException ex) {
			logger.log(Level.SEVERE, "Server not found: " + ex.getMessage());
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "I/O error: " + ex.getMessage());
		}
		return null;
	}

	public void updatePosition(String id, float[] position) {
		try {
			OutputStream output = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(output, true);
			writer.println(id + ":" + String.valueOf(position[0]) + ":" + String.valueOf(position[1]));
			logger.info("Setting position of drone " + id + " to coordenates (" + String.valueOf(position[0]) + ", "
					+ String.valueOf(position[1]) + ")");
		} catch (UnknownHostException ex) {
			logger.log(Level.SEVERE, "Server not found: " + ex.getMessage());
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "I/O error: " + ex.getMessage());
		}
	}

	public void interrupt() {
		try {
			OutputStream output = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(output, true);
			writer.println("interrupt");
			logger.info("Closing monitoring thread...");
			socket.close();
		} catch (UnknownHostException ex) {
			logger.log(Level.SEVERE, "Server not found: " + ex.getMessage());
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "I/O error: " + ex.getMessage());
		}
	}
}