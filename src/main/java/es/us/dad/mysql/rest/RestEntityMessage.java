package es.us.dad.mysql.rest;

/**
 * An enum that represents the entity associated with the message that the Rest
 * API exchanges with the controller. This entity differs from the one used
 * between the controller and the database to avoid conflicts between the
 * messages exchanged by both elements.
 * 
 * @author luismi
 *
 */
public enum RestEntityMessage {
	Sensor("SensorRest"), Actuator("ActuatorRest"), Group("GroupRest"), Device("DeviceRest"),
	SensorValue("SensorValueRest"), ActuatorStatus("ActuatorStatusRest");

	private final String value;
	private final String address;

	private RestEntityMessage(String value) {
		this.value = value;
		this.address = value;
	}

	public String getRestEntityMessage() {
		return value;
	}

	public String getAddress() {
		return address;
	}
}
