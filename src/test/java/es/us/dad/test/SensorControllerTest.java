package es.us.dad.test;

import java.util.concurrent.TimeUnit;

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
import es.us.dad.mysql.entities.Sensor;
import es.us.dad.mysql.entities.SensorType;
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
public class SensorControllerTest {

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
		Sensor sensor = new Sensor("Sensor 1", 1, SensorType.Temperature, false);
		vertx.eventBus().request(RestEntityMessage.Sensor.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Sensor,
						DatabaseMethod.CreateSensor, sensor)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Sensor returnObject = gson.fromJson(databaseMessage.getResponseBody(), Sensor.class);
						if (returnObject != null && returnObject.getIdSensor() != null) {
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
		Sensor sensor = new Sensor("Sensor 2", 1, SensorType.Humidity, false);
		vertx.eventBus().request(RestEntityMessage.Sensor.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Sensor,
						DatabaseMethod.CreateSensor, sensor)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Sensor returnObject = gson.fromJson(databaseMessage.getResponseBody(), Sensor.class);
						returnObject.setName("Sensor 2_1");
						returnObject.setRemoved(true);
						returnObject.setSensorType(SensorType.Pressure);
						returnObject.setIdDevice(2);
						vertx.eventBus()
								.request(RestEntityMessage.Sensor.getAddress(),
										gson.toJson(new DatabaseMessage(DatabaseMessageType.UPDATE,
												DatabaseEntity.Sensor, DatabaseMethod.EditSensor, returnObject)),
										messageHandlerUpdate -> {
											if (messageHandlerUpdate.succeeded()) {
												DatabaseMessage databaseMessageEdit = gson.fromJson(
														(String) messageHandlerUpdate.result().body(),
														DatabaseMessage.class);
												Sensor returnObjectEdit = gson
														.fromJson(databaseMessageEdit.getResponseBody(), Sensor.class);
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
		Sensor lastInsertedValue = new Sensor();
		VertxTestContext testContextInsert = new VertxTestContext();
		Sensor sensor = new Sensor("Sensor 3", 1, SensorType.Temperature, false);
		vertx.eventBus().request(RestEntityMessage.Sensor.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Sensor,
						DatabaseMethod.CreateSensor, sensor)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Sensor returnObject = gson.fromJson(databaseMessage.getResponseBody(), Sensor.class);
						lastInsertedValue.setIdDevice(returnObject.getIdDevice());
						lastInsertedValue.setName(returnObject.getName());
						lastInsertedValue.setIdSensor(returnObject.getIdSensor());
						lastInsertedValue.setRemoved(returnObject.isRemoved());
						lastInsertedValue.setSensorType(returnObject.getSensorType());
						testContextInsert.completeNow();
					} else {
						testContextInsert.failNow(messageHandler.cause());
					}
				});
		testContextInsert.awaitCompletion(5, TimeUnit.SECONDS);

		vertx.eventBus().request(
				RestEntityMessage.Sensor.getAddress(), gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT,
						DatabaseEntity.Sensor, DatabaseMethod.GetSensor, lastInsertedValue.getIdSensor())),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Sensor returnObject = gson.fromJson(databaseMessage.getResponseBody(), Sensor.class);
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
		Sensor lastInsertedValue = new Sensor();
		VertxTestContext testContextInsert = new VertxTestContext();
		VertxTestContext testContextDelete = new VertxTestContext();

		Sensor sensor = new Sensor("Sensor 4", 2, SensorType.Pressure, false);
		vertx.eventBus().request(RestEntityMessage.Sensor.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Sensor,
						DatabaseMethod.CreateSensor, sensor)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Sensor returnObject = gson.fromJson(databaseMessage.getResponseBody(), Sensor.class);
						lastInsertedValue.setIdSensor(returnObject.getIdSensor());
						testContextInsert.completeNow();
					} else {
						testContextInsert.failNow(messageHandler.cause());
					}
				});
		testContextInsert.awaitCompletion(5, TimeUnit.SECONDS);

		vertx.eventBus().request(
				RestEntityMessage.Sensor.getAddress(), gson.toJson(new DatabaseMessage(DatabaseMessageType.DELETE,
						DatabaseEntity.Sensor, DatabaseMethod.DeleteSensor, lastInsertedValue.getIdSensor())),
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
				RestEntityMessage.Sensor.getAddress(), gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT,
						DatabaseEntity.Sensor, DatabaseMethod.GetSensor, lastInsertedValue.getIdSensor())),
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

}
