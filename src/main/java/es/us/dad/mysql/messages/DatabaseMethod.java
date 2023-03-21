package es.us.dad.mysql.messages;

/**
 * This enum describes all the operations that can be performed at the
 * database level using the Verticle deployed for this purpose. In case of
 * needing any additional operation, they must take into account both this
 * enumeration, as well as in the Verticle of access to the database and the
 * associated controller.
 * 
 * @author luismi
 *
 */
public enum DatabaseMethod {
	// Group operations
	CreateGroup, GetGroup, EditGroup, DeleteGroup, AddDeviceToGroup, GetDevicesFromGroupId,

	// Device operations
	CreateDevice, GetDevice, EditDevice, DeleteDevice, GetSensorsFromDeviceId, GetActuatorsFromDeviceId,
	GetSensorsFromDeviceIdAndSensorType, GetActuatorsFromDeviceIdAndActuatorType,

	// Sensor operations
	CreateSensor, GetSensor, EditSensor, DeleteSensor,

	// Actuator operations
	CreateActuator, GetActuator, EditActuator, DeleteActuator,

	// Sensor value operations
	CreateSensorValue, DeleteSensorValue, GetLastSensorValueFromSensorId, GetLatestSensorValuesFromSensorId,

	// Actuator status operations
	CreateActuatorStatus, DeleteActuatorStatus, GetLastActuatorStatusFromActuatorId,
	GetLatestActuatorStatesFromActuatorId,
}
