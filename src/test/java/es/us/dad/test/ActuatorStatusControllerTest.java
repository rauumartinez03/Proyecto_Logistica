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
import es.us.dad.mysql.entities.ActuatorStatus;
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
public class ActuatorStatusControllerTest {

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
		ActuatorStatus actuator = new ActuatorStatus(19.5f, false, 10, Calendar.getInstance().getTimeInMillis(), false);
		vertx.eventBus().request(RestEntityMessage.ActuatorStatus.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.ActuatorStatus,
						DatabaseMethod.CreateActuatorStatus, actuator)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						ActuatorStatus returnObject = gson.fromJson(databaseMessage.getResponseBody(),
								ActuatorStatus.class);
						if (returnObject != null && returnObject.getIdActuator() != null) {
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
	@DisplayName("testCGetLastActuatorStatusFromActuatorId")
	public void testCGetLastActuatorStatusFromActuatorId(Vertx vertx, VertxTestContext testContext)
			throws InterruptedException {
		ActuatorStatus lastInsertedValue = new ActuatorStatus();
		VertxTestContext testContextInsertPre = new VertxTestContext();
		VertxTestContext testContextInsert = new VertxTestContext();
		ActuatorStatus actuator = new ActuatorStatus(1.2f, true, 2, Calendar.getInstance().getTimeInMillis(), false);

		for (int i = 0; i < 10; i++) {
			ActuatorStatus actuatorValue = new ActuatorStatus(actuator.getStatus(), actuator.isStatusBinary(),
					actuator.getIdActuator(), Calendar.getInstance().getTimeInMillis(), actuator.isRemoved());
			vertx.eventBus().send(RestEntityMessage.ActuatorStatus.getAddress(),
					gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.ActuatorStatus,
							DatabaseMethod.CreateActuatorStatus, actuatorValue)));
		}
		vertx.setTimer(1000, timerHandler -> testContextInsertPre.completeNow());

		testContextInsertPre.awaitCompletion(5, TimeUnit.SECONDS);
		actuator.setTimestamp(Calendar.getInstance().getTimeInMillis());

		vertx.eventBus().request(RestEntityMessage.ActuatorStatus.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.ActuatorStatus,
						DatabaseMethod.CreateActuatorStatus, actuator)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						ActuatorStatus returnObject = gson.fromJson(databaseMessage.getResponseBody(),
								ActuatorStatus.class);
						lastInsertedValue.setIdActuator(returnObject.getIdActuator());
						lastInsertedValue.setIdActuatorState(returnObject.getIdActuatorState());
						lastInsertedValue.setStatusBinary(returnObject.isStatusBinary());
						lastInsertedValue.setRemoved(returnObject.isRemoved());
						lastInsertedValue.setTimestamp(returnObject.getTimestamp());
						lastInsertedValue.setStatus(returnObject.getStatus());
						testContextInsert.completeNow();
					} else {
						testContextInsert.failNow(messageHandler.cause());
					}
				});
		testContextInsert.awaitCompletion(5, TimeUnit.SECONDS);

		vertx.eventBus().request(RestEntityMessage.ActuatorStatus.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.ActuatorStatus,
						DatabaseMethod.GetLastActuatorStatusFromActuatorId, lastInsertedValue.getIdActuator())),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						ActuatorStatus returnObject = gson.fromJson(databaseMessage.getResponseBody(),
								ActuatorStatus.class);
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
	@DisplayName("testCGetLatestActuatorStatesFromActuatorId")
	public void testCGetLatestActuatorStatesFromActuatorId(Vertx vertx, VertxTestContext testContext)
			throws InterruptedException {
		VertxTestContext testContextInsert = new VertxTestContext();
		ActuatorStatus actuatorStatusOriginal = new ActuatorStatus(1.2f, true, 71,
				Calendar.getInstance().getTimeInMillis(), false);

		List<ActuatorStatus> actuatorStatesAdded = new ArrayList<ActuatorStatus>();

		for (int i = 0; i < 30; i++) {
			try {
				ActuatorStatus actuatorStatus = new ActuatorStatus(
						actuatorStatusOriginal.getStatus() + (float) Math.random() * 10f, Math.random() > 0.5,
						actuatorStatusOriginal.getIdActuator(), Calendar.getInstance().getTimeInMillis() + i,
						actuatorStatusOriginal.isRemoved());
				actuatorStatesAdded.add(actuatorStatus);

			} catch (Exception e) {
				System.err.println(e.getLocalizedMessage());
			}
		}

		Promise<List<DatabaseMessage>> promise = ControllersUtils
				.launchDatabaseOperations(actuatorStatesAdded.stream().map(actuatorStatus -> {
					return new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.ActuatorStatus,
							DatabaseMethod.CreateActuatorStatus, actuatorStatus);
				}).collect(Collectors.toList()), vertx, 50);
		promise.future().onComplete(res -> {
			if (res.succeeded()) {
				vertx.eventBus().request(RestEntityMessage.ActuatorStatus.getAddress(),
						gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.ActuatorStatus,
								DatabaseMethod.GetLatestActuatorStatesFromActuatorId,
								new DatabaseMessageLatestValues(actuatorStatusOriginal.getIdActuator(), 10))),
						messageHandler -> {
							if (messageHandler.succeeded()) {
								DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
										DatabaseMessage.class);
								ActuatorStatus[] returnObject = gson.fromJson(databaseMessage.getResponseBody(),
										ActuatorStatus[].class);
								for (ActuatorStatus s : actuatorStatesAdded.subList(actuatorStatesAdded.size() - 10,
										actuatorStatesAdded.size())) {
									if (Stream.of(returnObject).noneMatch(elem -> elem.equalsWithNoIdConsidered(s))) {
										testContext.failNow(new Throwable("Actuator status not found"));
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
