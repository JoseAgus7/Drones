import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class ControlFrame extends JFrame implements ActionListener {

	// Default serial version ID
	private static final long serialVersionUID = 1L;

	// Logger
	private static Logger logger = Logger.getLogger(ControlFrame.class.getName());
	private static final int LOG_SIZE = 1000;

	// Threads
	private ServerThread serverThread;
	private AssignmentThread assignmentThread;

	// private DeliveryThread deliveryThread;
	private ExecutorService serverExecutor;
	private ExecutorService clientExecutor;

	// Drones and deliveries
	private ArrayList<DeliveryDrone> drones = new ArrayList<>();
	private ArrayList<DeliveryAssignment> assignments = new ArrayList<>();

	// Server control panel
	private JButton serverStartButton;
	private JButton serverStopButton;
	private JTextField serverPortField;
	private JLabel serverPortLabel;
	private JLabel serverStatusLabel;

	// Log area
	private JButton logClearButton;
	private JTextArea logArea;

	// Drone and deliveries panel
	private JButton droneAddButton;
	private JLabel droneXLabel;
	private JTextField droneXCoord;
	private JLabel droneYLabel;
	private JTextField droneYCoord;
	private JButton deliveryAddButton;
	private JLabel deliveryXLabel;
	private JTextField deliveryXCoord;
	private JLabel deliveryYLabel;
	private JTextField deliveryYCoord;

	// The GUI class
	public ControlFrame() {
		// Set window title
		super("Drone server and client control example");

		// Publish logs into the log text area
		LogManager.getLogManager().getLogger("").addHandler(new LogHandler());

		// Content panel
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		// Inner panels
		JPanel serverPanel = new JPanel(new BorderLayout());
		JPanel serverConfPanel = new JPanel();
		JPanel logPanel = new JPanel(new BorderLayout());
		JPanel controlPanel = new JPanel(new BorderLayout());
		JPanel droneControlPanel = new JPanel();
		JPanel deliveryControlPanel = new JPanel();

		// Server widgets
		serverStartButton = new JButton("Start");
		serverStopButton = new JButton("Stop");
		serverPortField = new JTextField(10);
		serverPortLabel = new JLabel("Port");
		serverPortField.setText("31416");
		serverStatusLabel = new JLabel("Offline");

		// Server Layout
		serverConfPanel.add(serverStartButton);
		serverConfPanel.add(serverStopButton);
		serverConfPanel.add(serverPortLabel);
		serverConfPanel.add(serverPortField);
		serverPanel.add(serverConfPanel, BorderLayout.WEST);
		serverPanel.add(serverStatusLabel, BorderLayout.EAST);
		serverPanel.setBorder(BorderFactory.createTitledBorder("Server Configuration"));

		// Log area
		logClearButton = new JButton("Clear log");
		logArea = new JTextArea(25, 80);
		logArea.setEditable(false);
		((AbstractDocument) logArea.getDocument()).setDocumentFilter(new LogDocumentFilter(LOG_SIZE));

		// Log layout
		logPanel.add(new JScrollPane(logArea), BorderLayout.NORTH);
		logPanel.add(new JScrollPane(logClearButton), BorderLayout.SOUTH);
		logPanel.setBorder(BorderFactory.createTitledBorder("Log Area"));

		// Control widgets
		droneAddButton = new JButton("  Add drone    ");
		droneXLabel = new JLabel("X:");
		droneXCoord = new JTextField(12);
		droneYLabel = new JLabel("Y:");
		droneYCoord = new JTextField(12);
		deliveryAddButton = new JButton(" Add delivery ");
		deliveryXLabel = new JLabel("X:");
		deliveryXCoord = new JTextField(12);
		deliveryYLabel = new JLabel("Y:");
		deliveryYCoord = new JTextField(12);

		// Control layout
		droneControlPanel.add(droneAddButton);
		droneControlPanel.add(droneXLabel);
		droneControlPanel.add(droneXCoord);
		droneControlPanel.add(droneYLabel);
		droneControlPanel.add(droneYCoord);
		deliveryControlPanel.add(deliveryAddButton);
		deliveryControlPanel.add(deliveryXLabel);
		deliveryControlPanel.add(deliveryXCoord);
		deliveryControlPanel.add(deliveryYLabel);
		deliveryControlPanel.add(deliveryYCoord);
		controlPanel.add(droneControlPanel, BorderLayout.NORTH);
		controlPanel.add(deliveryControlPanel, BorderLayout.SOUTH);
		controlPanel.setBorder(BorderFactory.createTitledBorder("Control area"));

		// General layout
		add(serverPanel, BorderLayout.NORTH);
		add(logPanel, BorderLayout.CENTER);
		add(controlPanel, BorderLayout.SOUTH);

		// Listeners
		serverStartButton.setActionCommand("start server");
		serverStartButton.addActionListener(this);
		serverStopButton.setActionCommand("stop server");
		serverStopButton.addActionListener(this);
		logClearButton.setActionCommand("clear log");
		logClearButton.addActionListener(this);
		droneAddButton.setActionCommand("add drone");
		droneAddButton.addActionListener(this);
		deliveryAddButton.setActionCommand("add delivery");
		deliveryAddButton.addActionListener(this);

		// Initial state
		serverStopButton.setEnabled(false);

		// serverExecutor
		serverExecutor = Executors.newSingleThreadExecutor();
		clientExecutor = Executors.newSingleThreadExecutor();

		// Prepare GUI
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	// Actions handler
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("start server")) {
			if (JOptionPane.showConfirmDialog(null,
					new JLabel("Sei sicuro di voler avviare il server?"
							+ " Una volta iniziato non potrai aggiungere nuove delivery o drone"),
					"Confirm server start", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
				try {
					int port = Integer.parseInt(serverPortField.getText());
					logger.info("Creating a new server...");
					serverThread = new ServerThread(port, drones);
					serverExecutor.execute(serverThread);
					serverStartedState();
					logger.info("Creating a client for demostrative purposes...");
					MonitoringThread monitoringThread = new MonitoringThread(port);
					monitoringThread.start();
					assignmentThread = new AssignmentThread(monitoringThread, assignments, drones);
					clientExecutor.execute(assignmentThread);
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(this, "Formato porta errato", "Errore", JOptionPane.ERROR_MESSAGE);
				} catch (IOException ex) {
					logger.log(Level.SEVERE, "I/O error opening server socket, exiting... " + ex);
					JOptionPane.showMessageDialog(this, "Incapace di aprire il socket. Forse il porto é in utilizzo?",
							"Errore", JOptionPane.ERROR_MESSAGE);
					serverStoppedState();
				}
			}
		} else if (cmd.equals("stop server")) {
			try {
				serverThread.stop();
				serverExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
				assignmentThread.stop();
				clientExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
				serverStoppedState();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		} else if (cmd.equals("clear log")) {
			logArea.setText("");
		} else if (cmd.equals("add delivery")) {
			try {
				float[] deliveryCoords = { Float.parseFloat(deliveryXCoord.getText()),
						Float.parseFloat(deliveryYCoord.getText()) };
				assignments.add(new DeliveryAssignment("Foo company", deliveryCoords, null, null));
				logger.log(Level.INFO, String.format("New delivery added with coordinates (%f,%f)", deliveryCoords[0],
						deliveryCoords[1]));
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Formato coordenate errato", "Errore", JOptionPane.ERROR_MESSAGE);
			}
		} else if (cmd.equals("add drone")) {
			try {
				float[] droneCoords = { Float.parseFloat(droneXCoord.getText()),
						Float.parseFloat(droneYCoord.getText()) };
				drones.add(new DeliveryDrone(droneCoords));
				logger.log(Level.INFO,
						String.format("New drone added with coordinates (%f,%f)", droneCoords[0], droneCoords[1]));
				drones.get(drones.size() - 1).setSerialNumber(String.valueOf(drones.size() - 1));
				;
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Formato coordenate errato", "Errore", JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	public void serverStartedState() {
		serverStartButton.setEnabled(false);
		serverStopButton.setEnabled(true);
		serverPortField.setEditable(false);
		droneAddButton.setEnabled(false);
		droneXCoord.setText("");
		droneXCoord.setEditable(false);
		droneYCoord.setText("");
		droneYCoord.setEditable(false);
		deliveryAddButton.setEnabled(false);
		deliveryXCoord.setText("");
		deliveryXCoord.setEditable(false);
		deliveryYCoord.setText("");
		deliveryYCoord.setEditable(false);
		serverStatusLabel.setText("Online");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	public void serverStoppedState() {
		serverStartButton.setEnabled(true);
		serverStopButton.setEnabled(false);
		droneAddButton.setEnabled(true);
		deliveryAddButton.setEnabled(true);
		droneXCoord.setText("");
		droneXCoord.setEditable(true);
		droneYCoord.setText("");
		droneYCoord.setEditable(true);
		deliveryXCoord.setText("");
		deliveryXCoord.setEditable(true);
		deliveryYCoord.setText("");
		deliveryYCoord.setEditable(true);
		serverStatusLabel.setText("Offline");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	@Override
	public void dispose() {
		if (JOptionPane.showConfirmDialog(null,
				new JLabel("Il server è avviato. Sei sicuro di chiudere questa finestra?"
						+ " Gestiremo l'arresto del server per te se fai clic su \"OK\"."),
				"Confirm exit", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			try {
				serverThread.stop();
				serverExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);

				serverStoppedState();
				super.dispose();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			System.exit(0);
		}
	}

	/**
	 * Implementation of a {@code DocumentFilter} that truncates the text area text
	 * to keep it within a fixed amount of lines.
	 */
	private class LogDocumentFilter extends DocumentFilter {
		private int maxLines;

		LogDocumentFilter(int maxLines) {
			this.maxLines = maxLines;
		}

		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
				throws BadLocationException {
			super.insertString(fb, offset, string, attr);
			truncate(fb);
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
				throws BadLocationException {
			super.replace(fb, offset, length, text, attrs);
			truncate(fb);
		}

		private void truncate(FilterBypass fb) throws BadLocationException {
			int lines = logArea.getLineCount();
			if (lines > maxLines) {
				int linesToRemove = lines - maxLines - 1;
				int lengthToRemove = logArea.getLineStartOffset(linesToRemove);
				remove(fb, 0, lengthToRemove);
			}
		}
	}

	/**
	 * Implementation of {@code Handler} that publishes records into a text area.
	 */
	private class LogHandler extends Handler {
		LogHandler() {
			setFormatter(new SimpleFormatter());
			setLevel(Level.ALL);
		}

		@Override
		public void publish(LogRecord record) {
			if (!isLoggable(record))
				return;
			String msg = getFormatter().format(record);
			SwingUtilities.invokeLater(() -> {
				logArea.append(msg);
				logArea.setCaretPosition(logArea.getText().length());
			});
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() throws SecurityException {
		}
	}

}
