package es.us.dad.test;

import java.util.ArrayList;
import java.util.Calendar;
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
import es.us.dad.mysql.entities.Device;
import es.us.dad.mysql.entities.Group;
import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
import es.us.dad.mysql.messages.DatabaseMessageType;
import es.us.dad.mysql.messages.DatabaseMethod;
import es.us.dad.mysql.rest.RestEntityMessage;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class GroupControllerTest {

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
		Group group = new Group("mqttChannelGroup1", "group1", Calendar.getInstance().getTimeInMillis());
		vertx.eventBus().request(RestEntityMessage.Group.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Group,
						DatabaseMethod.CreateGroup, group)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Group returnObject = gson.fromJson(databaseMessage.getResponseBody(), Group.class);
						if (returnObject != null && returnObject.getIdGroup() != null) {
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
		Group group = new Group("mqttChannelGroup2", "group2", Calendar.getInstance().getTimeInMillis());
		vertx.eventBus().request(RestEntityMessage.Group.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Group,
						DatabaseMethod.CreateGroup, group)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Group returnObject = gson.fromJson(databaseMessage.getResponseBody(), Group.class);
						returnObject.setIdGroup(3);
						returnObject.setMqttChannel("mqttChannelDevice3");
						returnObject.setName("Device number 3");
						vertx.eventBus()
								.request(RestEntityMessage.Group.getAddress(),
										gson.toJson(new DatabaseMessage(DatabaseMessageType.UPDATE,
												DatabaseEntity.Group, DatabaseMethod.EditGroup, returnObject)),
										messageHandlerUpdate -> {
											if (messageHandlerUpdate.succeeded()) {
												DatabaseMessage databaseMessageEdit = gson.fromJson(
														(String) messageHandlerUpdate.result().body(),
														DatabaseMessage.class);
												Group returnObjectEdit = gson
														.fromJson(databaseMessageEdit.getResponseBody(), Group.class);
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
		Group lastInsertedValue = new Group();
		VertxTestContext testContextInsert = new VertxTestContext();
		Group group = new Group("mqttChannelGroup3", "group3", Calendar.getInstance().getTimeInMillis());
		vertx.eventBus().request(RestEntityMessage.Group.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Group,
						DatabaseMethod.CreateGroup, group)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Group returnObject = gson.fromJson(databaseMessage.getResponseBody(), Group.class);
						lastInsertedValue.setLastMessageReceived(returnObject.getLastMessageReceived());
						lastInsertedValue.setMqttChannel(returnObject.getMqttChannel());
						lastInsertedValue.setName(returnObject.getName());
						lastInsertedValue.setIdGroup(returnObject.getIdGroup());
						testContextInsert.completeNow();
					} else {
						testContextInsert.failNow(messageHandler.cause());
					}
				});
		testContextInsert.awaitCompletion(5, TimeUnit.SECONDS);

		vertx.eventBus().request(
				RestEntityMessage.Group.getAddress(), gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT,
						DatabaseEntity.Group, DatabaseMethod.GetGroup, lastInsertedValue.getIdGroup())),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Group returnObject = gson.fromJson(databaseMessage.getResponseBody(), Group.class);
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
		Group lastInsertedValue = new Group();
		VertxTestContext testContextInsert = new VertxTestContext();
		VertxTestContext testContextDelete = new VertxTestContext();

		Group group = new Group("mqttChannelGroup4", "group4", Calendar.getInstance().getTimeInMillis());
		vertx.eventBus().request(RestEntityMessage.Group.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Group,
						DatabaseMethod.CreateGroup, group)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Group returnObject = gson.fromJson(databaseMessage.getResponseBody(), Group.class);
						lastInsertedValue.setIdGroup(returnObject.getIdGroup());
						testContextInsert.completeNow();
					} else {
						testContextInsert.failNow(messageHandler.cause());
					}
				});
		testContextInsert.awaitCompletion(5, TimeUnit.SECONDS);

		vertx.eventBus().request(
				RestEntityMessage.Group.getAddress(), gson.toJson(new DatabaseMessage(DatabaseMessageType.DELETE,
						DatabaseEntity.Group, DatabaseMethod.DeleteGroup, lastInsertedValue.getIdGroup())),
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
				RestEntityMessage.Group.getAddress(), gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT,
						DatabaseEntity.Group, DatabaseMethod.GetGroup, lastInsertedValue.getIdGroup())),
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
	@DisplayName("testEAddDeviceToGroup")
	public void testEAddDeviceToGroup(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
		Device lastInsertedValue = new Device();
		VertxTestContext testContextInsert = new VertxTestContext();
		VertxTestContext testContextEdit = new VertxTestContext();
		Device device = new Device("Serial number 5", "Device number 5", 1, "mqttChannelDevice5", 99999999L, 99999998L);
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

		lastInsertedValue.setIdGroup(18);
		vertx.eventBus().request(RestEntityMessage.Group.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.Group,
						DatabaseMethod.AddDeviceToGroup, lastInsertedValue)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Device changedDevice = gson.fromJson(databaseMessage.getResponseBody(), Device.class);
						if (changedDevice.getIdGroup() != 18) {
							testContext.failNow(new Throwable("Group not changed"));
						} else {
							testContextEdit.completeNow();
						}
					} else {
						testContextEdit.failNow(messageHandler.cause());
					}
				});

		testContextEdit.awaitCompletion(5, TimeUnit.SECONDS);

		vertx.eventBus().request(
				RestEntityMessage.Device.getAddress(), gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT,
						DatabaseEntity.Device, DatabaseMethod.GetDevice, lastInsertedValue.getIdDevice())),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Device changedDevice = gson.fromJson(databaseMessage.getResponseBody(), Device.class);
						if (changedDevice.getIdGroup() == 18) {
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
	@DisplayName("testEGetDevicesFromGroupId")
	public void testEGetDevicesFromGroupId(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
		Group lastInsertedValue = new Group();
		VertxTestContext testContextInsert = new VertxTestContext();
		Group group = new Group("mqttChannelGroup6", "group6", Calendar.getInstance().getTimeInMillis());
		List<Device> addedDevices = new ArrayList<Device>();
		vertx.eventBus().request(RestEntityMessage.Group.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Group,
						DatabaseMethod.CreateGroup, group)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Group returnObject = gson.fromJson(databaseMessage.getResponseBody(), Group.class);
						lastInsertedValue.setIdGroup(returnObject.getIdGroup());

						for (int i = 0; i < 10; i++) {
							Device device = new Device(
									"Serial number group_" + lastInsertedValue.getIdGroup() + "_" + i,
									"Device number group_" + lastInsertedValue.getIdGroup() + "_" + i,
									lastInsertedValue.getIdGroup(), "mqttChannelDevice" + i, 0L,
									Calendar.getInstance().getTimeInMillis());
							addedDevices.add(device);
							vertx.eventBus().send(RestEntityMessage.Device.getAddress(),
									gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Device,
											DatabaseMethod.CreateDevice, device)));
						}
						vertx.setTimer(1000, timerHandler -> testContextInsert.completeNow());
					} else {
						testContextInsert.failNow(messageHandler.cause());
					}
				});
		testContextInsert.awaitCompletion(5, TimeUnit.SECONDS);

		vertx.eventBus().request(
				RestEntityMessage.Group.getAddress(), gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT,
						DatabaseEntity.Group, DatabaseMethod.GetDevicesFromGroupId, lastInsertedValue.getIdGroup())),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Device[] devicesRetrieved = gson.fromJson(databaseMessage.getResponseBody(), Device[].class);
						for (Device s : addedDevices) {
							if (Stream.of(devicesRetrieved).noneMatch(elem -> elem.equalsWithNoIdConsidered(s))) {
								testContext.failNow(new Throwable("Device not found"));
							}
						}

						if (addedDevices.size() == devicesRetrieved.length) {
							testContext.completeNow();
						}else {
							testContext.failNow(new Throwable("Device not found"));
						}
					} else {
						testContext.failNow(messageHandler.cause());
					}
				});
	}

}
