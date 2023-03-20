package es.us.dad.controllers;

import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
import es.us.dad.mysql.rest.RestEntityMessage;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class SensorsController extends AbstractController {

	public SensorsController() {
		super(DatabaseEntity.Sensor);
	}

	public void start(Promise<Void> startFuture) {

		getVertx().eventBus().consumer(RestEntityMessage.Sensor.getAddress(), message -> {
			DatabaseMessage databaseMessage = gson.fromJson((String) message.body(), DatabaseMessage.class);
			switch (databaseMessage.getMethod()) {
			case CreateSensor:
				launchDatabaseOperation(message);
				break;
			case GetSensor:
				launchDatabaseOperation(message);
				break;
			case EditSensor:
				launchDatabaseOperation(message);
				break;
			case DeleteSensor:
				launchDatabaseOperation(message);
				break;
			default:
				message.fail(401, "Method not allowed");
			}
		});
		startFuture.complete();
	}

	public void stop(Future<Void> stopFuture) throws Exception {
		super.stop(stopFuture);
	}

}
