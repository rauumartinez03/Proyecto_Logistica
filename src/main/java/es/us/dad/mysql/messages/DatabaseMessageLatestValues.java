package es.us.dad.mysql.messages;

public class DatabaseMessageLatestValues {

	private int id;
	private int limit;

	public DatabaseMessageLatestValues() {
		super();
	}

	public DatabaseMessageLatestValues(int id, int limit) {
		super();
		this.id = id;
		this.limit = limit;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + limit;
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
		DatabaseMessageLatestValues other = (DatabaseMessageLatestValues) obj;
		if (id != other.id)
			return false;
		if (limit != other.limit)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DatabaseMessageIdAndSensorType [id=" + id + ", limit=" + limit + "]";
	}

}
