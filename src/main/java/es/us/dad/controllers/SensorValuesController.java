package es.us.dad.controllers;

import java.util.Calendar;

import es.us.dad.mysql.entities.Device;
import es.us.dad.mysql.entities.Sensor;
import es.us.dad.mysql.entities.SensorValue;
import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
import es.us.dad.mysql.messages.DatabaseMessageType;
import es.us.dad.mysql.messages.DatabaseMethod;
import es.us.dad.mysql.rest.RestEntityMessage;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class SensorValuesController extends AbstractController {

	public SensorValuesController() {
		super(DatabaseEntity.SensorValue);
	}

	public void start(Promise<Void> startFuture) {

		getVertx().eventBus().consumer(RestEntityMessage.SensorValue.getAddress(), message -> {
			DatabaseMessage databaseMessage = gson.fromJson((String) message.body(), DatabaseMessage.class);
			switch (databaseMessage.getMethod()) {
			case CreateSensorValue:
				launchDatabaseOperation(message);

				/*
				 * List<DatabaseMessage> operations = new ArrayList<DatabaseMessage>();
				 * operations.add(gson.fromJson((String) message.body(),
				 * DatabaseMessage.class)); operations.add(gson.fromJson((String)
				 * message.body(), DatabaseMessage.class));
				 * operations.add(gson.fromJson((String) message.body(),
				 * DatabaseMessage.class)); operations.add(gson.fromJson((String)
				 * message.body(), DatabaseMessage.class));
				 * launchDatabaseOperations(operations).future().onComplete(res -> {
				 * System.out.println(res.result().size()); });
				 */

				SensorValue sensorValue = databaseMessage.getRequestBodyAs(SensorValue.class);
				launchDatabaseOperation(DatabaseEntity.Sensor, new DatabaseMessage(DatabaseMessageType.SELECT,
						DatabaseEntity.Sensor, DatabaseMethod.GetSensor, sensorValue.getIdSensor())).future()
								.onComplete(res -> {
									if (res.succeeded()) {
										Sensor sensor = res.result().getResponseBodyAs(Sensor.class);
										if (sensor != null) {
											launchDatabaseOperation(DatabaseEntity.Device,
													new DatabaseMessage(DatabaseMessageType.UPDATE,
															DatabaseEntity.Device, DatabaseMethod.EditDevice,
															new Device(sensor.getIdDevice(), null, null, null, null,
																	null, Calendar.getInstance().getTimeInMillis())));
										}
									}
								});
				break;
			case DeleteSensorValue:
				launchDatabaseOperation(message);
				break;
			case GetLastSensorValueFromSensorId:
				launchDatabaseOperation(message);
				break;
			case GetLatestSensorValuesFromSensorId:
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
