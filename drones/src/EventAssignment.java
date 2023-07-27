import java.util.Date;

public class EventAssignment {
	String name;
	Date date;
	SecurityDrone[] assignedDrones;
	String role;

	public EventAssignment(String name, Date date, SecurityDrone[] assignedDrones, String role) {
		this.name = name;
		this.date = date;
		this.assignedDrones = assignedDrones;
		this.role = role;
	}
}
