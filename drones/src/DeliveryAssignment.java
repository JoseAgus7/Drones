public class DeliveryAssignment {
	private String adress;
	private float[] deliveryPosition = new float[2];
	private float[] returnPosition = new float[2];
	private DeliveryDrone assignedDrone;
	private boolean solved = false;

	public DeliveryAssignment(String adress, float[] deliveryPosition, float[] returnPosition,
			DeliveryDrone assignedDrone) {
		this.adress = adress;
		this.deliveryPosition = deliveryPosition;
		this.returnPosition = returnPosition;
		this.assignedDrone = assignedDrone;
	}

	public void setAssignedDrone(DeliveryDrone assignedDrone) {
		this.assignedDrone = assignedDrone;
	}

	public DeliveryDrone getAssignedDrone() {
		return assignedDrone;
	}

	public String getAdress() {
		return adress;
	}

	public void setAdress(String adress) {
		this.adress = adress;
	}

	public float[] getDeliveryPosition() {
		return deliveryPosition;
	}

	public void setDeliveryPosition(float[] deliveryPosition) {
		this.deliveryPosition = deliveryPosition;
	}

	public float[] getReturnPosition() {
		return returnPosition;
	}

	public void setReturnPosition(float[] returnPosition) {
		this.returnPosition = returnPosition;
	}

	public boolean isSolved() {
		return solved;
	}

	public void setSolved(boolean solved) {
		this.solved = solved;
	}
}
