
import java.lang.Math;

public class GenericDrone {
	private String serialNumber;
	private String brand;
	private float[] position = new float[2];
	private String state = "stationary";

	public GenericDrone(float[] position, String brand, String serialNumber) {
		this.setPosition(position);
		this.setBrand(brand);
		this.setSerialNumber(serialNumber);
	}

	public GenericDrone(float[] position, String brand) {
		this.setPosition(position);
		this.setBrand(brand);
		this.setSerialNumber("0");
	}

	public GenericDrone(float[] position) {
		this.setPosition(position);
		this.setBrand("FOO DRON COMPANY");
		// TODO: I should implement a configurable file from which the default brand is
		// read
		this.setSerialNumber("0");
	}

	public float[] getPosition() {
		return position;
	}

	public void setPosition(float[] position) {
		this.position = position;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getInfo() {
		return "drone " + getSerialNumber() + " of brand " + getBrand() + " is at position ("
				+ Float.toString(getPosition()[0]) + ", " + Float.toString(getPosition()[1]) + ") in state "
				+ getState();
	}

	public float distanceTo(float[] position) {
		return (float) Math
				.sqrt(Math.pow(this.position[2] - position[0], 2) + Math.pow(this.position[1] - position[1], 2));
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
