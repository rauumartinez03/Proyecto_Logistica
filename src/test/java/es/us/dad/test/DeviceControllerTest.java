package es.us.dad.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.gson.Gson;

import es.us.dad.controllers.ActuatorStatesController;
import es.us.dad.controllers.ActuatorsController;
import es.us.dad.controllers.DevicesController;
import es.us.dad.controllers.GroupsController;
import es.us.dad.controllers.SensorValuesController;
import es.us.dad.controllers.SensorsController;
import es.us.dad.mysql.MySQLVerticle;
import es.us.dad.mysql.entities.Actuator;
import es.us.dad.mysql.entities.ActuatorType;
import es.us.dad.mysql.entities.Device;
import es.us.dad.mysql.entities.Sensor;
import es.us.dad.mysql.entities.SensorType;
import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
import es.us.dad.mysql.messages.DatabaseMessageIdAndActuatorType;
import es.us.dad.mysql.messages.DatabaseMessageIdAndSensorType;
import es.us.dad.mysql.messages.DatabaseMessageType;
import es.us.dad.mysql.messages.DatabaseMethod;
import es.us.dad.mysql.rest.RestEntityMessage;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class DeviceControllerTest {

	Gson gson = new Gson();

	@BeforeEach
	void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
		Checkpoint deviceVerticle = testContext.checkpoint();
		Checkpoint sensorVerticle = testContext.checkpoint();
		Checkpoint actuatorVerticle = testContext.checkpoint();
		Checkpoint groupVerticle = testContext.checkpoint();
		Checkpoint sensorValueVerticle = testContext.checkpoint();
		Checkpoint actuatorStatusVerticle = testContext.checkpoint();
		vertx.deployVerticle(new MySQLVerticle(), handler -> {
			if (handler.succeeded()) {
				vertx.deployVerticle(new DevicesController(), handlerController -> {
					if (handlerController.succeeded())
						deviceVerticle.flag();
					else
						testContext.failNow(handlerController.cause());
				});
				vertx.deployVerticle(new SensorsController(), handlerController -> {
					if (handlerController.succeeded())
						sensorVerticle.flag();
					else
						testContext.failNow(handlerController.cause());
				});
				vertx.deployVerticle(new ActuatorsController(), handlerController -> {
					if (handlerController.succeeded())
						actuatorVerticle.flag();
					else
						testContext.failNow(handlerController.cause());
				});
				vertx.deployVerticle(new GroupsController(), handlerController -> {
					if (handlerController.succeeded())
						groupVerticle.flag();
					else
						testContext.failNow(handlerController.cause());
				});
				vertx.deployVerticle(new SensorValuesController(), handlerController -> {
					if (handlerController.succeeded())
						sensorValueVerticle.flag();
					else
						testContext.failNow(handlerController.cause());
				});
				vertx.deployVerticle(new ActuatorStatesController(), handlerController -> {
					if (handlerController.succeeded())
						actuatorStatusVerticle.flag();
					else
						testContext.failNow(handlerController.cause());
				});
			} else {
				testContext.failNow(handler.cause());
			}
		});
	}

	@Test
	@DisplayName("testACreate")
	public void testACreate(Vertx vertx, VertxTestContext testContext) {
		Device device = new Device("Serial number 1", "Device number 1", 1, "mqttChannelDevice1", 99999999L, 99999998L);
		vertx.eventBus().request(RestEntityMessage.Device.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Device,
						DatabaseMethod.CreateDevice, device)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Device returnObject = gson.fromJson(databaseMessage.getResponseBody(), Device.class);
						if (returnObject != null && returnObject.getIdDevice() != null) {
							testContext.completeNow();
						} else {
							testContext.failNow(messageHandler.cause());
						}
					} else {
						testContext.failNow(messageHandler.cause());
					}
				});

	}

	@Test
	@DisplayName("testBEdit")
	public void testBEdit(Vertx vertx, VertxTestContext testContext) {
		Device device = new Device("Serial number 2", "Device number 2", 2, "mqttChannelDevice2", 99999997L, 99999996L);
		vertx.eventBus().request(RestEntityMessage.Device.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Device,
						DatabaseMethod.CreateDevice, device)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Device returnObject = gson.fromJson(databaseMessage.getResponseBody(), Device.class);
						returnObject.setIdGroup(3);
						returnObject.setMqttChannel("mqttChannelDevice3");
						returnObject.setDeviceSerialId("Serial number 3");
						returnObject.setName("Device number 3");
						returnObject.setLastTimestampActuatorModified(99999995L);
						returnObject.setLastTimestampSensorModified(99999994L);
						vertx.eventBus()
								.request(RestEntityMessage.Device.getAddress(),
										gson.toJson(new DatabaseMessage(DatabaseMessageType.UPDATE,
												DatabaseEntity.Device, DatabaseMethod.EditDevice, returnObject)),
										messageHandlerUpdate -> {
											if (messageHandlerUpdate.succeeded()) {
												DatabaseMessage databaseMessageEdit = gson.fromJson(
														(String) messageHandlerUpdate.result().body(),
														DatabaseMessage.class);
												Device returnObjectEdit = gson
														.fromJson(databaseMessageEdit.getResponseBody(), Device.class);
												if (returnObjectEdit.equals(returnObject)) {
													testContext.completeNow();
												} else {
													testContext.failNow(new Throwable("Edit not works"));
												}
											} else {
												testContext.failNow(messageHandlerUpdate.cause());
											}
										});
					} else {
						testContext.failNow(messageHandler.cause());
					}
				});
	}

	@Test
	@DisplayName("testCGet")
	public void testCGet(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
		Device lastInsertedValue = new Device();
		VertxTestContext testContextInsert = new VertxTestContext();
		Device device = new Device("Serial number 1", "Device number 1", 1, "mqttChannelDevice1", 99999999L, 99999998L);
		vertx.eventBus().request(RestEntityMessage.Device.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Device,
						DatabaseMethod.CreateDevice, device)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Device returnObject = gson.fromJson(databaseMessage.getResponseBody(), Device.class);
						lastInsertedValue.setDeviceSerialId(returnObject.getDeviceSerialId());
						lastInsertedValue.setIdDevice(returnObject.getIdDevice());
						lastInsertedValue.setIdGroup(returnObject.getIdGroup());
						lastInsertedValue
								.setLastTimestampActuatorModified(returnObject.getLastTimestampActuatorModified());
						lastInsertedValue.setLastTimestampSensorModified(returnObject.getLastTimestampSensorModified());
						lastInsertedValue.setMqttChannel(returnObject.getMqttChannel());
						lastInsertedValue.setName(returnObject.getName());
						testContextInsert.completeNow();
					} else {
						testContextInsert.failNow(messageHandler.cause());
					}
				});
		testContextInsert.awaitCompletion(5, TimeUnit.SECONDS);

		vertx.eventBus().request(
				RestEntityMessage.Device.getAddress(), gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT,
						DatabaseEntity.Device, DatabaseMethod.GetDevice, lastInsertedValue.getIdDevice())),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Device returnObject = gson.fromJson(databaseMessage.getResponseBody(), Device.class);
						if (returnObject.equals(lastInsertedValue)) {
							testContext.completeNow();
						} else {
							testContext.failNow(new Throwable("Not equals get value"));
						}
					} else {
						testContext.failNow(messageHandler.cause());
					}
				});

	}

	@Test
	@DisplayName("testDDelete")
	public void testDDelete(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
		Device lastInsertedValue = new Device();
		VertxTestContext testContextInsert = new VertxTestContext();
		VertxTestContext testContextDelete = new VertxTestContext();

		Device device = new Device("Serial number 4", "Device number 4", 4, "mqttChannelDevice4", 99999999L, 99999998L);
		vertx.eventBus().request(RestEntityMessage.Device.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Device,
						DatabaseMethod.CreateDevice, device)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Device returnObject = gson.fromJson(databaseMessage.getResponseBody(), Device.class);
						lastInsertedValue.setIdDevice(returnObject.getIdDevice());
						testContextInsert.completeNow();
					} else {
						testContextInsert.failNow(messageHandler.cause());
					}
				});
		testContextInsert.awaitCompletion(5, TimeUnit.SECONDS);

		vertx.eventBus().request(
				RestEntityMessage.Device.getAddress(), gson.toJson(new DatabaseMessage(DatabaseMessageType.DELETE,
						DatabaseEntity.Device, DatabaseMethod.DeleteDevice, lastInsertedValue.getIdDevice())),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						System.out.println("Deleted object ID: " + databaseMessage.getResponseBody().toString());
						testContextDelete.completeNow();
					} else {
						testContextDelete.failNow(messageHandler.cause());
					}
				});

		testContextDelete.awaitCompletion(5, TimeUnit.SECONDS);

		vertx.eventBus().request(
				RestEntityMessage.Device.getAddress(), gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT,
						DatabaseEntity.Device, DatabaseMethod.GetDevice, lastInsertedValue.getIdDevice())),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						if (databaseMessage.getResponseBody() == null) {
							testContext.completeNow();
						} else {
							testContext.failNow(new Throwable("Not equals get value"));
						}
					} else {
						testContext.failNow(messageHandler.cause());
					}
				});

	}

	@Test
	@DisplayName("testEGetSensorsFromDevice")
	public void testEGetSensorsFromDevice(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
		Device lastInsertedValue = new Device();
		VertxTestContext testContextInsert = new VertxTestContext();
		Device device = new Device("Serial number 5", "Device number 5", 5, "mqttChannelDevice5", 99999999L, 99999998L);
		List<Sensor> addedSensors = new ArrayList<Sensor>();
		vertx.eventBus().request(RestEntityMessage.Device.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Device,
						DatabaseMethod.CreateDevice, device)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Device returnObject = gson.fromJson(databaseMessage.getResponseBody(), Device.class);
						lastInsertedValue.setIdDevice(returnObject.getIdDevice());

						for (int i = 0; i < 10; i++) {
							Sensor sensor = new Sensor("sensor " + i, returnObject.getIdDevice(), SensorType
									.values()[(int) Math.round(Math.random() * (SensorType.values().length - 1))],
									false);
							addedSensors.add(sensor);
							vertx.eventBus().send(RestEntityMessage.Sensor.getAddress(),
									gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Sensor,
											DatabaseMethod.CreateSensor, sensor)));
						}
						vertx.setTimer(1000, timerHandler -> testContextInsert.completeNow());
					} else {
						testContextInsert.failNow(messageHandler.cause());
					}
				});
		testContextInsert.awaitCompletion(5, TimeUnit.SECONDS);

		vertx.eventBus().request(
				RestEntityMessage.Device.getAddress(), gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT,
						DatabaseEntity.Device, DatabaseMethod.GetSensorsFromDeviceId, lastInsertedValue.getIdDevice())),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Sensor[] sensorsRetrieved = gson.fromJson(databaseMessage.getResponseBody(), Sensor[].class);
						for (Sensor s : addedSensors) {
							if (Stream.of(sensorsRetrieved).noneMatch(elem -> elem.equalsWithNoIdConsidered(s))) {
								testContext.failNow(new Throwable("Sensor not found"));
							}
						}
						if (addedSensors.size() == sensorsRetrieved.length) {
							testContext.completeNow();
						} else {
							testContext.failNow(new Throwable("Sensor not found"));
						}
					} else {
						testContext.failNow(messageHandler.cause());
					}
				});
	}

	@Test
	@DisplayName("testFGetActuatorsFromDevice")
	public void testFGetActuatorsFromDevice(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
		Device lastInsertedValue = new Device();
		VertxTestContext testContextInsert = new VertxTestContext();
		Device device = new Device("Serial number 6", "Device number 6", 6, "mqttChannelDevice6", 99999999L, 99999998L);
		List<Actuator> addedActuators = new ArrayList<Actuator>();
		vertx.eventBus().request(RestEntityMessage.Device.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Device,
						DatabaseMethod.CreateDevice, device)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Device returnObject = gson.fromJson(databaseMessage.getResponseBody(), Device.class);
						lastInsertedValue.setIdDevice(returnObject.getIdDevice());

						for (int i = 0; i < 10; i++) {
							Actuator actuator = new Actuator("actuator " + i, returnObject.getIdDevice(), ActuatorType
									.values()[(int) Math.round(Math.random() * (ActuatorType.values().length - 1))],
									false);
							addedActuators.add(actuator);
							vertx.eventBus().send(RestEntityMessage.Actuator.getAddress(),
									gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Actuator,
											DatabaseMethod.CreateActuator, actuator)));
						}
						vertx.setTimer(1000, timerHandler -> testContextInsert.completeNow());
					} else {
						testContextInsert.failNow(messageHandler.cause());
					}
				});
		testContextInsert.awaitCompletion(5, TimeUnit.SECONDS);

		vertx.eventBus()
				.request(RestEntityMessage.Device.getAddress(),
						gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.Device,
								DatabaseMethod.GetActuatorsFromDeviceId, lastInsertedValue.getIdDevice())),
						messageHandler -> {
							if (messageHandler.succeeded()) {
								DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
										DatabaseMessage.class);
								Actuator[] actuatorsRetrieved = gson.fromJson(databaseMessage.getResponseBody(),
										Actuator[].class);
								for (Actuator s : addedActuators) {
									if (Stream.of(actuatorsRetrieved)
											.noneMatch(elem -> elem.equalsWithNoIdConsidered(s))) {
										testContext.failNow(new Throwable("Actuator not found"));
									}
								}

								if (addedActuators.size() == actuatorsRetrieved.length) {
									testContext.completeNow();
								} else {
									testContext.failNow(new Throwable("Actuator not found"));
								}
							} else {
								testContext.failNow(messageHandler.cause());
							}
						});
	}

	@Test
	@DisplayName("testGGetSensorsFromDeviceIdAndSensorType")
	public void testGGetSensorsFromDeviceIdAndSensorType(Vertx vertx, VertxTestContext testContext)
			throws InterruptedException {
		Device lastInsertedValue = new Device();
		VertxTestContext testContextInsert = new VertxTestContext();
		Device device = new Device("Serial number 5", "Device number 5", 5, "mqttChannelDevice5", 99999999L, 99999998L);
		List<Sensor> addedSensors = new ArrayList<Sensor>();
		vertx.eventBus().request(RestEntityMessage.Device.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Device,
						DatabaseMethod.CreateDevice, device)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Device returnObject = gson.fromJson(databaseMessage.getResponseBody(), Device.class);
						lastInsertedValue.setIdDevice(returnObject.getIdDevice());

						for (int i = 0; i < 50; i++) {
							Sensor sensor = new Sensor("sensor " + i, returnObject.getIdDevice(), SensorType
									.values()[(int) Math.round(Math.random() * (SensorType.values().length - 1))],
									false);
							addedSensors.add(sensor);
							vertx.eventBus().send(RestEntityMessage.Sensor.getAddress(),
									gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Sensor,
											DatabaseMethod.CreateSensor, sensor)));
						}
						vertx.setTimer(1000, timerHandler -> testContextInsert.completeNow());
					} else {
						testContextInsert.failNow(messageHandler.cause());
					}
				});
		testContextInsert.awaitCompletion(5, TimeUnit.SECONDS);

		SensorType sensorType = SensorType.values()[(int) Math.round(Math.random() * (SensorType.values().length - 1))];

		vertx.eventBus().request(RestEntityMessage.Device.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.Device,
						DatabaseMethod.GetSensorsFromDeviceIdAndSensorType,
						new DatabaseMessageIdAndSensorType(lastInsertedValue.getIdDevice(), sensorType))),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Sensor[] sensorsRetrieved = gson.fromJson(databaseMessage.getResponseBody(), Sensor[].class);
						for (Sensor s : addedSensors) {
							if (s.getSensorType().equals(sensorType) && Stream.of(sensorsRetrieved)
									.noneMatch(elem -> elem.equalsWithNoIdConsidered(s))) {
								testContext.failNow(new Throwable("Sensor not found"));
							}
						}
						testContext.completeNow();
					} else {
						testContext.failNow(messageHandler.cause());
					}
				});
	}

	@Test
	@DisplayName("testHGetActuatorsFromDeviceIdAndActuatorType")
	public void testHGetActuatorsFromDeviceIdAndActuatorType(Vertx vertx, VertxTestContext testContext)
			throws InterruptedException {
		Device lastInsertedValue = new Device();
		VertxTestContext testContextInsert = new VertxTestContext();
		Device device = new Device("Serial number 6", "Device number 6", 6, "mqttChannelDevice6", 99999999L, 99999998L);
		List<Actuator> addedActuators = new ArrayList<Actuator>();
		vertx.eventBus().request(RestEntityMessage.Device.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Device,
						DatabaseMethod.CreateDevice, device)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Device returnObject = gson.fromJson(databaseMessage.getResponseBody(), Device.class);
						lastInsertedValue.setIdDevice(returnObject.getIdDevice());

						for (int i = 0; i < 50; i++) {
							Actuator actuator = new Actuator("actuator " + i, returnObject.getIdDevice(), ActuatorType
									.values()[(int) Math.round(Math.random() * (ActuatorType.values().length - 1))],
									false);
							addedActuators.add(actuator);
							vertx.eventBus().send(RestEntityMessage.Actuator.getAddress(),
									gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Actuator,
											DatabaseMethod.CreateActuator, actuator)));
						}
						vertx.setTimer(1000, timerHandler -> testContextInsert.completeNow());
					} else {
						testContextInsert.failNow(messageHandler.cause());
					}
				});
		testContextInsert.awaitCompletion(5, TimeUnit.SECONDS);

		ActuatorType actuatorType = ActuatorType
				.values()[(int) Math.round(Math.random() * (ActuatorType.values().length - 1))];

		vertx.eventBus().request(RestEntityMessage.Device.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.Device,
						DatabaseMethod.GetActuatorsFromDeviceIdAndActuatorType,
						new DatabaseMessageIdAndActuatorType(lastInsertedValue.getIdDevice(), actuatorType))),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Actuator[] actuatorsRetrieved = gson.fromJson(databaseMessage.getResponseBody(),
								Actuator[].class);
						for (Actuator s : addedActuators) {
							if (s.getActuatorType().equals(actuatorType) && Stream.of(actuatorsRetrieved)
									.noneMatch(elem -> elem.equalsWithNoIdConsidered(s))) {
								testContext.failNow(new Throwable("Actuator not found"));
							}
						}
						testContext.completeNow();
					} else {
						testContext.failNow(messageHandler.cause());
					}
				});
	}

}
