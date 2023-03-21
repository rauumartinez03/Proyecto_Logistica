package es.us.dad.mysql.entities;

/**
 * Representation of the Actuator entity. An actuator will be associated to a
 * device through the device id. Said actuator will be associated with its
 * states defined through the ActuatorStatus entity.
 * 
 * @author luismi
 *
 */
public class Actuator {

	/**
	 * Actuator name. It can be used to differentiate between different actuators
	 * associated with the same device.
	 */
	private String name;

	/**
	 * Identifier that represents the actuator and serves as the primary key. Each
	 * actuator associated with a plate will have a unique identifier in the
	 * database.
	 */
	private Integer idActuator;

	/**
	 * Device identifier to which the actuator is linked
	 */
	private Integer idDevice;

	/**
	 * Actuator type that will be defined through the ActuatorType enumerator
	 */
	private ActuatorType actuatorType;

	/**
	 * Logical value indicating if the actuator has been removed. The elimination is
	 * done by marking this field to true, so the value is never permanently
	 * eliminated from the database.
	 */
	private Boolean removed;

	public Actuator() {
		super();
	}

	public Actuator(String name, Integer idDevice, ActuatorType actuatorType, Boolean removed) {
		super();
		this.name = name;
		this.idDevice = idDevice;
		this.actuatorType = actuatorType;
		this.removed = removed;
	}

	public Actuator(String name, Integer idDevice, String actuatorType, Boolean removed) {
		super();
		this.name = name;
		this.idDevice = idDevice;
		this.actuatorType = ActuatorType.valueOf(actuatorType);
		this.removed = removed;
	}

	public Actuator(Integer idActuator, String name, Integer idDevice, ActuatorType actuatorType, Boolean removed) {
		super();
		this.name = name;
		this.idActuator = idActuator;
		this.idDevice = idDevice;
		this.actuatorType = actuatorType;
		this.removed = removed;
	}

	public Actuator(Integer idActuator, String name, Integer idDevice, String actuatorType, Boolean removed) {
		super();
		this.name = name;
		this.idActuator = idActuator;
		this.idDevice = idDevice;
		this.actuatorType = ActuatorType.valueOf(actuatorType);
		this.removed = removed;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getIdActuator() {
		return idActuator;
	}

	public void setIdActuator(Integer idActuator) {
		this.idActuator = idActuator;
	}

	public Integer getIdDevice() {
		return idDevice;
	}

	public void setIdDevice(Integer idDevice) {
		this.idDevice = idDevice;
	}

	public ActuatorType getActuatorType() {
		return actuatorType;
	}

	public void setActuatorType(ActuatorType actuatorType) {
		this.actuatorType = actuatorType;
	}

	public Boolean isRemoved() {
		return removed;
	}

	public void setRemoved(Boolean removed) {
		this.removed = removed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actuatorType == null) ? 0 : actuatorType.hashCode());
		result = prime * result + ((idActuator == null) ? 0 : idActuator.hashCode());
		result = prime * result + ((idDevice == null) ? 0 : idDevice.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((removed == null) ? 0 : removed.hashCode());
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
		Actuator other = (Actuator) obj;
		if (actuatorType != other.actuatorType)
			return false;
		if (idActuator == null) {
			if (other.idActuator != null)
				return false;
		} else if (!idActuator.equals(other.idActuator))
			return false;
		if (idDevice == null) {
			if (other.idDevice != null)
				return false;
		} else if (!idDevice.equals(other.idDevice))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (removed == null) {
			if (other.removed != null)
				return false;
		} else if (!removed.equals(other.removed))
			return false;
		return true;
	}

	public boolean equalsWithNoIdConsidered(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Actuator other = (Actuator) obj;
		if (actuatorType != other.actuatorType)
			return false;
		if (idDevice == null) {
			if (other.idDevice != null)
				return false;
		} else if (!idDevice.equals(other.idDevice))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (removed == null) {
			if (other.removed != null)
				return false;
		} else if (!removed.equals(other.removed))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Actuator [name=" + name + ", idActuator=" + idActuator + ", idDevice=" + idDevice + ", actuatorType="
				+ actuatorType + ", removed=" + removed + "]";
	}

}
