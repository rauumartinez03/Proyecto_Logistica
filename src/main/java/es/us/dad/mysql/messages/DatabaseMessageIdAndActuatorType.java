package es.us.dad.mysql.messages;

import es.us.dad.mysql.entities.ActuatorType;

public class DatabaseMessageIdAndActuatorType {

	private int id;
	private ActuatorType actuatorType;

	public DatabaseMessageIdAndActuatorType() {
		super();
	}

	public DatabaseMessageIdAndActuatorType(int id, ActuatorType actuatorType) {
		super();
		this.id = id;
		this.actuatorType = actuatorType;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ActuatorType getActuatorType() {
		return actuatorType;
	}

	public String getActuatorTypeAsString() {
		return actuatorType.name();
	}

	public void setActuatorType(ActuatorType actuatorType) {
		this.actuatorType = actuatorType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((actuatorType == null) ? 0 : actuatorType.hashCode());
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
		DatabaseMessageIdAndActuatorType other = (DatabaseMessageIdAndActuatorType) obj;
		if (id != other.id)
			return false;
		if (actuatorType != other.actuatorType)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DatabaseMessageIdAndActuatorType [id=" + id + ", sensorType=" + actuatorType + "]";
	}

}
