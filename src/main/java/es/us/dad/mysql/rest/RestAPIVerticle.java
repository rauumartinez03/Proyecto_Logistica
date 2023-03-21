package es.us.dad.mysql.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import es.us.dad.mysql.entities.Sensor;
import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
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

		router.get("/api/sensors/:sensorid").handler(this::getSensorById);
		router.post("/api/sensors").handler(this::addSensor);
		router.delete("/api/sensors/:sensorid").handler(this::deleteSensor);
		router.put("/api/sensors/:sensorid").handler(this::putSensor);
	}

	private DatabaseMessage deserializeDatabaseMessageFromMessageHandler(AsyncResult<Message<Object>> handler) {
		return gson.fromJson(handler.result().body().toString(), DatabaseMessage.class);
	}

	private void getSensorById(RoutingContext routingContext) {
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

	private void addSensor(RoutingContext routingContext) {
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

	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		super.stop(stopFuture);
	}

}
