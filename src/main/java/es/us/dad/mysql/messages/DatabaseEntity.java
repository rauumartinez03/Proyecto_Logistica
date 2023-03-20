package es.us.dad.mysql.messages;

/**
 * Class defining the enumeration corresponding to the different entities stored
 * in the database and managed by the system. It is also used to obtain the name
 * of the channel on which the messages coming from the controller will be
 * published and as a recipient the Verticle for database access management.
 * 
 * @author luismi
 *
 */
public enum DatabaseEntity {
	Sensor("Sensor"), Actuator("Actuator"), Group("Group"), Device("Device"), SensorValue("SensorValue"),
	ActuatorStatus("ActuatorStatus");

	/**
	 * Textual value associated with the value listed
	 */
	private final String value;

	/**
	 * Text value of the address of the channel where the messages associated with
	 * each listing will be published.
	 */
	private final String address;

	/**
	 * Constructor of the list that allows defining the textual value of the
	 * publication channel and the representation of each list
	 * 
	 * @param value Textual representation of the associated value
	 */
	private DatabaseEntity(String value) {
		this.value = value;
		this.address = value;
	}

	public String getDatabaseEntity() {
		return value;
	}

	/**
	 * Publication channel for messages associated with this entity
	 * 
	 * @return
	 */
	public String getAddress() {
		return address;
	}
}
