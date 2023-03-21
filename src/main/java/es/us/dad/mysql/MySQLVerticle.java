package es.us.dad.mysql;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import es.us.dad.mysql.entities.Actuator;
import es.us.dad.mysql.entities.ActuatorStatus;
import es.us.dad.mysql.entities.Device;
import es.us.dad.mysql.entities.Group;
import es.us.dad.mysql.entities.Sensor;
import es.us.dad.mysql.entities.SensorValue;
import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
import es.us.dad.mysql.messages.DatabaseMessageIdAndActuatorType;
import es.us.dad.mysql.messages.DatabaseMessageIdAndSensorType;
import es.us.dad.mysql.messages.DatabaseMessageLatestValues;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class MySQLVerticle extends AbstractVerticle {

	protected MySQLPool mySqlClient;
	protected transient Gson gson;

	public void start(Promise<Void> startFuture) {
		MySQLConnectOptions connectOptions = new MySQLConnectOptions().setPort(3306).setHost("localhost")
				.setDatabase("dad_database").setUser("root").setPassword("rootroot");

		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

		mySqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);

		gson = new Gson();

		getVertx().eventBus().consumer(DatabaseEntity.Group.getAddress(), message -> {
			DatabaseMessage databaseMessage = gson.fromJson((String) message.body(), DatabaseMessage.class);
			switch (databaseMessage.getMethod()) {
			case CreateGroup:
				this.createGroup(databaseMessage.getRequestBodyAs(Group.class), databaseMessage, message);
				break;
			case GetGroup:
				this.getGroup(Integer.parseInt(databaseMessage.getRequestBody()), databaseMessage, message);
				break;
			case EditGroup:
				this.editGroup(databaseMessage.getRequestBodyAs(Group.class), databaseMessage, message);
				break;
			case DeleteGroup:
				this.deleteGroup(Integer.parseInt(databaseMessage.getRequestBody()), databaseMessage, message);
				break;
			case AddDeviceToGroup:
				this.addDeviceToGroup(databaseMessage.getRequestBodyAs(Device.class), databaseMessage, message);
				break;
			case GetDevicesFromGroupId:
				this.getDevicesFromGroupId(Integer.parseInt(databaseMessage.getRequestBody()), databaseMessage,
						message);
				break;
			default:
				message.fail(401, "Method not allowed");
				break;
			}
		});

		getVertx().eventBus().consumer(DatabaseEntity.Device.getAddress(), message -> {
			DatabaseMessage databaseMessage = gson.fromJson((String) message.body(), DatabaseMessage.class);
			switch (databaseMessage.getMethod()) {
			case CreateDevice:
				this.createDevice(databaseMessage.getRequestBodyAs(Device.class), databaseMessage, message);
				break;
			case GetDevice:
				this.getDevice(Integer.parseInt(databaseMessage.getRequestBody()), databaseMessage, message);
				break;
			case EditDevice:
				this.editDevice(databaseMessage.getRequestBodyAs(Device.class), databaseMessage, message);
				break;
			case DeleteDevice:
				this.deleteDevice(Integer.parseInt(databaseMessage.getRequestBody()), databaseMessage, message);
				break;
			case GetSensorsFromDeviceId:
				this.getSensorsFromDeviceId(Integer.parseInt(databaseMessage.getRequestBody()), databaseMessage,
						message);
				break;
			case GetActuatorsFromDeviceId:
				this.getActuatorsFromDeviceId(Integer.parseInt(databaseMessage.getRequestBody()), databaseMessage,
						message);
				break;
			case GetSensorsFromDeviceIdAndSensorType:
				this.getSensorsFromDeviceIdAndSensorType(
						databaseMessage.getRequestBodyAs(DatabaseMessageIdAndSensorType.class), databaseMessage,
						message);
				break;
			case GetActuatorsFromDeviceIdAndActuatorType:
				this.getActuatorsFromDeviceIdAndActuatorType(
						databaseMessage.getRequestBodyAs(DatabaseMessageIdAndActuatorType.class), databaseMessage,
						message);
				break;
			default:
				message.fail(401, "Method not allowed");
				break;
			}
		});

		getVertx().eventBus().consumer(DatabaseEntity.Sensor.getAddress(), message -> {
			DatabaseMessage databaseMessage = gson.fromJson((String) message.body(), DatabaseMessage.class);
			switch (databaseMessage.getMethod()) {
			case CreateSensor:
				this.createSensor(databaseMessage.getRequestBodyAs(Sensor.class), databaseMessage, message);
				break;
			case GetSensor:
				this.getSensor(Integer.parseInt(databaseMessage.getRequestBody()), databaseMessage, message);
				break;
			case EditSensor:
				this.editSensor(databaseMessage.getRequestBodyAs(Sensor.class), databaseMessage, message);
				break;
			case DeleteSensor:
				this.deleteSensor(Integer.parseInt(databaseMessage.getRequestBody()), databaseMessage, message);
				break;
			default:
				message.fail(401, "Method not allowed");
				break;
			}
		});

		getVertx().eventBus().consumer(DatabaseEntity.Actuator.getAddress(), message -> {
			DatabaseMessage databaseMessage = gson.fromJson((String) message.body(), DatabaseMessage.class);
			switch (databaseMessage.getMethod()) {
			case CreateActuator:
				this.createActuator(databaseMessage.getRequestBodyAs(Actuator.class), databaseMessage, message);
				break;
			case GetActuator:
				this.getActuator(Integer.parseInt(databaseMessage.getRequestBody()), databaseMessage, message);
				break;
			case EditActuator:
				this.editActuator(databaseMessage.getRequestBodyAs(Actuator.class), databaseMessage, message);
				break;
			case DeleteActuator:
				this.deleteActuator(Integer.parseInt(databaseMessage.getRequestBody()), databaseMessage, message);
				break;
			default:
				message.fail(401, "Method not allowed");
				break;
			}
		});

		getVertx().eventBus().consumer(DatabaseEntity.SensorValue.getAddress(), message -> {
			DatabaseMessage databaseMessage = gson.fromJson((String) message.body(), DatabaseMessage.class);
			switch (databaseMessage.getMethod()) {
			case CreateSensorValue:
				this.createSensorValue(databaseMessage.getRequestBodyAs(SensorValue.class), databaseMessage, message);
				break;
			case DeleteSensorValue:
				this.deleteSensorValue(Integer.parseInt(databaseMessage.getRequestBody()), databaseMessage, message);
				break;
			case GetLastSensorValueFromSensorId:
				this.getLastSensorValueFromSensorId(Integer.parseInt(databaseMessage.getRequestBody()), databaseMessage,
						message);
				break;
			case GetLatestSensorValuesFromSensorId:
				this.getLatestSensorValuesFromSensorId(
						databaseMessage.getRequestBodyAs(DatabaseMessageLatestValues.class), databaseMessage, message);
				break;
			default:
				message.fail(401, "Method not allowed");
				break;
			}
		});

		getVertx().eventBus().consumer(DatabaseEntity.ActuatorStatus.getAddress(), message -> {
			DatabaseMessage databaseMessage = gson.fromJson((String) message.body(), DatabaseMessage.class);
			switch (databaseMessage.getMethod()) {
			case CreateActuatorStatus:
				this.createActuatorStatus(databaseMessage.getRequestBodyAs(ActuatorStatus.class), databaseMessage,
						message);
				break;
			case DeleteActuatorStatus:
				this.deleteActuatorStatus(Integer.parseInt(databaseMessage.getRequestBody()), databaseMessage, message);
				break;
			case GetLastActuatorStatusFromActuatorId:
				this.getLastActuatorStatusFromActuatorId(Integer.parseInt(databaseMessage.getRequestBody()),
						databaseMessage, message);
				break;
			case GetLatestActuatorStatesFromActuatorId:
				this.getLatestActuatorStatesFromActuatorId(
						databaseMessage.getRequestBodyAs(DatabaseMessageLatestValues.class), databaseMessage, message);
				break;
			default:
				message.fail(401, "Method not allowed");
				break;
			}
		});
		startFuture.complete();
	}

	// ================================================================================
	// Groups
	// ================================================================================

	protected void createGroup(Group group, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery(
				"INSERT INTO dad_database.groups (name, mqttChannel, lastMessageReceived) VALUES (?,?,?);",
				Tuple.of(group.getName(), group.getMqttChannel(), group.getLastMessageReceived()), res -> {
					if (res.succeeded()) {
						long lastInsertId = res.result().property(MySQLClient.LAST_INSERTED_ID);
						group.setIdGroup((int) lastInsertId);
						databaseMessage.setResponseBody(group);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void getGroup(int idGroup, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery("SELECT * FROM dad_database.groups WHERE idGroup = ?;", Tuple.of(idGroup), res -> {
			if (res.succeeded()) {
				// Get the result set
				RowSet<Row> resultSet = res.result();
				Group group = null;
				if (resultSet.iterator().hasNext()) {
					Row elem = resultSet.iterator().next();
					group = new Group(elem.getInteger("idGroup"), elem.getString("mqttChannel"), elem.getString("name"),
							elem.getLong("lastMessageReceived"));
				}
				databaseMessage.setResponseBody(group);
				databaseMessage.setStatusCode(200);
				message.reply(gson.toJson(databaseMessage));
			} else {
				message.fail(100, res.cause().getLocalizedMessage());
				System.err.println(res.cause());
			}
		});
	}

	protected void editGroup(Group group, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery(
				"UPDATE dad_database.groups g SET name = COALESCE(?, g.name), mqttChannel = COALESCE(?, g.mqttChannel), lastMessageReceived = COALESCE(?, g.lastMessageReceived) WHERE idGroup = ?;",
				Tuple.of(group.getName(), group.getMqttChannel(), group.getLastMessageReceived(), group.getIdGroup()),
				res -> {
					if (res.succeeded()) {
						databaseMessage.setResponseBody(group);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void deleteGroup(int idGroup, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery("DELETE FROM dad_database.groups WHERE idGroup = ?;", Tuple.of(idGroup), res -> {
			if (res.succeeded()) {
				databaseMessage.setResponseBody(idGroup);
				databaseMessage.setStatusCode(200);
				message.reply(gson.toJson(databaseMessage));
			} else {
				message.fail(100, res.cause().getLocalizedMessage());
				System.err.println(res.cause());
			}
		});
	}

	protected void setDeviceToGroup(int idGroup, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery("DELETE FROM dad_database.groups WHERE idGroup = ?;", Tuple.of(idGroup), res -> {
			if (res.succeeded()) {
				databaseMessage.setResponseBody(idGroup);
				databaseMessage.setStatusCode(200);
				message.reply(gson.toJson(databaseMessage));
			} else {
				message.fail(100, res.cause().getLocalizedMessage());
				System.err.println(res.cause());
			}
		});
	}

	// ================================================================================
	// Devices
	// ================================================================================

	protected void createDevice(Device device, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery(
				"INSERT INTO dad_database.devices (deviceSerialId, name, idGroup, mqttChannel, lastTimestampSensorModified,"
						+ " lastTimestampActuatorModified) VALUES (?,?,?,?,?,?);",
				Tuple.of(device.getDeviceSerialId(), device.getName(), device.getIdGroup(), device.getMqttChannel(),
						device.getLastTimestampSensorModified(), device.getLastTimestampActuatorModified()),
				res -> {
					if (res.succeeded()) {
						long lastInsertId = res.result().property(MySQLClient.LAST_INSERTED_ID);
						device.setIdDevice((int) lastInsertId);
						databaseMessage.setResponseBody(device);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(500, "Error");
						System.err.println(res.cause());
					}
				});
	}

	protected void getDevice(int idDevice, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery("SELECT * FROM dad_database.devices WHERE idDevice = ?;", Tuple.of(idDevice), res -> {
			if (res.succeeded()) {
				// Get the result set
				RowSet<Row> resultSet = res.result();
				Device device = null;
				if (resultSet.iterator().hasNext()) {
					Row elem = resultSet.iterator().next();
					device = new Device(elem.getInteger("idDevice"), elem.getString("deviceSerialId"),
							elem.getString("name"), elem.getInteger("idGroup"), elem.getString("mqttChannel"),
							elem.getLong("lastTimestampSensorModified"), elem.getLong("lastTimestampActuatorModified"));
				}
				databaseMessage.setResponseBody(device);
				databaseMessage.setStatusCode(200);
				message.reply(gson.toJson(databaseMessage));
			} else {
				message.fail(100, res.cause().getLocalizedMessage());
				System.err.println(res.cause());
			}
		});
	}

	protected void editDevice(Device device, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery(
				"UPDATE dad_database.devices g SET deviceSerialId = COALESCE(?, g.deviceSerialId), name = COALESCE(?, g.name), idGroup = COALESCE(?, g.idGroup), mqttChannel = COALESCE(?, g.mqttChannel), lastTimestampSensorModified = COALESCE(?, g.lastTimestampSensorModified), lastTimestampActuatorModified = COALESCE(?, g.lastTimestampActuatorModified) WHERE idDevice = ?;",
				Tuple.of(device.getDeviceSerialId(), device.getName(), device.getIdGroup(), device.getMqttChannel(),
						device.getLastTimestampSensorModified(), device.getLastTimestampActuatorModified(),
						device.getIdDevice()),
				res -> {
					if (res.succeeded()) {
						databaseMessage.setResponseBody(device);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void deleteDevice(int idDevice, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery("DELETE FROM dad_database.devices WHERE idDevice = ?;", Tuple.of(idDevice), res -> {
			if (res.succeeded()) {
				databaseMessage.setResponseBody(idDevice);
				databaseMessage.setStatusCode(200);
				message.reply(gson.toJson(databaseMessage));
			} else {
				message.fail(100, res.cause().getLocalizedMessage());
				System.err.println(res.cause());
			}
		});
	}

	protected void getSensorsFromDeviceId(int idDevice, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery("SELECT * FROM dad_database.sensors WHERE idDevice = ?;", Tuple.of(idDevice), res -> {
			if (res.succeeded()) {
				// Get the result set
				RowSet<Row> resultSet = res.result();
				List<Sensor> result = new ArrayList<>();
				for (Row elem : resultSet) {
					result.add(new Sensor(elem.getInteger("idSensor"), elem.getString("name"),
							elem.getInteger("idDevice"), elem.getString("sensorType"), elem.getBoolean("removed")));
				}

				databaseMessage.setResponseBody(result);
				databaseMessage.setStatusCode(200);
				message.reply(gson.toJson(databaseMessage));
			} else {
				message.fail(100, res.cause().getLocalizedMessage());
				System.err.println(res.cause());
			}
		});
	}

	protected void getActuatorsFromDeviceId(int idDevice, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery("SELECT * FROM dad_database.actuators WHERE idDevice = ?;", Tuple.of(idDevice),
				res -> {
					if (res.succeeded()) {
						// Get the result set
						RowSet<Row> resultSet = res.result();
						List<Actuator> result = new ArrayList<>();
						for (Row elem : resultSet) {
							result.add(new Actuator(elem.getInteger("idActuator"), elem.getString("name"),
									elem.getInteger("idDevice"), elem.getString("actuatorType"),
									elem.getBoolean("removed")));
						}

						databaseMessage.setResponseBody(result);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void getSensorsFromDeviceIdAndSensorType(DatabaseMessageIdAndSensorType queryParams,
			DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery("SELECT * FROM dad_database.sensors WHERE idDevice = ? AND sensorType = ?;",
				Tuple.of(queryParams.getId(), queryParams.getSensorTypeAsString()), res -> {
					if (res.succeeded()) {
						// Get the result set
						RowSet<Row> resultSet = res.result();
						List<Sensor> result = new ArrayList<>();
						for (Row elem : resultSet) {
							result.add(new Sensor(elem.getInteger("idSensor"), elem.getString("name"),
									elem.getInteger("idDevice"), elem.getString("sensorType"),
									elem.getBoolean("removed")));
						}

						databaseMessage.setResponseBody(result);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void getActuatorsFromDeviceIdAndActuatorType(DatabaseMessageIdAndActuatorType queryParams,
			DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery("SELECT * FROM dad_database.actuators WHERE idDevice = ? AND actuatorType = ?;",
				Tuple.of(queryParams.getId(), queryParams.getActuatorTypeAsString()), res -> {
					if (res.succeeded()) {
						// Get the result set
						RowSet<Row> resultSet = res.result();
						List<Actuator> result = new ArrayList<>();
						for (Row elem : resultSet) {
							result.add(new Actuator(elem.getInteger("idActuator"), elem.getString("name"),
									elem.getInteger("idDevice"), elem.getString("actuatorType"),
									elem.getBoolean("removed")));
						}

						databaseMessage.setResponseBody(result);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void addDeviceToGroup(Device device, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery("UPDATE dad_database.devices SET idGroup = ? WHERE idDevice = ?;",
				Tuple.of(device.getIdGroup(), device.getIdDevice()), res -> {
					if (res.succeeded()) {
						databaseMessage.setResponseBody(device);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void getDevicesFromGroupId(int idGroup, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery("SELECT * FROM dad_database.devices WHERE idGroup = ?;", Tuple.of(idGroup), res -> {
			if (res.succeeded()) {
				// Get the result set
				RowSet<Row> resultSet = res.result();
				List<Device> result = new ArrayList<>();
				for (Row elem : resultSet) {
					result.add(new Device(elem.getInteger("idDevice"), elem.getString("deviceSerialId"),
							elem.getString("name"), elem.getInteger("idGroup"), elem.getString("mqttChannel"),
							elem.getLong("lastTimestampSensorModified"),
							elem.getLong("lastTimestampActuatorModified")));
				}

				databaseMessage.setResponseBody(result);
				databaseMessage.setStatusCode(200);
				message.reply(gson.toJson(databaseMessage));
			} else {
				message.fail(100, res.cause().getLocalizedMessage());
				System.err.println(res.cause());
			}
		});
	}

	// ================================================================================
	// Sensors
	// ================================================================================

	protected void createSensor(Sensor sensor, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery(
				"INSERT INTO dad_database.sensors (name, idDevice, sensorType, removed) VALUES (?,?,?,?);",
				Tuple.of(sensor.getName(), sensor.getIdDevice(), sensor.getSensorType().name(), sensor.isRemoved()),
				res -> {
					if (res.succeeded()) {
						long lastInsertId = res.result().property(MySQLClient.LAST_INSERTED_ID);
						sensor.setIdSensor((int) lastInsertId);
						databaseMessage.setResponseBody(sensor);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void getSensor(int idSensor, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery("SELECT * FROM dad_database.sensors WHERE idSensor = ?;", Tuple.of(idSensor), res -> {
			if (res.succeeded()) {
				// Get the result set
				RowSet<Row> resultSet = res.result();
				Sensor sensor = null;
				if (resultSet.iterator().hasNext()) {
					Row elem = resultSet.iterator().next();
					sensor = new Sensor(elem.getInteger("idSensor"), elem.getString("name"),
							elem.getInteger("idDevice"), elem.getString("sensorType"), elem.getBoolean("removed"));
				}
				databaseMessage.setResponseBody(sensor);
				databaseMessage.setStatusCode(200);
				message.reply(gson.toJson(databaseMessage));
			} else {
				message.fail(100, res.cause().getLocalizedMessage());
				System.err.println(res.cause());
			}
		});
	}

	protected void editSensor(Sensor sensor, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery(
				"UPDATE dad_database.sensors g SET name = COALESCE(?, g.name), idDevice = COALESCE(?, g.idDevice), sensorType = COALESCE(?, g.sensorType), removed = COALESCE(?, g.removed) WHERE idSensor = ?;",
				Tuple.of(sensor.getName(), sensor.getIdDevice(), sensor.getSensorType(), sensor.isRemoved(),
						sensor.getIdSensor()),
				res -> {
					if (res.succeeded()) {
						databaseMessage.setResponseBody(sensor);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void deleteSensor(int idSensor, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery("DELETE FROM dad_database.sensors WHERE idSensor = ?;", Tuple.of(idSensor), res -> {
			if (res.succeeded()) {
				databaseMessage.setResponseBody(idSensor);
				databaseMessage.setStatusCode(200);
				message.reply(gson.toJson(databaseMessage));
			} else {
				message.fail(100, res.cause().getLocalizedMessage());
				System.err.println(res.cause());
			}
		});
	}

	// ================================================================================
	// SensorValues
	// ================================================================================

	protected void createSensorValue(SensorValue sensorValue, DatabaseMessage databaseMessage,
			Message<Object> message) {
		mySqlClient.preparedQuery(
				"INSERT INTO dad_database.sensorValues (value, idSensor, timestamp, removed) VALUES (?,?,?,?);",
				Tuple.of(sensorValue.getValue(), sensorValue.getIdSensor(), sensorValue.getTimestamp(),
						sensorValue.isRemoved()),
				res -> {
					if (res.succeeded()) {
						long lastInsertId = res.result().property(MySQLClient.LAST_INSERTED_ID);
						sensorValue.setIdSensorValue((int) lastInsertId);
						databaseMessage.setResponseBody(sensorValue);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void deleteSensorValue(int idSensorValue, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery("DELETE FROM dad_database.sensorValues WHERE idSensorValue = ?;",
				Tuple.of(idSensorValue), res -> {
					if (res.succeeded()) {
						databaseMessage.setResponseBody(idSensorValue);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void getLastSensorValueFromSensorId(int idSensor, DatabaseMessage databaseMessage,
			Message<Object> message) {
		mySqlClient.preparedQuery(
				"SELECT * FROM dad_database.sensorValues WHERE idSensor = ? ORDER BY `timestamp` DESC LIMIT 1;",
				Tuple.of(idSensor), res -> {
					if (res.succeeded()) {
						// Get the result set
						RowSet<Row> resultSet = res.result();
						SensorValue sensorValue = null;
						if (resultSet.iterator().hasNext()) {
							Row elem = resultSet.iterator().next();
							sensorValue = new SensorValue(elem.getInteger("idSensorValue"), elem.getFloat("value"),
									elem.getInteger("idSensor"), elem.getLong("timestamp"), elem.getBoolean("removed"));
						}
						databaseMessage.setResponseBody(sensorValue);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void getLatestSensorValuesFromSensorId(DatabaseMessageLatestValues queryParam,
			DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery(
				"SELECT * FROM dad_database.sensorValues WHERE idSensor = ? ORDER BY `timestamp` DESC LIMIT ?;",
				Tuple.of(queryParam.getId(), queryParam.getLimit()), res -> {
					if (res.succeeded()) {
						// Get the result set
						RowSet<Row> resultSet = res.result();
						List<SensorValue> sensorValues = new ArrayList<SensorValue>();
						for (Row elem : resultSet) {
							sensorValues.add(new SensorValue(elem.getInteger("idSensorValue"), elem.getFloat("value"),
									elem.getInteger("idSensor"), elem.getLong("timestamp"),
									elem.getBoolean("removed")));
						}
						databaseMessage.setResponseBody(sensorValues);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	// ================================================================================
	// Actuators
	// ================================================================================

	protected void createActuator(Actuator actuator, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery(
				"INSERT INTO dad_database.actuators (name, idDevice, actuatorType, removed) VALUES (?,?,?,?);",
				Tuple.of(actuator.getName(), actuator.getIdDevice(), actuator.getActuatorType().name(),
						actuator.isRemoved()),
				res -> {
					if (res.succeeded()) {
						long lastInsertId = res.result().property(MySQLClient.LAST_INSERTED_ID);
						actuator.setIdActuator((int) lastInsertId);
						databaseMessage.setResponseBody(actuator);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void getActuator(int idActuator, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery("SELECT * FROM dad_database.actuators WHERE idActuator = ?;", Tuple.of(idActuator),
				res -> {
					if (res.succeeded()) {
						// Get the result set
						RowSet<Row> resultSet = res.result();
						Actuator actuator = null;
						if (resultSet.iterator().hasNext()) {
							Row elem = resultSet.iterator().next();
							actuator = new Actuator(elem.getInteger("idActuator"), elem.getString("name"),
									elem.getInteger("idDevice"), elem.getString("actuatorType"),
									elem.getBoolean("removed"));
						}
						databaseMessage.setResponseBody(actuator);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void editActuator(Actuator actuator, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery(
				"UPDATE dad_database.actuators g SET name = COALESCE(?, g.name), idDevice = COALESCE(?, g.idDevice), actuatorType = COALESCE(?, g.actuatorType), removed = COALESCE(?, g.removed) WHERE idActuator = ?;",
				Tuple.of(actuator.getName(), actuator.getIdDevice(), actuator.getActuatorType(), actuator.isRemoved(),
						actuator.getIdActuator()),
				res -> {
					if (res.succeeded()) {
						databaseMessage.setResponseBody(actuator);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void deleteActuator(int idActuator, DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery("DELETE FROM dad_database.actuators WHERE idActuator = ?;", Tuple.of(idActuator),
				res -> {
					if (res.succeeded()) {
						databaseMessage.setResponseBody(idActuator);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	// ================================================================================
	// ActuatorStates
	// ================================================================================

	protected void createActuatorStatus(ActuatorStatus actuatorState, DatabaseMessage databaseMessage,
			Message<Object> message) {
		mySqlClient.preparedQuery(
				"INSERT INTO dad_database.actuatorStates (status, statusBinary, idActuator, timestamp, removed) VALUES (?,?,?,?,?);",
				Tuple.of(actuatorState.getStatus(), actuatorState.isStatusBinary(), actuatorState.getIdActuator(),
						actuatorState.getTimestamp(), actuatorState.isRemoved()),
				res -> {
					if (res.succeeded()) {
						long lastInsertId = res.result().property(MySQLClient.LAST_INSERTED_ID);
						actuatorState.setIdActuatorState((int) lastInsertId);
						databaseMessage.setResponseBody(actuatorState);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void deleteActuatorStatus(int idActuatorStatus, DatabaseMessage databaseMessage,
			Message<Object> message) {
		mySqlClient.preparedQuery("DELETE FROM dad_database.idActuatorState WHERE idSensorValue = ?;",
				Tuple.of(idActuatorStatus), res -> {
					if (res.succeeded()) {
						databaseMessage.setResponseBody(idActuatorStatus);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void getLastActuatorStatusFromActuatorId(int idActuator, DatabaseMessage databaseMessage,
			Message<Object> message) {
		mySqlClient.preparedQuery(
				"SELECT * FROM dad_database.actuatorStates WHERE idActuator = ? ORDER BY `timestamp` DESC LIMIT 1;",
				Tuple.of(idActuator), res -> {
					if (res.succeeded()) {
						// Get the result set
						RowSet<Row> resultSet = res.result();
						ActuatorStatus actuatorStatus = null;
						if (resultSet.iterator().hasNext()) {
							Row elem = resultSet.iterator().next();
							actuatorStatus = new ActuatorStatus(elem.getInteger("idActuatorState"),
									elem.getFloat("status"), elem.getBoolean("statusBinary"),
									elem.getInteger("idActuator"), elem.getLong("timestamp"),
									elem.getBoolean("removed"));
						}
						databaseMessage.setResponseBody(actuatorStatus);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	protected void getLatestActuatorStatesFromActuatorId(DatabaseMessageLatestValues queryParam,
			DatabaseMessage databaseMessage, Message<Object> message) {
		mySqlClient.preparedQuery(
				"SELECT * FROM dad_database.actuatorStates WHERE idActuator = ? ORDER BY `timestamp` DESC LIMIT ?;",
				Tuple.of(queryParam.getId(), queryParam.getLimit()), res -> {
					if (res.succeeded()) {
						// Get the result set
						RowSet<Row> resultSet = res.result();
						List<ActuatorStatus> actuatorStates = new ArrayList<ActuatorStatus>();
						for (Row elem : resultSet) {
							actuatorStates
									.add(new ActuatorStatus(elem.getInteger("idActuatorState"), elem.getFloat("status"),
											elem.getBoolean("statusBinary"), elem.getInteger("idActuator"),
											elem.getLong("timestamp"), elem.getBoolean("removed")));
						}
						databaseMessage.setResponseBody(actuatorStates);
						databaseMessage.setStatusCode(200);
						message.reply(gson.toJson(databaseMessage));
					} else {
						message.fail(100, res.cause().getLocalizedMessage());
						System.err.println(res.cause());
					}
				});
	}

	public void stop(Future<Void> stopFuture) throws Exception {
		super.stop(stopFuture);
	}

}
