package es.us.dad.mysql.messages;

import es.us.dad.mysql.entities.SensorType;

public class DatabaseMessageIdAndSensorType {

	private int id;
	private SensorType sensorType;

	public DatabaseMessageIdAndSensorType() {
		super();
	}

	public DatabaseMessageIdAndSensorType(int id, SensorType sensorType) {
		super();
		this.id = id;
		this.sensorType = sensorType;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public SensorType getSensorType() {
		return sensorType;
	}

	public String getSensorTypeAsString() {
		return sensorType.name();
	}

	public void setSensorType(SensorType sensorType) {
		this.sensorType = sensorType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((sensorType == null) ? 0 : sensorType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DatabaseMessageIdAndSensorType other = (DatabaseMessageIdAndSensorType) obj;
		if (id != other.id)
			return false;
		if (sensorType != other.sensorType)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DatabaseMessageIdAndSensorType" + " [id=" + id + ", sensorType=" + sensorType + "]";
	}

}
