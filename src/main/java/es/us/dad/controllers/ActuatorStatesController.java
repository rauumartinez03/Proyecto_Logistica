package es.us.dad.controllers;

import java.util.Calendar;

import es.us.dad.mysql.entities.Actuator;
import es.us.dad.mysql.entities.Device;
import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
import es.us.dad.mysql.messages.DatabaseMessageType;
import es.us.dad.mysql.messages.DatabaseMethod;
import es.us.dad.mysql.rest.RestEntityMessage;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class ActuatorStatesController extends AbstractController {

	public ActuatorStatesController() {
		super(DatabaseEntity.ActuatorStatus);
	}

	public void start(Promise<Void> startFuture) {

		getVertx().eventBus().consumer(RestEntityMessage.ActuatorStatus.getAddress(), message -> {
			DatabaseMessage databaseMessage = gson.fromJson((String) message.body(), DatabaseMessage.class);
			switch (databaseMessage.getMethod()) {
			case CreateActuatorStatus:
				launchDatabaseOperation(message);
				Actuator actuator = databaseMessage.getRequestBodyAs(Actuator.class);
				launchDatabaseOperation(DatabaseEntity.Device,
						new DatabaseMessage(DatabaseMessageType.UPDATE, DatabaseEntity.Device,
								DatabaseMethod.EditDevice, new Device(actuator.getIdDevice(), null, null, null, null,
										null, Calendar.getInstance().getTimeInMillis())));
				break;
			case DeleteActuatorStatus:
				launchDatabaseOperation(message);
				break;
			case GetLastActuatorStatusFromActuatorId:
				launchDatabaseOperation(message);
				break;
			case GetLatestActuatorStatesFromActuatorId:
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
