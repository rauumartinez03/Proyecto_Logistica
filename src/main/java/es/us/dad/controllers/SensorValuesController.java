package es.us.dad.controllers;

import java.util.Calendar;

import es.us.dad.mqtt.MqttClientUtil;
import es.us.dad.mysql.entities.Device;
import es.us.dad.mysql.entities.Group;
import es.us.dad.mysql.entities.Sensor;
import es.us.dad.mysql.entities.SensorValue;
import es.us.dad.mysql.messages.DatabaseEntity;
import es.us.dad.mysql.messages.DatabaseMessage;
import es.us.dad.mysql.messages.DatabaseMessageType;
import es.us.dad.mysql.messages.DatabaseMethod;
import es.us.dad.mysql.rest.RestEntityMessage;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * Controller associated with the SensorValue entity. It will perform all
 * operations related to this entity at the request of the Verticle deploying
 * the Rest API. This class extends the basic functionality implemented by the
 * AbstractController class.
 * 
 * @author luismi
 *
 */
public class SensorValuesController extends AbstractController {

	/**
	 * Constructor of the class where the type of entity managed by the class is
	 * indicated to the AbstractController class where the basic functionality of
	 * the controllers is defined.
	 */
	public SensorValuesController() {
		super(DatabaseEntity.SensorValue);
	}

	/**
	 * Method that allows the Verticle to be launched. This method will deploy the
	 * handler that will later attend the usage requests made by the Rest API. The
	 * channel to which this handler is associated will depend on the entity
	 * controlled by each controller. In this case, the entity is SensorValue. Note
	 * that in this case, the communication channel is not given by the
	 * DatabaseEntity class, but through the RestEntityMessage class. This is due to
	 * the need to define different channel names to avoid overlapping messages
	 * coming from the communication between the controller and the data access
	 * layer.
	 */
	public void start(Promise<Void> startFuture) {
		MqttClientUtil mqttClientUtil = MqttClientUtil.getInstance(vertx);
		getVertx().eventBus().consumer(RestEntityMessage.SensorValue.getAddress(), message -> {
			DatabaseMessage databaseMessage = gson.fromJson((String) message.body(), DatabaseMessage.class);
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
			switch (databaseMessage.getMethod()) {
			case CreateSensorValue:
				launchDatabaseOperation(message);
				SensorValue sensorValue = databaseMessage.getRequestBodyAs(SensorValue.class);

				// Getting sensor entity from idSensor property present in SensorValue
				launchDatabaseOperation(DatabaseEntity.Sensor, new DatabaseMessage(DatabaseMessageType.SELECT,
						DatabaseEntity.Sensor, DatabaseMethod.GetSensor, sensorValue.getIdSensor())).future()
						.onComplete(res -> {
							if (res.succeeded()) {
								Sensor sensor = res.result().getResponseBodyAs(Sensor.class);
								if (sensor != null) {

									// Getting device entity from idDevice property present in Sensor
									launchDatabaseOperation(DatabaseEntity.Device,
											new DatabaseMessage(DatabaseMessageType.SELECT, DatabaseEntity.Device,
													DatabaseMethod.GetDevice, sensor.getIdDevice()))
											.future().onComplete(resDevice -> {
												Device device = resDevice.result().getResponseBodyAs(Device.class);
												if (resDevice.succeeded()) {
													// Publish MQTT Message in device MQTT topic
													mqttClientUtil.publishMqttMessage(device.getMqttChannel(),
															gson.toJson(sensorValue), handler -> {
																System.out.println(handler.result());
															});

													// Getting group entity from idGroup property present in Device
													launchDatabaseOperation(DatabaseEntity.Group,
															new DatabaseMessage(DatabaseMessageType.SELECT,
																	DatabaseEntity.Group, DatabaseMethod.GetGroup,
																	device.getIdGroup()))
															.future().onComplete(resGroup -> {
																Group group = resGroup.result()
																		.getResponseBodyAs(Group.class);
																if (resGroup.succeeded()) {
																	// Publish MQTT Message in group' MQTT topic
																	// TODO: implements business logic here (publish
																	// some readable message for devices in group)
																	mqttClientUtil.publishMqttMessage(
																			group.getMqttChannel(),
																			gson.toJson(sensorValue), handler -> {
																				System.out.println(handler.result());
																			});
																}
															});

												}
											});

									// Updates device entity with the current timestamp where last sensor has
									// been modified
									launchDatabaseOperation(DatabaseEntity.Device,
											new DatabaseMessage(DatabaseMessageType.UPDATE, DatabaseEntity.Device,
													DatabaseMethod.EditDevice,
													new Device(sensor.getIdDevice(), null, null, null, null, null,
															Calendar.getInstance().getTimeInMillis())));
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