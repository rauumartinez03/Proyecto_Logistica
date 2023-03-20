package es.us.dad.mysql.rest;

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
