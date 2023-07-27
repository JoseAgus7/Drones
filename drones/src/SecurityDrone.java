class SecurityDrone extends GenericDrone {
	private EventAssignment event;
	private int noCameras;

	public SecurityDrone(float[] position, EventAssignment event, int noCameras, String brand, String serialNumber) {
		super(position, brand, serialNumber);
		this.setEvent(event);
		this.setNoCameras(noCameras);
	}

	public SecurityDrone(float[] position, EventAssignment event, int noCameras, String brand) {
		super(position, brand);
		this.setEvent(event);
		this.setNoCameras(noCameras);
	}

	public SecurityDrone(float[] position, EventAssignment event, int noCameras) {
		super(position);
		this.setEvent(event);
		this.setNoCameras(noCameras);
	}

	public SecurityDrone(float[] position, EventAssignment event) {
		super(position);
		this.setEvent(event);
		this.setNoCameras(1);
	}

	public EventAssignment getEvent() {
		return event;
	}

	public void setEvent(EventAssignment event) {
		this.event = event;
	}

	public int getNoCameras() {
		return noCameras;
	}

	public void setNoCameras(int noCameras) {
		this.noCameras = noCameras;
	}
}
