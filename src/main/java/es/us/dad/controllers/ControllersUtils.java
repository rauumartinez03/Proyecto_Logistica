package es.us.dad.controllers;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

/**
 * 
 * This class offers a number of utility methods for controllers. All the
 * methods are marked as static, so it is not necessary to instantiate the class
 * to make use of them.
 * 
 * @author luismi
 *
 */
public class ControllersUtils {
	protected static transient Gson gson = new Gson();

	/**
	 * 
	 * Executes a set of operations against the data access Verticle. These
	 * operations are defined by the messages that will be sent to said Verticle.
	 * The messages will be sent sequentially, respecting the order defined in the
	 * list passed by parameter. In the event that any message returns an error, the
	 * rest of the messages will continue to be sent, although the response can be
	 * checked in the list of messages returned in the Promise.
	 * 
	 * @param databaseMessages List of DatabaseMessage type messages that will be
	 *                         sent to the database controller to be executed
	 *                         sequentially
	 * @param vertx            Vertx instance that will allow to put in context the
	 *                         calls and make use of the associated event bus.
	 * 
	 * @return Promise with the list of messages processed. DatabaseMessage type
	 *         messages will be the same as those passed by parameter but will
	 *         contain the values of the responses in the response body property
	 */
	public static Promise<List<DatabaseMessage>> launchDatabaseOperations(List<DatabaseMessage> databaseMessages,
			Vertx vertx) {
		return launchDatabaseOperationsAux(0, databaseMessages, vertx, 0);
	}

	/**
	 * 
	 * Executes a set of operations against the data access Verticle. These
	 * operations are defined by the messages that will be sent to said Verticle.
	 * The only difference with respect to the launchDatabaseOperations method is
	 * that this time a delay of @delay milliseconds is inserted between each of the
	 * operations. The messages will be sent sequentially, respecting the order
	 * defined in the list passed by parameter. In the event that any message
	 * returns an error, the rest of the messages will continue to be sent, although
	 * the response can be checked in the list of messages returned in the Promise.
	 * 
	 * @param databaseMessages List of DatabaseMessage type messages that will be
	 *                         sent to the database controller to be executed
	 *                         sequentially
	 * @param vertx            Vertx instance that will allow to put in context the
	 *                         calls and make use of the associated event bus.
	 * @param delay            Delay in milliseconds between each message submission
	 *                         to the database controller.
	 * 
	 * @return Promise with the list of messages processed. DatabaseMessage type
	 *         messages will be the same as those passed by parameter but will
	 *         contain the values of the responses in the response body property
	 */
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

	/**
	 * 
	 * Sends a message to the database controller with the content specified in
	 * 
	 * @databaseMessage and to the channel defined by @databaseEntity
	 * 
	 * @param databaseEntity  Entity on which the indicated operation is performed
	 *                        and through whose channel the message will be sent for
	 *                        processing
	 * @param databaseMessage Content of the message that indicates the operation to
	 *                        be performed and the body of the request with the
	 *                        information necessary to carry out said operation
	 * @param vertx           Vertx instance that will allow to put in context the
	 *                        calls and make use of the associated event bus.
	 * @return
	 */
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
