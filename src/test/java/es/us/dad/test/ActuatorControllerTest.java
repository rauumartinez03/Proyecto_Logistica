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
import es.us.dad.mysql.entities.Actuator;
import es.us.dad.mysql.entities.ActuatorType;
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
public class ActuatorControllerTest {

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
		Actuator actuator = new Actuator("Actuator 1", 1, ActuatorType.Fan, false);
		vertx.eventBus().request(RestEntityMessage.Actuator.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Actuator,
						DatabaseMethod.CreateActuator, actuator)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Actuator returnObject = gson.fromJson(databaseMessage.getResponseBody(), Actuator.class);
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
	@DisplayName("testBEdit")
	public void testBEdit(Vertx vertx, VertxTestContext testContext) {
		Actuator actuator = new Actuator("Actuator 2", 1, ActuatorType.Light, false);
		vertx.eventBus().request(RestEntityMessage.Actuator.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Actuator,
						DatabaseMethod.CreateActuator, actuator)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Actuator returnObject = gson.fromJson(databaseMessage.getResponseBody(), Actuator.class);
						returnObject.setName("Actuator 2_1");
						returnObject.setRemoved(true);
						returnObject.setActuatorType(ActuatorType.Relay);
						returnObject.setIdDevice(2);
						vertx.eventBus()
								.request(RestEntityMessage.Actuator.getAddress(),
										gson.toJson(new DatabaseMessage(DatabaseMessageType.UPDATE,
												DatabaseEntity.Actuator, DatabaseMethod.EditActuator, returnObject)),
										messageHandlerUpdate -> {
											if (messageHandlerUpdate.succeeded()) {
												DatabaseMessage databaseMessageEdit = gson.fromJson(
														(String) messageHandlerUpdate.result().body(),
														DatabaseMessage.class);
												Actuator returnObjectEdit = gson.fromJson(
														databaseMessageEdit.getResponseBody(), Actuator.class);
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
		Actuator lastInsertedValue = new Actuator();
		VertxTestContext testContextInsert = new VertxTestContext();
		Actuator actuator = new Actuator("Actuator 3", 1, ActuatorType.Relay, false);
		vertx.eventBus().request(RestEntityMessage.Actuator.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Actuator,
						DatabaseMethod.CreateActuator, actuator)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Actuator returnObject = gson.fromJson(databaseMessage.getResponseBody(), Actuator.class);
						lastInsertedValue.setIdDevice(returnObject.getIdDevice());
						lastInsertedValue.setName(returnObject.getName());
						lastInsertedValue.setIdActuator(returnObject.getIdActuator());
						lastInsertedValue.setRemoved(returnObject.isRemoved());
						lastInsertedValue.setActuatorType(returnObject.getActuatorType());
						testContextInsert.completeNow();
					} else {
						testContextInsert.failNow(messageHandler.cause());
					}
				});
		testContextInsert.awaitCompletion(5, TimeUnit.SECONDS);

		vertx.eventBus().request(
				RestEntityMessage.Actuator.getAddress(), gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT,
						DatabaseEntity.Actuator, DatabaseMethod.GetActuator, lastInsertedValue.getIdActuator())),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Actuator returnObject = gson.fromJson(databaseMessage.getResponseBody(), Actuator.class);
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
		Actuator lastInsertedValue = new Actuator();
		VertxTestContext testContextInsert = new VertxTestContext();
		VertxTestContext testContextDelete = new VertxTestContext();

		Actuator actuator = new Actuator("Actuator 4", 2, ActuatorType.Light, false);
		vertx.eventBus().request(RestEntityMessage.Actuator.getAddress(),
				gson.toJson(new DatabaseMessage(DatabaseMessageType.INSERT, DatabaseEntity.Actuator,
						DatabaseMethod.CreateActuator, actuator)),
				messageHandler -> {
					if (messageHandler.succeeded()) {
						DatabaseMessage databaseMessage = gson.fromJson((String) messageHandler.result().body(),
								DatabaseMessage.class);
						Actuator returnObject = gson.fromJson(databaseMessage.getResponseBody(), Actuator.class);
						lastInsertedValue.setIdActuator(returnObject.getIdActuator());
						testContextInsert.completeNow();
					} else {
						testContextInsert.failNow(messageHandler.cause());
					}
				});
		testContextInsert.awaitCompletion(5, TimeUnit.SECONDS);

		vertx.eventBus().request(
				RestEntityMessage.Actuator.getAddress(), gson.toJson(new DatabaseMessage(DatabaseMessageType.DELETE,
						DatabaseEntity.Actuator, DatabaseMethod.DeleteActuator, lastInsertedValue.getIdActuator())),
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
				RestEntityMessage.Actuator.getAddress(), gson.toJson(new DatabaseMessage(DatabaseMessageType.SELECT,
						DatabaseEntity.Actuator, DatabaseMethod.GetActuator, lastInsertedValue.getIdActuator())),
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
