import java.lang.System;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

public class AssignmentThread implements Runnable {
	private ArrayList<DeliveryAssignment> assignments;
	private ArrayList<DeliveryAssignment> doneAssignments = new ArrayList<DeliveryAssignment>();
	private ArrayList<DeliveryDrone> drones;
	private MonitoringThread monitoringThread;
	private boolean running = false;
	private long lastChecked = System.currentTimeMillis();
	private long lastTimeBussy = 0;
	private final long waitTime = 60000;
	private final long sleepTime = 100;
	private static final Logger logger = Logger.getLogger(AssignmentThread.class.getName());
	private NumberFormat positionFormater;

	public AssignmentThread(MonitoringThread monitoringThread, ArrayList<DeliveryAssignment> assignments,
			ArrayList<DeliveryDrone> drones) {
		this.assignments = assignments;
		this.drones = drones;
		this.monitoringThread = monitoringThread;
		this.positionFormater = NumberFormat.getNumberInstance();
		positionFormater.setMinimumFractionDigits(5);
		positionFormater.setMaximumFractionDigits(5);
		logger.info("I WORK");
	}

	class SorterByDistance implements Comparator<DeliveryDrone> {

		float[] position = new float[2];

		public SorterByDistance(float[] position) {
			this.position = position;
		}

		public int compare(DeliveryDrone a, DeliveryDrone b) {
			return (int) (100000000 * (a.distanceTo(position) - b.distanceTo(position)));
		}
	}

	public ArrayList<DeliveryDrone> sortDronesByDistance(float[] position) {
		ArrayList<DeliveryDrone> sortedDrones = new ArrayList<DeliveryDrone>();
		sortedDrones.addAll(drones);
		Collections.sort(sortedDrones, new SorterByDistance(position));
		return sortedDrones;
	}

	public void update() {
		for (DeliveryAssignment delivery : assignments) {
			if (delivery.getAssignedDrone() != null && !doneAssignments.contains(delivery)) {
				if (delivery.getAssignedDrone().getState() == "delivery") {
					logger.info("Drone " + delivery.getAssignedDrone().getSerialNumber() + " returning to base");
					delivery.setDeliveryPosition(null);
					delivery.getAssignedDrone().setState("flying");
				} else if (delivery.getAssignedDrone().getState() == "flying") {
					if (delivery.getDeliveryPosition() != null) {
						if (comparePositions(delivery.getDeliveryPosition(),
								monitoringThread.queryPosition(delivery.getAssignedDrone().getSerialNumber()))) {
							logger.info(
									"Drone " + delivery.getAssignedDrone().getSerialNumber() + " going to delivery");
							delivery.getAssignedDrone().setState("delivery");
						} else {
							monitoringThread.updatePosition(delivery.getAssignedDrone().getSerialNumber(),
									delivery.getDeliveryPosition());
						}
					} else if (delivery.getReturnPosition() != null) {
						if (comparePositions(delivery.getReturnPosition(),
								monitoringThread.queryPosition(delivery.getAssignedDrone().getSerialNumber()))) {
							logger.info("Drone " + delivery.getAssignedDrone().getSerialNumber()
									+ " landing at return position");
							delivery.setReturnPosition(null);
						} else {
							monitoringThread.updatePosition(delivery.getAssignedDrone().getSerialNumber(),
									delivery.getReturnPosition());
						}
					} else {
						delivery.getAssignedDrone().setState("stationary");
						logger.info("Drone " + delivery.getAssignedDrone().getSerialNumber()
								+ " landed and ready for a new delivery");
					}
				} else if (delivery.getAssignedDrone().getState() == "stationary") {
					doneAssignments.add(delivery);
				}
				sleepToBeReadable();
			}
		}
		assignDrones();
		if (assignments.size() == doneAssignments.size()) {
			logger.info("ALL TASKS COMPLETED, INTERRUPTING...");
			stop();
		}
	}

	public void assignDrones() {
		lastChecked = System.currentTimeMillis();
		if ((lastChecked - lastTimeBussy) < waitTime || assignments.isEmpty()) {
			return;
		}
		logger.info("Trying to assign deliveries to drones");
		boolean allAssigned = true;
		for (DeliveryAssignment delivery : assignments) {
			if (delivery.getAssignedDrone() != null || delivery.getDeliveryPosition() == null) {
				continue;
			}
			for (DeliveryDrone drone : drones) {
				if (drone.getState() == "stationary") {
					String returnCoords = JOptionPane.showInputDialog(
							"Inserire coordenade di ritorno separate per comma. Lascire vuoto per saltare questo drone");
					String[] returnCoordsSeparated = returnCoords.split(",");
					if (returnCoords == "") {
						logger.info("Skipping drone " + drone.getSerialNumber() + " for this delivery");
					} else if (returnCoordsSeparated.length != 2) {
						JOptionPane.showMessageDialog(null, "Formato coordenate errato, skipping this drone", "Errore",
								JOptionPane.ERROR_MESSAGE);
					} else {
						try {
							float[] floatReturnCoords = { Float.parseFloat(returnCoordsSeparated[0]),
									Float.parseFloat(returnCoordsSeparated[1]) };
							delivery.setReturnPosition(floatReturnCoords);
							delivery.setAssignedDrone(drone);
							delivery.getAssignedDrone().setState("flying");
							logger.log(Level.INFO,
									String.format("New assignement added to drone %s with return coordinates (%f, %f)",
											drone.getSerialNumber(), floatReturnCoords[0], floatReturnCoords[1]));
							logger.info("Drone " + drone.getSerialNumber() + " begins to fly");
							break;
						} catch (NumberFormatException ex) {
							JOptionPane.showMessageDialog(null, "Formato coordenate errato", "Errore",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				allAssigned = false;
			}

		}
		if (!allAssigned) {
			lastTimeBussy = System.currentTimeMillis();
			logger.info("Some deliveries were unatended, waiting " + String.valueOf(waitTime)
					+ " milliseconds until next try.");
		}
	}

	public String positionToFormatedString(float[] position) {
		return positionFormater.format(position[0]) + positionFormater.format(position[1]);
	}

	public boolean comparePositions(float[] pos1, float[] pos2) {
		return positionToFormatedString(pos1).equals(positionToFormatedString(pos2));
	}

	@Override
	public void run() {
		running = true;
		while (running) {
			update();
		}
	}

	public void sleepToBeReadable() {
		try {
			TimeUnit.MILLISECONDS.sleep(sleepTime);
		} catch (InterruptedException e) {
			stop();
		}
	}

	public void stop() {
		running = false;
		monitoringThread.interrupt();
	}
}
