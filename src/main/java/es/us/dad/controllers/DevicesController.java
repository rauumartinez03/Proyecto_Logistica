package es.us.dad.controllers;

import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
import es.us.dad.mysql.rest.RestEntityMessage;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class DevicesController extends AbstractController {

	public DevicesController() {
		super(DatabaseEntity.Device);
	}

	public void start(Promise<Void> startFuture) {

		getVertx().eventBus().consumer(RestEntityMessage.Device.getAddress(), message -> {
			DatabaseMessage databaseMessage = gson.fromJson((String) message.body(), DatabaseMessage.class);
			switch (databaseMessage.getMethod()) {
			case CreateDevice:
				launchDatabaseOperation(message);
				break;
			case GetDevice:
				launchDatabaseOperation(message);
				break;
			case EditDevice:
				launchDatabaseOperation(message);
				break;
			case DeleteDevice:
				launchDatabaseOperation(message);
				break;
			case GetSensorsFromDeviceId:
				launchDatabaseOperation(message);
				break;
			case GetActuatorsFromDeviceId:
				launchDatabaseOperation(message);
				break;
			case GetSensorsFromDeviceIdAndSensorType:
				launchDatabaseOperation(message);
				break;
			case GetActuatorsFromDeviceIdAndActuatorType:
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
