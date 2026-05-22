package it.unibo.actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*

object AlarmSystemGuardian:
  import it.unibo.SmartHomeAlarmSystemProtocol.*
  import it.unibo.actors.AlarmControlUnitActor.*

  enum Command:
    case SignalDetection(sensorId: String)
    case InputPin(pin: String)
    case ArmCommand(pin: String, zones: Set[Zone])

  export Command.*

  def controlUnitNode(config: AlarmControlUnitActor.Config, simulateCrash: Boolean = false): Behavior[Nothing] =
    Behaviors.setup: context =>
      context.log.info("Initializing Control Unit Node...", simulateCrash)
      val siren = context.spawn(SirenActor(), "siren")

      def spawnUnit(isRecovery: Boolean): Unit =
        val ref = context.spawn(
          AlarmControlUnitActor(config, siren, isRecovery, simulateCrash),
          "control-unit"
        )
        context.watch(ref)

      spawnUnit(isRecovery = false)

      Behaviors.receiveSignal:
        case (context, Terminated(_)) =>
          context.log.error("Alarm Control Unit crashed! Restarting in Safe Recovery Mode...")
          siren ! SirenActor.Stop
          spawnUnit(isRecovery = true)
          Behaviors.same

  def keypadNode(): Behavior[Command] =
    Behaviors.setup: context =>
      context.log.info("Initializing Keypad Node...")
      val keypad = context.spawn(KeypadActor(), "keypad")

      Behaviors.receiveMessage:
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
        case _ =>
          Behaviors.same

  def sensorsNode(sensorsToCreate: List[Sensor]): Behavior[Command] =
    Behaviors.setup: context =>
      context.log.info("Initializing Sensors Node with {} sensors", sensorsToCreate.size)
      val sensorMap = sensorsToCreate.map { sensor =>
        val sensorActor = context.spawn(SensorActor(sensor), s"sensor-${sensor.id}")
        sensor.id -> sensorActor
      }.toMap

      Behaviors.receiveMessage:
        case SignalDetection(id) =>
          sensorMap.get(id) match
            case Some(sensorActor) =>
              sensorActor ! SensorActor.DetectIntrusion
            case None =>
              context.log.warn("Sensor [{}] not found in system", id)
          Behaviors.same
        case _ =>
          Behaviors.same