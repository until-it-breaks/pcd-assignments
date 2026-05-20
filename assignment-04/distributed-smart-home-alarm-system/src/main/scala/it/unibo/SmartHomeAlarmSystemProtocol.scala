package it.unibo

import org.apache.pekko.actor.typed.ActorRef

object SmartHomeAlarmSystemProtocol {

  trait CborSerializable

  sealed trait Zone extends CborSerializable

  object Zone:
    case object Perimeter     extends Zone
    case object GroundFloor   extends Zone
    case object SleepingArea  extends Zone

  final case class Sensor(id: String, zone: Zone) extends CborSerializable

  sealed trait AlarmState extends CborSerializable

  object AlarmState:
    case object Disarmed                                            extends AlarmState
    case object ExitDelay                                           extends AlarmState
    final case class Armed(activeZones: Set[Zone])                  extends AlarmState
    case object EntryDelay                                          extends AlarmState
    case object Alarm                                               extends AlarmState
    case object SafeRecovery                                        extends AlarmState

  sealed trait AlarmControlUnitInput extends CborSerializable

  object AlarmControlUnitInput:
    final case class SensorTriggered(sensor: Sensor)                extends AlarmControlUnitInput
    final case class ArmRequest(pin: String, zonesToArm: Set[Zone]) extends AlarmControlUnitInput
    final case class PinEntered(pin: String)                        extends AlarmControlUnitInput
    case object ExitTimeout                                         extends AlarmControlUnitInput
    case object EntryTimeout                                        extends AlarmControlUnitInput
    final case class GetState(replyTo: ActorRef[AlarmState])        extends AlarmControlUnitInput
}
