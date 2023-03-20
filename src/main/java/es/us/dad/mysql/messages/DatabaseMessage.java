package es.us.dad.mysql.messages;

import com.google.gson.Gson;

public class DatabaseMessage {

	private transient Gson gson = new Gson();

	private DatabaseMessageType type;
	private DatabaseEntity entity;
	private DatabaseMethod method;
	private String requestBody;
	private String responseBody;

	public DatabaseMessage() {
		super();
	}

	public DatabaseMessage(DatabaseMessageType type, DatabaseEntity entity, DatabaseMethod method, String requestBody) {
		super();
		this.type = type;
		this.entity = entity;
		this.method = method;
		this.requestBody = requestBody;
	}

	public DatabaseMessage(DatabaseMessageType type, DatabaseEntity entity, DatabaseMethod method, Object requestBody) {
		super();
		this.type = type;
		this.entity = entity;
		this.method = method;
		this.requestBody = gson.toJson(requestBody);
	}

	public DatabaseMessage(DatabaseMessageType type, DatabaseEntity entity, DatabaseMethod method, String requestBody,
			String responseBody) {
		super();
		this.type = type;
		this.entity = entity;
		this.method = method;
		this.requestBody = requestBody;
		this.responseBody = responseBody;
	}

	public DatabaseMessage(DatabaseMessageType type, DatabaseEntity entity, DatabaseMethod method, Object requestBody,
			Object responseBody) {
		super();
		this.type = type;
		this.entity = entity;
		this.method = method;
		this.requestBody = requestBody != null ? gson.toJson(requestBody) : null;
		this.responseBody = responseBody != null ? gson.toJson(responseBody) : null;
	}

	public DatabaseMessageType getType() {
		return type;
	}

	public void setType(DatabaseMessageType type) {
		this.type = type;
	}

	public DatabaseEntity getEntity() {
		return entity;
	}

	public void setEntity(DatabaseEntity entity) {
		this.entity = entity;
	}

	public DatabaseMethod getMethod() {
		return method;
	}

	public void setMethod(DatabaseMethod method) {
		this.method = method;
	}

	public String getRequestBody() {
		return requestBody;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public <E> E getRequestBodyAs(Class<E> type) {
		return requestBody != null ? gson.fromJson(requestBody, type) : null;
	}

	public <E> E getResponseBodyAs(Class<E> type) {
		return responseBody != null ? gson.fromJson(responseBody, type) : null;
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	public <E> void setRequestBody(E requestBody) {
		this.requestBody = requestBody != null ? gson.toJson(requestBody) : null;
	}

	public <E> void setResponseBody(E responseBody) {
		this.responseBody = responseBody != null ? gson.toJson(responseBody) : null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((requestBody == null) ? 0 : requestBody.hashCode());
		result = prime * result + ((responseBody == null) ? 0 : responseBody.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		DatabaseMessage other = (DatabaseMessage) obj;
		if (entity != other.entity)
			return false;
		if (method != other.method)
			return false;
		if (requestBody == null) {
			if (other.requestBody != null)
				return false;
		} else if (!requestBody.equals(other.requestBody))
			return false;
		if (responseBody == null) {
			if (other.responseBody != null)
				return false;
		} else if (!responseBody.equals(other.responseBody))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DatabaseMessage [type=" + type + ", entity=" + entity + ", method=" + method + ", requestBody="
				+ requestBody + ", responseBody=" + responseBody + "]";
	}
}
