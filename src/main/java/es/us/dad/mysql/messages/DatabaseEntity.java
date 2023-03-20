package es.us.dad.mysql.messages;

public enum DatabaseEntity {
	Sensor("Sensor"), Actuator("Actuator"), Group("Group"), Device("Device"), SensorValue("SensorValue"),
	ActuatorStatus("ActuatorStatus");

	private final String value;
	private final String address;

	private DatabaseEntity(String value) {
		this.value = value;
		this.address = value;
	}

	public String getDatabaseEntity() {
		return value;
	}

	public String getAddress() {
		return address;
	}
}
