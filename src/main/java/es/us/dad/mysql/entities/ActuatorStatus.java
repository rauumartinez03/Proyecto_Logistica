package es.us.dad.mysql.entities;

public class ActuatorStatus {

	private Integer idActuatorState;
	private Float status;
	private Boolean statusBinary;
	private Integer idActuator;
	private Long timestamp;
	private Boolean removed;

	public ActuatorStatus() {
		super();
	}

	public ActuatorStatus(Float status, Boolean statusBinary, Integer idActuator, Long timestamp, Boolean removed) {
		super();
		this.status = status;
		this.idActuator = idActuator;
		this.removed = removed;
		this.timestamp = timestamp;
		this.statusBinary = statusBinary;
	}

	public ActuatorStatus(Integer idActuatorState, Float status, Boolean statusBinary, Integer idActuator,
			Long timestamp, Boolean removed) {
		super();
		this.idActuatorState = idActuatorState;
		this.status = status;
		this.idActuator = idActuator;
		this.removed = removed;
		this.timestamp = timestamp;
		this.statusBinary = statusBinary;
	}

	public Float getStatus() {
		return status;
	}

	public void setStatus(Float status) {
		this.status = status;
	}

	public Boolean isStatusBinary() {
		return statusBinary;
	}

	public void setStatusBinary(Boolean statusBinary) {
		this.statusBinary = statusBinary;
	}

	public Integer getIdActuator() {
		return idActuator;
	}

	public void setIdActuator(Integer idActuator) {
		this.idActuator = idActuator;
	}

	public Boolean isRemoved() {
		return removed;
	}

	public void setRemoved(Boolean removed) {
		this.removed = removed;
	}

	public Integer getIdActuatorState() {
		return idActuatorState;
	}

	public void setIdActuatorState(Integer idActuatorState) {
		this.idActuatorState = idActuatorState;
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
		result = prime * result + ((idActuator == null) ? 0 : idActuator.hashCode());
		result = prime * result + ((idActuatorState == null) ? 0 : idActuatorState.hashCode());
		result = prime * result + ((removed == null) ? 0 : removed.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((statusBinary == null) ? 0 : statusBinary.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
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
		ActuatorStatus other = (ActuatorStatus) obj;
		if (idActuator == null) {
			if (other.idActuator != null)
				return false;
		} else if (!idActuator.equals(other.idActuator))
			return false;
		if (idActuatorState == null) {
			if (other.idActuatorState != null)
				return false;
		} else if (!idActuatorState.equals(other.idActuatorState))
			return false;
		if (removed == null) {
			if (other.removed != null)
				return false;
		} else if (!removed.equals(other.removed))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (statusBinary == null) {
			if (other.statusBinary != null)
				return false;
		} else if (!statusBinary.equals(other.statusBinary))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ActuatorState [idActuatorState=" + idActuatorState + ", status=" + status + ", statusBinary="
				+ statusBinary + ", idActuator=" + idActuator + ", timestamp=" + timestamp + ", removed=" + removed
				+ "]";
	}

	public boolean equalsWithNoIdConsidered(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActuatorStatus other = (ActuatorStatus) obj;
		if (idActuator == null) {
			if (other.idActuator != null)
				return false;
		} else if (!idActuator.equals(other.idActuator))
			return false;
		if (removed == null) {
			if (other.removed != null)
				return false;
		} else if (!removed.equals(other.removed))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (statusBinary == null) {
			if (other.statusBinary != null)
				return false;
		} else if (!statusBinary.equals(other.statusBinary))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}

}
