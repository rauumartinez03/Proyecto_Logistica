package es.us.dad.controllers;

import java.util.List;

import com.google.gson.Gson;

import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;

/**
 * 
 * This class extends @see #AbstracVerticle, so it allows to define Verticles
 * in @see #Vertx that reuse the methods defined in the abstract class. As it is
 * an abstract class, it cannot be instantiated directly, but must be extended
 * by another class which, if necessary, can be implemented. This abstract class
 * allows us to define the controllers of our project by reusing the methods
 * defined in it. Remember that the controllers are used to perform operations
 * against the @see #Verticle to access the database on request of a message
 * sent from the @see #Verticle that deploys the API Rest. This layer is
 * necessary to process complex requests that involve calling more than one
 * method of the data access @see #MySQLVerticle.
 * 
 * @author luismi
 *
 */
public abstract class AbstractController extends AbstractVerticle {

	/**
	 * Enumerated that defines the type of object to be handled by the
	 * AbstractController implementation. It refers to one of the data types managed
	 * by the system. @see #DatabaseEntity
	 */
	private DatabaseEntity databaseEntity;

	/**
	 * Instance of the library in charge of serialising and deserialising the
	 * objects that are managed from this AbstractController. It is defined as
	 * transient in order not to be serialised in case of serialisations of the
	 * AbstractController class.
	 */
	protected transient Gson gson = new Gson();

	/**
	 * Abstract class constructor. It must be invoked in the constructor of the
	 * different classes that extend this abstract class by calling super.
	 * 
	 * @param databaseEntity Type of entity type managed by the controller that is
	 *                       defined by reusing this abstract class
	 */
	public AbstractController(DatabaseEntity databaseEntity) {
		super();
		this.databaseEntity = databaseEntity;
	}

	/**
	 * This method allows sending a message to the verticle in charge of performing
	 * operations against the database. This will require an instance of a Vertx
	 * message to indicate the channel on which the message will be published, as
	 * well as the body of the request. In case of a failure in the request (or in
	 * the response to the request), the original message will be replied to with
	 * the error code 100 and the message of the cause of the exception.
	 * 
	 * @param message Message where the request to be made to the Verticle of
	 *                communication with the database is stored. The body of this
	 *                message must necessarily be of type @see #DatabaseMessage. The
	 *                message publication channel will be given by the channel
	 *                associated to the entity that manages the controller instance.
	 */
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

	/**
	 * Enables the execution of a set of DatabaseMessage against the Data Access
	 * Verticle. These messages shall be executed sequentially in the same order as
	 * they are passed in the message list.
	 * 
	 * @param databaseMessages List of DatabaseMessage with the content of the
	 *                         requests to be made to the database connection
	 *                         Verticle.
	 * @return Promise with the outcome of implementation. As it is an asynchronous
	 *         method, it will not directly return the result of the execution of
	 *         the messages, but it will be done through a promise to which the
	 *         handler can subscribe through the onComplete method of its future:
	 *         promise.future().onComplete(res -> {...});
	 */
	public Promise<List<DatabaseMessage>> launchDatabaseOperations(List<DatabaseMessage> databaseMessages) {
		return ControllersUtils.launchDatabaseOperations(databaseMessages, this.getVertx());
	}

	/**
	 * Posts a message to the channel linked to the databaseEntity entity passed by
	 * parameter.
	 * 
	 * @param databaseEntity  Entity involved in the published message and to whose
	 *                        channel the request indicated in the second parameter
	 *                        shall be sent.
	 * @param databaseMessage Message describing the task to be performed by the
	 *                        database connection Verticle.
	 * @return Promise with the outcome of implementation. As it is an asynchronous
	 *         method, it will not directly return the result of the execution of
	 *         the message, but it will be done through a promise to which the
	 *         handler can subscribe through the onComplete method of its future:
	 *         promise.future().onComplete(res -> {...});
	 */
	public Promise<DatabaseMessage> launchDatabaseOperation(DatabaseEntity databaseEntity,
			DatabaseMessage databaseMessage) {
		return ControllersUtils.launchDatabaseOperation(databaseEntity, databaseMessage, getVertx());
	}

}
