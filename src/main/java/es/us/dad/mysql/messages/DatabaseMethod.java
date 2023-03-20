package es.us.dad.mysql.messages;

public enum DatabaseMethod {
	CreateGroup,
	GetGroup,
	EditGroup,
	DeleteGroup,
	AddDeviceToGroup,
	GetDevicesFromGroupId,
	
	CreateDevice,
	GetDevice,
	EditDevice,
	DeleteDevice,
	GetSensorsFromDeviceId,
	GetActuatorsFromDeviceId,
	GetSensorsFromDeviceIdAndSensorType,
	GetActuatorsFromDeviceIdAndActuatorType,
	
	CreateSensor,
	GetSensor,
	EditSensor,
	DeleteSensor,
	
	CreateActuator,
	GetActuator,
	EditActuator,
	DeleteActuator,
	
	CreateSensorValue,
	DeleteSensorValue,
	GetLastSensorValueFromSensorId,
	GetLatestSensorValuesFromSensorId,
	
	CreateActuatorStatus,
	DeleteActuatorStatus,
	GetLastActuatorStatusFromActuatorId,
	GetLatestActuatorStatesFromActuatorId,
}
