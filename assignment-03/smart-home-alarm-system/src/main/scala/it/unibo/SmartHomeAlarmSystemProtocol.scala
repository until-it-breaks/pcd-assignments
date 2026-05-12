package it.unibo

import org.apache.pekko.actor.typed.ActorRef

object SmartHomeAlarmSystemProtocol {
  enum Zone:
    case Perimeter
    case GroundFloor
    case SleepingArea

  final case class Sensor(id: String, zone: Zone)

  enum AlarmState:
    case Disarmed
    case ExitDelay
    case Armed(activeZones: Set[Zone])
    case EntryDelay
    case Alarm

  enum AlarmSystemInput:
    case SensorTriggered(sensor: Sensor)
    case ArmRequest(pin: String, zonesToArm: Set[Zone])
    case PinEntered(pin: String)
    case ExitTimeout
    case EntryTimeout
    case GetState(replyTo: ActorRef[AlarmState])
}
