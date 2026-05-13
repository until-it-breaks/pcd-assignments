package it.unibo.actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*

object AlarmSystemGuardian {
  import it.unibo.SmartHomeAlarmSystemProtocol
  import it.unibo.SmartHomeAlarmSystemProtocol.*
  import it.unibo.actors.AlarmControlUnitActor.*

  enum Command:
    case SignalDetection(sensorId: String)
    case InputPin(pin: String)
    case ArmCommand(pin: String, zones: Set[Zone])

  export Command.*

  def apply(config: Config, sensorsToCreate: List[Sensor]): Behavior[Command] =
    Behaviors.setup: context =>
      val siren = context.spawn(SirenActor(), "siren")
      val controlUnit = context.spawn(AlarmControlUnitActor(config: Config, siren), "control-unit")
      val keypad = context.spawn(KeypadActor(controlUnit), "keypad")
      val sensorMap = sensorsToCreate.map { sensor =>
        val sensorActor = context.spawn(SensorActor(sensor, controlUnit), s"sensor-${sensor.id}")
        sensor.id -> sensorActor
      }.toMap
      context.log.info("Alarm system initialized with {} sensors", sensorMap.size)
      active(context, controlUnit, keypad, sensorMap)

  private def active(
    context: ActorContext[Command],
    controlUnit: ActorRef[SmartHomeAlarmSystemProtocol.AlarmControlUnitInput],
    keypad: ActorRef[KeypadActor.Command],
    sensors: Map[String, ActorRef[SensorActor.Command]]
  ): Behavior[Command] =
    Behaviors.receiveMessage:
      case SignalDetection(id) =>
        sensors.get(id) match {
          case Some(sensorActor) =>
            sensorActor ! SensorActor.DetectIntrusion
          case None =>
            context.log.warn("Warning: Sensor [{}] not found in system", id)
        }
        Behaviors.same
      case InputPin(pin) =>
        pin.foreach { char =>
         if (char.isDigit) keypad ! KeypadActor.PressDigit(char.asDigit)
        }
        keypad ! KeypadActor.PressEnter
        Behaviors.same
      case ArmCommand(pin, zones) =>
        pin.foreach { char =>
          if (char.isDigit) keypad ! KeypadActor.PressDigit(char.asDigit)
        }
        keypad ! KeypadActor.PressEnterWithZones(zones)
        Behaviors.same
}
