package es.us.dad.controllers;

import java.util.List;

import com.google.gson.Gson;

import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;

public abstract class AbstractController extends AbstractVerticle {

	private DatabaseEntity databaseEntity;
	protected transient Gson gson = new Gson();

	public AbstractController(DatabaseEntity databaseEntity) {
		super();
		this.databaseEntity = databaseEntity;
	}

	protected void launchDatabaseOperation(Message<Object> message) {
		DatabaseMessage databaseMessage = gson.fromJson((String) message.body(), DatabaseMessage.class);
		getVertx().eventBus().request(databaseEntity.getAddress(), gson.toJson(databaseMessage), persistenceMessage -> {
			if (persistenceMessage.succeeded()) {
				message.reply(persistenceMessage.result().body());
			} else {
				message.fail(100, persistenceMessage.cause().getLocalizedMessage());
				System.err.println(persistenceMessage.cause());
			}
		});
	}

	public Promise<List<DatabaseMessage>> launchDatabaseOperations(List<DatabaseMessage> databaseMessages) {
		return ControllersUtils.launchDatabaseOperations(databaseMessages, this.getVertx());
	}

	public Promise<DatabaseMessage> launchDatabaseOperation(DatabaseEntity databaseEntity,
			DatabaseMessage databaseMessage) {
		return ControllersUtils.launchDatabaseOperation(databaseEntity, databaseMessage, getVertx());
	}

}
