package it.unibo

object SmartHomeAlarmSystemProtocol {
  enum SensorType:
    case Motion
    case Door
    case Window

  final case class Sensor(id: String, sensorType: SensorType)

  enum AlarmSystemInput:
    case SensorTriggered(sensor: Sensor)
    case PinEntered(pin: String)
    case ExitTimeout
    case EntryTimeout

  export SensorType.*
}
