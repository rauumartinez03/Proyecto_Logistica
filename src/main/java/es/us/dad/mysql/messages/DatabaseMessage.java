package es.us.dad.mysql.messages;

import com.google.gson.Gson;

/**
 * An entity that models the content of the messages that are exchanged between
 * the Rest API and the controller, as well as between the controller and the
 * data access layer. These messages allow implementing the asynchronous
 * communication on which Vertx is based to communicate the different Verticles
 * deployed in the solution. The messages are self-contained, so with the
 * information contained in them the operation to be performed can be
 * determined, as well as the input parameters and the result obtained once
 * applied.
 * 
 * @author luismi
 *
 */
public class DatabaseMessage {

	private transient Gson gson = new Gson();

	/**
	 * An enumerator indicating the database operation profile. Identifies the type
	 * of verb of said operation following the CRUD nomenclature.
	 */
	private DatabaseMessageType type;

	/**
	 * Class defining the enumeration corresponding to the different entities stored
	 * in the database and managed by the system. It is also used to obtain the name
	 * of the channel on which the messages coming from the controller will be
	 * published and as a recipient the Verticle for database access management.
	 */
	private DatabaseEntity entity;

	/**
	 * This enum describes the operation that must be applied.
	 */
	private DatabaseMethod method;

	/**
	 * Property containing the request body. Sometimes this content will be a
	 * primitive type converted to String (number, boolean, etc.) or it may also
	 * contain a serialization of a more complex object, which must be deserialized
	 * by the recipient of the message to process the request.
	 */
	private String requestBody;

	/**
	 * Property containing the result of the operation performed. Sometimes this
	 * content will be a primitive type converted to String (number, boolean, etc.)
	 * or it may also contain a serialization of a more complex object, which must
	 * be deserialized by the sender to process the response.
	 */
	private String responseBody;

	/**
	 * Recipient's status code when processing the message. A 20X code indicates
	 * that everything has worked correctly. A 30X, 40X, or 50X code indicates an
	 * error in the processing of the operation.
	 * 
	 */
	private Integer statusCode;

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
			String responseBody, Integer statusCode) {
		super();
		this.type = type;
		this.entity = entity;
		this.method = method;
		this.requestBody = requestBody;
		this.responseBody = responseBody;
		this.statusCode = statusCode;
	}

	public DatabaseMessage(DatabaseMessageType type, DatabaseEntity entity, DatabaseMethod method, Object requestBody,
			Object responseBody, Integer statusCode) {
		super();
		this.type = type;
		this.entity = entity;
		this.method = method;
		this.requestBody = requestBody != null ? gson.toJson(requestBody) : null;
		this.responseBody = responseBody != null ? gson.toJson(responseBody) : null;
		this.statusCode = statusCode;
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

	public Integer getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
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
		result = prime * result + ((statusCode == null) ? 0 : statusCode.hashCode());
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
		if (statusCode == null) {
			if (other.statusCode != null)
				return false;
		} else if (!statusCode.equals(other.statusCode))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DatabaseMessage [type=" + type + ", entity=" + entity + ", method=" + method + ", requestBody="
				+ requestBody + ", responseBody=" + responseBody + ", statusCode=" + statusCode + "]";
	}

}
