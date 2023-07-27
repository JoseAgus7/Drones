class DeliveryDrone extends GenericDrone {
	float maxPayload;

	public DeliveryDrone(float[] position, float maxPayload, String brand, String serialNumber) {
		super(position, brand, serialNumber);
		this.setMaxPayload(maxPayload);
	}

	public DeliveryDrone(float[] position, float maxPayload, String brand) {
		super(position, brand);
		this.setMaxPayload(maxPayload);
	}

	public DeliveryDrone(float[] position, float maxPayload) {
		super(position);
		this.setMaxPayload(maxPayload);
	}

	public DeliveryDrone(float[] position) {
		super(position);
		this.setMaxPayload(2.5f);
		// TODO: I should implement a configurable file from which the default maximum
		// payload is read
	}

	public void setMaxPayload(float maxPayload) {
		this.maxPayload = maxPayload;
	}

	public float getMaxPayload() {
		return maxPayload;
	}

	public void performNavigation(float[] position) {
		// the details are uninteresting
	}
}
