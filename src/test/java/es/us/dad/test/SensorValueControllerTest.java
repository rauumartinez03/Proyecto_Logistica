package es.us.dad.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.gson.Gson;

import es.us.dad.controllers.ActuatorStatesController;
import es.us.dad.controllers.ActuatorsController;
import es.us.dad.controllers.ControllersUtils;
import es.us.dad.controllers.DevicesController;
import es.us.dad.controllers.GroupsController;
import es.us.dad.controllers.SensorValuesController;
import es.us.dad.controllers.SensorsController;
import es.us.dad.mysql.MySQLVerticle;
import es.us.dad.mysql.entities.SensorValue;
import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
import es.us.dad.mysql.messages.DatabaseMessageLatestValues;
import es.us.dad.mysql.messages.DatabaseMessageType;
import es.us.dad.mysql.messages.DatabaseMethod;
import es.us.dad.mysql.rest.RestEntityMessage;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class SensorValueControllerTest {

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
		SensorValue sensor = new SensorValue(19.5f, 10, Calendar.getInstance().getTimeInMillis(), false);
		vertx.eventBus().request(RestEntityMessage.SensorValue.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.SensorValue,
						DatabaseMethod.CreateSensorValue, sensor)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						SensorValue returnObject = gson.fromJson(databaseMessage.getResponseBody(), SensorValue.class);
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
	@DisplayName("testCGetLastSensorValueFromSensorId")
	public void testCGetLastSensorValueFromSensorId(Vertx vertx, VertxTestContext testContext)
			throws InterruptedException {
		SensorValue lastInsertedValue = new SensorValue();
		VertxTestContext testContextInsertPre = new VertxTestContext();
		VertxTestContext testContextInsert = new VertxTestContext();
		SensorValue sensor = new SensorValue(1.2f, 71, Calendar.getInstance().getTimeInMillis(), false);

		for (int i = 0; i < 10; i++) {
			SensorValue sensorValue = new SensorValue(sensor.getValue(), sensor.getIdSensor(),
					Calendar.getInstance().getTimeInMillis(), sensor.isRemoved());
			vertx.eventBus().send(RestEntityMessage.SensorValue.getAddress(),
					gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.SensorValue,
							DatabaseMethod.CreateSensorValue, sensorValue)));
		}
		vertx.setTimer(1000, timerHandler -> testContextInsertPre.completeNow());

		testContextInsertPre.awaitCompletion(5, TimeUnit.SECONDS);
		sensor.setTimestamp(Calendar.getInstance().getTimeInMillis());

		vertx.eventBus().request(RestEntityMessage.SensorValue.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.SensorValue,
						DatabaseMethod.CreateSensorValue, sensor)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						SensorValue returnObject = gson.fromJson(databaseMessage.getResponseBody(), SensorValue.class);
						lastInsertedValue.setIdSensor(returnObject.getIdSensor());
						lastInsertedValue.setIdSensorValue(returnObject.getIdSensorValue());
						lastInsertedValue.setRemoved(returnObject.isRemoved());
						lastInsertedValue.setTimestamp(returnObject.getTimestamp());
						lastInsertedValue.setValue(returnObject.getValue());
						testContextInsert.completeNow();
					} else {
						testContextInsert.failNow(messageHandler.cause());
					}
				});
		testContextInsert.awaitCompletion(5, TimeUnit.SECONDS);

		vertx.eventBus().request(RestEntityMessage.SensorValue.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.SensorValue,
						DatabaseMethod.GetLastSensorValueFromSensorId, lastInsertedValue.getIdSensor())),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						SensorValue returnObject = gson.fromJson(databaseMessage.getResponseBody(), SensorValue.class);
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
	@DisplayName("testCGetLatestSensorValuesFromSensorId")
	public void testCGetLatestSensorValuesFromSensorId(Vertx vertx, VertxTestContext testContext)
			throws InterruptedException {
		VertxTestContext testContextInsert = new VertxTestContext();
		SensorValue sensorValueOriginal = new SensorValue(1.2f, 71, Calendar.getInstance().getTimeInMillis(), false);

		List<SensorValue> sensorValuesAdded = new ArrayList<SensorValue>();

		for (int i = 0; i < 30; i++) {
			try {
				SensorValue sensorValue = new SensorValue(sensorValueOriginal.getValue() + (float) Math.random() * 10f,
						sensorValueOriginal.getIdSensor(), Calendar.getInstance().getTimeInMillis() + i,
						sensorValueOriginal.isRemoved());
				sensorValuesAdded.add(sensorValue);
				
			} catch (Exception e) {
				System.err.println(e.getLocalizedMessage());
			}
		}

		Promise<List<DatabaseMessage>> promise = ControllersUtils
				.launchDatabaseOperations(sensorValuesAdded.stream().map(sensorValue -> {
					return new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.SensorValue,
							DatabaseMethod.CreateSensorValue, sensorValue);
				}).collect(Collectors.toList()), vertx, 50);
		promise.future().onComplete(res -> {
			if (res.succeeded()) {
				vertx.eventBus().request(RestEntityMessage.SensorValue.getAddress(),
						gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.SensorValue,
								DatabaseMethod.GetLatestSensorValuesFromSensorId,
								new DatabaseMessageLatestValues(sensorValueOriginal.getIdSensor(), 10))),
						messageHandler -> {
							if (messageHandler.succeeded()) {
								DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
										DatabaseMessage.class);
								SensorValue[] returnObject = gson.fromJson(databaseMessage.getResponseBody(),
										SensorValue[].class);
								for (SensorValue s : sensorValuesAdded.subList(sensorValuesAdded.size() - 10,
										sensorValuesAdded.size())) {
									if (Stream.of(returnObject).noneMatch(elem -> elem.equalsWithNoIdConsidered(s))) {
										testContext.failNow(new Throwable("Sensor value not found"));
									}
								}

								testContext.completeNow();
							} else {
								testContext.failNow(messageHandler.cause());
							}
						});
			}
		});

		testContextInsert.awaitCompletion(5, TimeUnit.SECONDS);

	}

}
