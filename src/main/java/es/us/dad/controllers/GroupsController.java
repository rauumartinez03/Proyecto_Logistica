package es.us.dad.controllers;

import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
import es.us.dad.mysql.rest.RestEntityMessage;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class GroupsController extends AbstractController {

	public GroupsController() {
		super(DatabaseEntity.Group);
	}

	public void start(Promise<Void> startFuture) {
		getVertx().eventBus().consumer(RestEntityMessage.Group.getAddress(), message -> {
			DatabaseMessage databaseMessage = gson.fromJson((String) message.body(), DatabaseMessage.class);
			switch (databaseMessage.getMethod()) {
			case CreateGroup:
				launchDatabaseOperation(message);
				break;
			case GetGroup:
				launchDatabaseOperation(message);
				break;
			case EditGroup:
				launchDatabaseOperation(message);
				break;
			case DeleteGroup:
				launchDatabaseOperation(message);
				break;
			case AddDeviceToGroup:
				launchDatabaseOperation(message);
				break;
			case GetDevicesFromGroupId:
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
