package es.us.dad.controllers;

import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
import es.us.dad.mysql.rest.RestEntityMessage;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * Controller associated with the Device entity. It will perform all operations
 * related to this entity at the request of the Verticle deploying the Rest API.
 * This class extends the basic functionality implemented by the
 * AbstractController class.
 * 
 * @author luismi
 *
 */
public class DevicesController extends AbstractController {

	/**
	 * Constructor of the class where the type of entity managed by the class is
	 * indicated to the AbstractController class where the basic functionality of
	 * the controllers is defined.
	 */
	public DevicesController() {
		super(DatabaseEntity.Device);
	}

	/**
	 * Method that allows the Verticle to be launched. This method will deploy the
	 * handler that will later attend the usage requests made by the Rest API. The
	 * channel to which this handler is associated will depend on the entity
	 * controlled by each controller. In this case, the entity is Device. Note that
	 * in this case, the communication channel is not given by the DatabaseEntity
	 * class, but through the RestEntityMessage class. This is due to the need to
	 * define different channel names to avoid overlapping messages coming from the
	 * communication between the controller and the data access layer.
	 */
	public void start(Promise<Void> startFuture) {

		/*
		 * The switch considers all the messages that can be managed by this controller
		 * and delegates them to the launchDatabaseOperation method of the
		 * AbstractController class, which sends the original message to be processed by
		 * the data access Verticle. Note that this switch is not necessary unless there
		 * are methods in which the operations are different from the one described
		 * above. However, for clarity, it has been decided to make explicit the values
		 * of the enumerated DatabaseMethod that manages each controller. This
		 * implementation assumes that the message coming from the Rest API is well
		 * defined so that it can be reused in the request at the data access layer.
		 */
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
				/*
				 * In case the request cannot be handled by this controller, a 401 error code
				 * will be issued in response to the message received from the API Rest.
				 */
				message.fail(401, "Method not allowed");
			}
		});
		startFuture.complete();
	}

	public void stop(Future<Void> stopFuture) throws Exception {
		super.stop(stopFuture);
	}

}
