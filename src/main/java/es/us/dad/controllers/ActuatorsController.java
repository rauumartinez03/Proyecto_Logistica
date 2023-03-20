package es.us.dad.controllers;

import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
import es.us.dad.mysql.rest.RestEntityMessage;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class ActuatorsController extends AbstractController {

	public ActuatorsController() {
		super(DatabaseEntity.Actuator);
	}

	public void start(Promise<Void> startFuture) {

		getVertx().eventBus().consumer(RestEntityMessage.Actuator.getAddress(), message -> {
			DatabaseMessage databaseMessage = gson.fromJson((String) message.body(), DatabaseMessage.class);
			switch (databaseMessage.getMethod()) {
			case CreateActuator:
				launchDatabaseOperation(message);
				break;
			case GetActuator:
				launchDatabaseOperation(message);
				break;
			case EditActuator:
				launchDatabaseOperation(message);
				break;
			case DeleteActuator:
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
