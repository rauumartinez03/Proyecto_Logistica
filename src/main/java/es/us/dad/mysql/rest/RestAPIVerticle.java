package es.us.dad.mysql.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import es.us.dad.mysql.entities.Actuator;
import es.us.dad.mysql.entities.ActuatorStatus;
import es.us.dad.mysql.entities.ActuatorType;
import es.us.dad.mysql.entities.Device;
import es.us.dad.mysql.entities.Group;
import es.us.dad.mysql.entities.Sensor;
import es.us.dad.mysql.entities.SensorType;
import es.us.dad.mysql.entities.SensorValue;
import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
import es.us.dad.mysql.messages.DatabaseMessageIdAndActuatorType;
import es.us.dad.mysql.messages.DatabaseMessageIdAndSensorType;
import es.us.dad.mysql.messages.DatabaseMessageLatestValues;
import es.us.dad.mysql.messages.DatabaseMessageType;
import es.us.dad.mysql.messages.DatabaseMethod;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RestAPIVerticle extends AbstractVerticle {

	private transient Gson gson;

	@Override
	public void start(Promise<Void> startFuture) {

		// Instantiating a Gson serialize object using specific date format
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

		// Defining the router object
		Router router = Router.router(vertx);

		// Handling any server startup result
		HttpServer httpServer = vertx.createHttpServer();
		httpServer.requestHandler(router::handle).listen(80, result -> {
			if (result.succeeded()) {
				System.out.println("API Rest is listening on port 80");
				startFuture.complete();
			} else {
				startFuture.fail(result.cause());
			}
		});

		// Defining URI paths for each method in RESTful interface, including body
		// handling
		router.route("/api*").handler(BodyHandler.create());
		
		router.get("/api/groups/:groupid").handler(this::getGroup);
		router.post("/api/groups").handler(this::createGroup);
		router.delete("/api/groups/:groupid").handler(this::deleteGroup);
		router.put("/api/groups/:groupid").handler(this::putGroup);
		router.put("/api/groups/:groupid/devices/:deviceid").handler(this::addDeviceToGroup);
		router.get("/api/groups/devices/:groupid").handler(this::getDevicesFromGroupId);
		
		router.get("/api/devices/:deviceid").handler(this::getDevice);
		router.post("/api/devices").handler(this::createDevice);
		router.delete("/api/devices/:deviceid").handler(this::deleteDevice);
		router.put("/api/devices/:deviceid").handler(this::putDevice);
		router.get("/api/devices/sensors/:deviceid").handler(this::getSensorsFromDeviceId);
		router.get("/api/devices/actuators/:deviceid").handler(this::getActuatorsFromDeviceId);
		router.get("/api/devices/:deviceid/sensors/:sensortype").handler(this::getSensorsFromDeviceIdAndSensorType);
		router.get("/api/devices/:deviceid/actuators/:actuatortype").handler(this::getActuatorsFromDeviceIdAndActuatorType);
		
		router.get("/api/sensors/:sensorid").handler(this::getSensor);
		router.post("/api/sensors").handler(this::createSensor);
		router.delete("/api/sensors/:sensorid").handler(this::deleteSensor);
		router.put("/api/sensors/:sensorid").handler(this::putSensor);
		
		router.get("/api/actuators/:actuatorid").handler(this::getActuator);
		router.post("/api/actuators").handler(this::createActuator);
		router.delete("/api/actuators/:actuatorid").handler(this::deleteActuator);
		router.put("/api/actuators/:actuatorid").handler(this::putActuator);
		
		router.post("/api/sensorValues").handler(this::createSensorValue);
		router.delete("/api/sensorValues/:valueid").handler(this::deleteSensorValue);
		router.get("/api/sensorValues/latest/:sensorid").handler(this::getLastSensorValueFromSensorId);
		router.get("/api/sensorValues/:sensorid/latest/:numEntries").handler(this::getLatestSensorValuesFromSensorId);
		
		router.post("/api/actuatorStatus").handler(this::createActuatorStatus);
		router.delete("/api/actuatorStatus/:stateid").handler(this::deleteActuatorStatus);
		router.get("/api/actuatorStatus/latest/:actuatorid").handler(this::getLastActuatorStatusFromActuatorId);
		router.get("/api/actuatorStatus/:actuatorid/latest/:numEntries").handler(this::getLatestActuatorStatesFromActuatorId);
	}

	private DatabaseMessage deserializeDatabaseMessageFromMessageHandler(AsyncResult<Message<Object>> handler) {
		return gson.fromJson(handler.result().body().toString(), DatabaseMessage.class);
	}
	
	private void getGroup(RoutingContext routingContext) {
		int groupId = Integer.parseInt(routingContext.request().getParam("groupid"));

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.Group,
				DatabaseMethod.GetGroup, groupId);

		vertx.eventBus().request(RestEntityMessage.Group.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Group.class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	private void createGroup(RoutingContext routingContext) {
		final Group group = gson.fromJson(routingContext.getBodyAsString(), Group.class);
		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Group,
				DatabaseMethod.CreateGroup, gson.toJson(group));

		vertx.eventBus().request(RestEntityMessage.Group.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(201)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Group.class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	private void deleteGroup(RoutingContext routingContext) {
		int groupId = Integer.parseInt(routingContext.request().getParam("groupid"));

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.DELETE, DatabaseEntity.Group,
				DatabaseMethod.DeleteGroup, groupId);

		vertx.eventBus().request(RestEntityMessage.Group.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(responseMessage.getResponseBody());
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	private void putGroup(RoutingContext routingContext) {
		final Group group = gson.fromJson(routingContext.getBodyAsString(), Group.class);
		int groupId = Integer.parseInt(routingContext.request().getParam("groupId"));
		group.setIdGroup(groupId);
		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.UPDATE, DatabaseEntity.Group,
				DatabaseMethod.EditGroup, gson.toJson(group));

		vertx.eventBus().request(RestEntityMessage.Group.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(201)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Group.class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	private void addDeviceToGroup(RoutingContext routingContext) {
		final Device device = new Device();
		int groupId = Integer.parseInt(routingContext.request().getParam("groupid"));
		int deviceId = Integer.parseInt(routingContext.request().getParam("deviceid"));
		device.setIdDevice(deviceId);
		device.setIdGroup(groupId);
		
		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.UPDATE, DatabaseEntity.Group,
				DatabaseMethod.AddDeviceToGroup, gson.toJson(device));

		vertx.eventBus().request(RestEntityMessage.Group.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(201)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Device.class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	private void getDevicesFromGroupId(RoutingContext routingContext) {
		int groupId = Integer.parseInt(routingContext.request().getParam("groupid"));

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.Group,
				DatabaseMethod.GetDevicesFromGroupId, groupId);

		vertx.eventBus().request(RestEntityMessage.Group.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Device [].class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	
	private void getDevice(RoutingContext routingContext) {
		int deviceId = Integer.parseInt(routingContext.request().getParam("deviceid"));

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.Device,
				DatabaseMethod.GetDevice, deviceId);

		vertx.eventBus().request(RestEntityMessage.Device.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Device.class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}

	private void createDevice(RoutingContext routingContext) {
		final Device device = gson.fromJson(routingContext.getBodyAsString(), Device.class);
		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Device,
				DatabaseMethod.CreateDevice, gson.toJson(device));

		vertx.eventBus().request(RestEntityMessage.Device.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(201)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Device.class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}

	private void deleteDevice(RoutingContext routingContext) {
		int deviceId = Integer.parseInt(routingContext.request().getParam("deviceId"));

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.DELETE, DatabaseEntity.Device,
				DatabaseMethod.DeleteDevice, deviceId);

		vertx.eventBus().request(RestEntityMessage.Device.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(responseMessage.getResponseBody());
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}

	private void putDevice(RoutingContext routingContext) {
		final Device device = gson.fromJson(routingContext.getBodyAsString(), Device.class);
		int deviceId = Integer.parseInt(routingContext.request().getParam("deviceid"));
		device.setIdDevice(deviceId);
		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.UPDATE, DatabaseEntity.Device,
				DatabaseMethod.EditDevice, gson.toJson(device));

		vertx.eventBus().request(RestEntityMessage.Device.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(201)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Device.class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	private void getSensorsFromDeviceId(RoutingContext routingContext) {
		int deviceId = Integer.parseInt(routingContext.request().getParam("deviceid"));

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.Device,
				DatabaseMethod.GetSensorsFromDeviceId, deviceId);

		vertx.eventBus().request(RestEntityMessage.Device.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Sensor [].class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	private void getActuatorsFromDeviceId(RoutingContext routingContext) {
		int deviceId = Integer.parseInt(routingContext.request().getParam("deviceid"));

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.Device,
				DatabaseMethod.GetActuatorsFromDeviceId, deviceId);

		vertx.eventBus().request(RestEntityMessage.Device.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Actuator [].class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	private void getSensorsFromDeviceIdAndSensorType(RoutingContext routingContext) {
		int deviceId = Integer.parseInt(routingContext.request().getParam("deviceid"));
		SensorType sensorType = SensorType.valueOf(routingContext.request().getParam("sensortype"));
		DatabaseMessageIdAndSensorType dt = new DatabaseMessageIdAndSensorType(deviceId, sensorType);

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.Device,
				DatabaseMethod.GetSensorsFromDeviceIdAndSensorType, dt);

		vertx.eventBus().request(RestEntityMessage.Device.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Sensor [].class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	private void getActuatorsFromDeviceIdAndActuatorType(RoutingContext routingContext) {
		int deviceId = Integer.parseInt(routingContext.request().getParam("deviceid"));
		ActuatorType actuatorType = ActuatorType.valueOf(routingContext.request().getParam("actuatortype"));
		DatabaseMessageIdAndActuatorType dt = new DatabaseMessageIdAndActuatorType(deviceId, actuatorType);

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.Device,
				DatabaseMethod.GetActuatorsFromDeviceIdAndActuatorType, dt);

		vertx.eventBus().request(RestEntityMessage.Device.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Actuator [].class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	
	private void getSensor(RoutingContext routingContext) {
		int sensorId = Integer.parseInt(routingContext.request().getParam("sensorid"));

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.Sensor,
				DatabaseMethod.GetSensor, sensorId);

		vertx.eventBus().request(RestEntityMessage.Sensor.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Sensor.class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}

	private void createSensor(RoutingContext routingContext) {
		final Sensor sensor = gson.fromJson(routingContext.getBodyAsString(), Sensor.class);
		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Sensor,
				DatabaseMethod.CreateSensor, gson.toJson(sensor));

		vertx.eventBus().request(RestEntityMessage.Sensor.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(201)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Sensor.class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}

	private void deleteSensor(RoutingContext routingContext) {
		int sensorId = Integer.parseInt(routingContext.request().getParam("sensorid"));

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.DELETE, DatabaseEntity.Sensor,
				DatabaseMethod.DeleteSensor, sensorId);

		vertx.eventBus().request(RestEntityMessage.Sensor.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(responseMessage.getResponseBody());
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}

	private void putSensor(RoutingContext routingContext) {
		final Sensor sensor = gson.fromJson(routingContext.getBodyAsString(), Sensor.class);
		int sensorId = Integer.parseInt(routingContext.request().getParam("sensorid"));
		sensor.setIdSensor(sensorId);
		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.UPDATE, DatabaseEntity.Sensor,
				DatabaseMethod.EditSensor, gson.toJson(sensor));

		vertx.eventBus().request(RestEntityMessage.Sensor.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(201)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Sensor.class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	
	private void getActuator(RoutingContext routingContext) {
		int actuatorId = Integer.parseInt(routingContext.request().getParam("actuatorid"));

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.Actuator,
				DatabaseMethod.GetActuator, actuatorId);

		vertx.eventBus().request(RestEntityMessage.Actuator.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Actuator.class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}

	private void createActuator(RoutingContext routingContext) {
		final Actuator actuator = gson.fromJson(routingContext.getBodyAsString(), Actuator.class);
		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Actuator,
				DatabaseMethod.CreateActuator, gson.toJson(actuator));

		vertx.eventBus().request(RestEntityMessage.Actuator.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(201)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Actuator.class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}

	private void deleteActuator(RoutingContext routingContext) {
		int actuatorId = Integer.parseInt(routingContext.request().getParam("actuatorid"));

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.DELETE, DatabaseEntity.Actuator,
				DatabaseMethod.DeleteActuator, actuatorId);

		vertx.eventBus().request(RestEntityMessage.Actuator.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(responseMessage.getResponseBody());
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}

	private void putActuator(RoutingContext routingContext) {
		final Actuator actuator = gson.fromJson(routingContext.getBodyAsString(), Actuator.class);
		int actuatorId = Integer.parseInt(routingContext.request().getParam("actuatorid"));
		actuator.setIdActuator(actuatorId);
		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.UPDATE, DatabaseEntity.Actuator,
				DatabaseMethod.EditActuator, gson.toJson(actuator));

		vertx.eventBus().request(RestEntityMessage.Actuator.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(201)
						.end(gson.toJson(responseMessage.getResponseBodyAs(Actuator.class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	

	private void createSensorValue(RoutingContext routingContext) {
		final SensorValue sensorValue = gson.fromJson(routingContext.getBodyAsString(), SensorValue.class);
		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.SensorValue,
				DatabaseMethod.CreateSensorValue, gson.toJson(sensorValue));

		vertx.eventBus().request(RestEntityMessage.SensorValue.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(201)
						.end(gson.toJson(responseMessage.getResponseBodyAs(SensorValue.class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	private void deleteSensorValue(RoutingContext routingContext) {
		int valueId = Integer.parseInt(routingContext.request().getParam("valueid"));

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.DELETE, DatabaseEntity.SensorValue,
				DatabaseMethod.DeleteSensorValue, valueId);

		vertx.eventBus().request(RestEntityMessage.SensorValue.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(responseMessage.getResponseBody());
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	private void getLastSensorValueFromSensorId(RoutingContext routingContext) {
		int sensorId = Integer.parseInt(routingContext.request().getParam("sensorid"));
		
		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.SensorValue,
				DatabaseMethod.GetLastSensorValueFromSensorId, sensorId);

		vertx.eventBus().request(RestEntityMessage.SensorValue.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(gson.toJson(responseMessage.getResponseBodyAs(SensorValue.class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	private void getLatestSensorValuesFromSensorId(RoutingContext routingContext) {
		int sensorId = Integer.parseInt(routingContext.request().getParam("sensorid"));
		int numEntries = Integer.parseInt(routingContext.request().getParam("numEntries"));
		DatabaseMessageLatestValues dv = new DatabaseMessageLatestValues(sensorId, numEntries);

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.SensorValue,
				DatabaseMethod.GetLatestSensorValuesFromSensorId, dv);

		vertx.eventBus().request(RestEntityMessage.SensorValue.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(gson.toJson(responseMessage.getResponseBodyAs(SensorValue [].class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	
	private void createActuatorStatus(RoutingContext routingContext) {
		final ActuatorStatus actuatorStatus = gson.fromJson(routingContext.getBodyAsString(), ActuatorStatus.class);
		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.ActuatorStatus,
				DatabaseMethod.CreateActuatorStatus, gson.toJson(actuatorStatus));

		vertx.eventBus().request(RestEntityMessage.ActuatorStatus.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(201)
						.end(gson.toJson(responseMessage.getResponseBodyAs(ActuatorStatus.class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	private void deleteActuatorStatus(RoutingContext routingContext) {
		int stateId = Integer.parseInt(routingContext.request().getParam("stateid"));

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.DELETE, DatabaseEntity.ActuatorStatus,
				DatabaseMethod.DeleteActuatorStatus, stateId);

		vertx.eventBus().request(RestEntityMessage.ActuatorStatus.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(responseMessage.getResponseBody());
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	private void getLastActuatorStatusFromActuatorId(RoutingContext routingContext) {
		int actuatorId = Integer.parseInt(routingContext.request().getParam("actuatorid"));

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.ActuatorStatus,
				DatabaseMethod.GetLastActuatorStatusFromActuatorId, actuatorId);

		vertx.eventBus().request(RestEntityMessage.ActuatorStatus.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(gson.toJson(responseMessage.getResponseBodyAs(ActuatorStatus.class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
	
	private void getLatestActuatorStatesFromActuatorId(RoutingContext routingContext) {
		int actuatorId = Integer.parseInt(routingContext.request().getParam("actuatorid"));
		int numEntries = Integer.parseInt(routingContext.request().getParam("numEntries"));
		DatabaseMessageLatestValues dv = new DatabaseMessageLatestValues(actuatorId, numEntries);

		DatabaseMessage databaseMessage = new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.ActuatorStatus,
				DatabaseMethod.GetLatestActuatorStatesFromActuatorId, dv);

		vertx.eventBus().request(RestEntityMessage.ActuatorStatus.getAddress(), gson.toJson(databaseMessage), handler -> {
			if (handler.succeeded()) {
				DatabaseMessage responseMessage = deserializeDatabaseMessageFromMessageHandler(handler);
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(gson.toJson(responseMessage.getResponseBodyAs(ActuatorStatus [].class)));
			} else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}

	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		super.stop(stopFuture);
	}

}
