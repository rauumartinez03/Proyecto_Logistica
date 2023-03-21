package es.us.dad;

import java.util.Arrays;

import es.us.dad.controllers.ActuatorStatesController;
import es.us.dad.controllers.ActuatorsController;
import es.us.dad.controllers.DevicesController;
import es.us.dad.controllers.GroupsController;
import es.us.dad.controllers.SensorValuesController;
import es.us.dad.controllers.SensorsController;
import es.us.dad.mysql.MySQLVerticle;
import es.us.dad.mysql.rest.RestAPIVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * Launcher in charge of launching all the Verticles necessary for the execution
 * of the project. It is necessary to make the changes in the run configuration
 * so that the default Verticle at launch is this: "run es.us.dad.Launcher"
 * 
 * @author luismi
 *
 */
public class Launcher extends AbstractVerticle {

	@Override
	public void start(Promise<Void> startFuture) throws Exception {
		Promise<Void> deviceVerticle = Promise.promise();
		Promise<Void> sensorVerticle = Promise.promise();
		Promise<Void> actuatorVerticle = Promise.promise();
		Promise<Void> groupVerticle = Promise.promise();
		Promise<Void> sensorValueVerticle = Promise.promise();
		Promise<Void> actuatorStatusVerticle = Promise.promise();
		Promise<Void> databaseVerticle = Promise.promise();
		Promise<Void> restApiVerticle = Promise.promise();

		CompositeFuture compositeFuture = CompositeFuture
				.all(Arrays.asList(deviceVerticle.future(), sensorVerticle.future(), actuatorVerticle.future(),
						groupVerticle.future(), sensorValueVerticle.future(), actuatorStatusVerticle.future(),
						databaseVerticle.future(), restApiVerticle.future()));

		compositeFuture.onComplete(handler -> {
			if (handler.succeeded())
				startFuture.complete();
			else
				startFuture.fail(handler.cause());
		});

		vertx.deployVerticle(new DevicesController(), handlerController -> {
			if (handlerController.succeeded())
				deviceVerticle.complete();
			else
				deviceVerticle.fail(handlerController.cause());
		});
		vertx.deployVerticle(new SensorsController(), handlerController -> {
			if (handlerController.succeeded())
				sensorVerticle.complete();
			else
				sensorVerticle.fail(handlerController.cause());
		});
		vertx.deployVerticle(new ActuatorsController(), handlerController -> {
			if (handlerController.succeeded())
				actuatorVerticle.complete();
			else
				actuatorVerticle.fail(handlerController.cause());
		});
		vertx.deployVerticle(new GroupsController(), handlerController -> {
			if (handlerController.succeeded())
				groupVerticle.complete();
			else
				groupVerticle.fail(handlerController.cause());
		});
		vertx.deployVerticle(new SensorValuesController(), handlerController -> {
			if (handlerController.succeeded())
				sensorValueVerticle.complete();
			else
				sensorValueVerticle.fail(handlerController.cause());
		});
		vertx.deployVerticle(new ActuatorStatesController(), handlerController -> {
			if (handlerController.succeeded())
				actuatorStatusVerticle.complete();
			else
				actuatorStatusVerticle.fail(handlerController.cause());
		});
		vertx.deployVerticle(new MySQLVerticle(), handlerController -> {
			if (handlerController.succeeded())
				databaseVerticle.complete();
			else
				databaseVerticle.fail(handlerController.cause());
		});
		vertx.deployVerticle(new RestAPIVerticle(), handlerController -> {
			if (handlerController.succeeded())
				restApiVerticle.complete();
			else
				restApiVerticle.fail(handlerController.cause());
		});
	}

	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		super.stop(stopFuture);
	}

}
