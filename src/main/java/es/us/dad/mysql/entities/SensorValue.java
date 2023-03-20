package es.us.dad.mysql.entities;

public class SensorValue {

	private Integer idSensorValue;
	private Float value;
	private Integer idSensor;
	private Long timestamp;
	private Boolean removed;

	public SensorValue() {
		super();
	}

	public SensorValue(Float value, Integer idSensor, Long timestamp, Boolean removed) {
		super();
		this.value = value;
		this.idSensor = idSensor;
		this.removed = removed;
		this.timestamp = timestamp;
	}

	public SensorValue(Integer idSensorValue, Float value, Integer idSensor, Long timestamp, Boolean removed) {
		super();
		this.idSensorValue = idSensorValue;
		this.value = value;
		this.idSensor = idSensor;
		this.removed = removed;
		this.timestamp = timestamp;
	}

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	public Integer getIdSensor() {
		return idSensor;
	}

	public void setIdSensor(Integer idSensor) {
		this.idSensor = idSensor;
	}

	public Boolean isRemoved() {
		return removed;
	}

	public void setRemoved(Boolean removed) {
		this.removed = removed;
	}

	public Integer getIdSensorValue() {
		return idSensorValue;
	}

	public void setIdSensorValue(Integer idSensorValue) {
		this.idSensorValue = idSensorValue;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idSensor == null) ? 0 : idSensor.hashCode());
		result = prime * result + ((idSensorValue == null) ? 0 : idSensorValue.hashCode());
		result = prime * result + ((removed == null) ? 0 : removed.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		SensorValue other = (SensorValue) obj;
		if (idSensor == null) {
			if (other.idSensor != null)
				return false;
		} else if (!idSensor.equals(other.idSensor))
			return false;
		if (idSensorValue == null) {
			if (other.idSensorValue != null)
				return false;
		} else if (!idSensorValue.equals(other.idSensorValue))
			return false;
		if (removed == null) {
			if (other.removed != null)
				return false;
		} else if (!removed.equals(other.removed))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SensorValue [idSensorValue=" + idSensorValue + ", value=" + value + ", idSensor=" + idSensor
				+ ", timestamp=" + timestamp + ", removed=" + removed + "]";
	}

	public boolean equalsWithNoIdConsidered(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SensorValue other = (SensorValue) obj;
		if (idSensor == null) {
			if (other.idSensor != null)
				return false;
		} else if (!idSensor.equals(other.idSensor))
			return false;
		if (removed == null) {
			if (other.removed != null)
				return false;
		} else if (!removed.equals(other.removed))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
