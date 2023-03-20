package es.us.dad.controllers;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

public class ControllersUtils {
	protected static transient Gson gson = new Gson();

	public static Promise<List<DatabaseMessage>> launchDatabaseOperations(List<DatabaseMessage> databaseMessages,
			Vertx vertx) {
		return launchDatabaseOperationsAux(0, databaseMessages, vertx, 0);
	}

	public static Promise<List<DatabaseMessage>> launchDatabaseOperations(List<DatabaseMessage> databaseMessages,
			Vertx vertx, int delay) {
		return launchDatabaseOperationsAux(0, databaseMessages, vertx, delay);
	}

	private static Promise<List<DatabaseMessage>> launchDatabaseOperationsAux(int currentMessagePosition,
			List<DatabaseMessage> databaseMessages, Vertx vertx, int delay) {
		Promise<List<DatabaseMessage>> result = Future.factory.promise();
		Promise<DatabaseMessage> promise = Promise.promise();
		vertx.setTimer(delay, function -> {
			Promise<DatabaseMessage> promiseAux = launchDatabaseOperation(
					databaseMessages.get(currentMessagePosition).getEntity(),
					databaseMessages.get(currentMessagePosition), vertx);
			promiseAux.future().onComplete(res -> promise.complete(res.result()));
		});

		promise.future().onComplete(res -> {
			if (currentMessagePosition == databaseMessages.size() - 1) {
				List<DatabaseMessage> resPromise = new ArrayList<DatabaseMessage>();
				resPromise.add(0, res.result());
				result.complete(resPromise);
			} else {
				launchDatabaseOperationsAux(currentMessagePosition + 1, databaseMessages, vertx, delay).future()
						.onComplete(resRec -> {
							List<DatabaseMessage> resPromise = resRec.result();
							resPromise.add(0, res.result());
							result.complete(resPromise);
						});
			}
		});
		return result;

	}

	public static Promise<DatabaseMessage> launchDatabaseOperation(DatabaseEntity databaseEntity,
			DatabaseMessage databaseMessage, Vertx vertx) {
		Promise<DatabaseMessage> ret = Promise.promise();
		vertx.eventBus().request(databaseEntity.getAddress(), gson.toJson(databaseMessage), persistenceMessage -> {
			if (persistenceMessage.succeeded()) {
				ret.complete(gson.fromJson(persistenceMessage.result().body().toString(), DatabaseMessage.class));
			} else {
				ret.fail(persistenceMessage.cause());
			}
		});
		return ret;
	}
}
