package es.us.dad.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

/**
 * This class needs Mosquitto broker running on localhost. For install Mosquitto
 * broker in your computer, you can follow the instructions in
 * https://mosquitto.org/download/. Default deployment port for Mosquitto is
 * 1883. Mosquitto allows anonymous user request, but mosquitto.conf file must
 * be modified to (MacOS: nano /usr/local/etc/mosquitto/mosquitto.conf, Linux:
 * nano /etc/mosquitto/mosquitto.conf, Windows: edit file located in Mosquitto
 * installation folder). For this purpose, uncomment line where "allow_anonymous
 * true" is defined. If you need connect to this broker from non-localhost device,
 * uncomment this line: "listener 1883 0.0.0.0". You should restart mosquitto service once this change is
 * made (MacOS: brew services restart mosquitto, Windows: net stop mosquitto and
 * net start mosquitto [powershell with admin rights], Linux: sudo systemctl
 * restart mosquitto)
 * 
 * @author luismi
 *
 */
public class MqttClientUtil {

	protected static transient MqttClient mqttClient;

	private static transient MqttClientUtil mqttClientClass = null;

	private MqttClientUtil(Vertx vertx) {
		mqttClient = MqttClient.create(vertx, new MqttClientOptions());
		mqttClient.connect(1883, "localhost", s -> {
			if (s.succeeded()) {
				System.out.println("Sucessfully connected to MQTT brocker");
			} else {
				System.err.println(s.cause());
			}
		});
	}

	public void publishMqttMessage(String topic, String payload, Handler<AsyncResult<Integer>> handler) {
		mqttClient.publish(topic, Buffer.buffer(payload), MqttQoS.AT_LEAST_ONCE, false, false, handler);
	}

	public void subscribeMqttTopic(String topic, Handler<AsyncResult<Integer>> handler) {
		mqttClient.subscribe(topic, MqttQoS.AT_LEAST_ONCE.value(), handler);
	}

	public void unsubscribeMqttTopic(String topic) {
		mqttClient.unsubscribe(topic);
	}

	public static MqttClientUtil getInstance(Vertx vertx) {
		if (mqttClientClass == null) {
			mqttClientClass = new MqttClientUtil(vertx);
		}
		return mqttClientClass;
	}

}
