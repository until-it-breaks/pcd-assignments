package it.unibo.actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.receptionist.{Receptionist, ServiceKey}

import scala.concurrent.duration.FiniteDuration

object AlarmControlUnitActor {
  import it.unibo.SmartHomeAlarmSystemProtocol.*
  import it.unibo.SmartHomeAlarmSystemProtocol.AlarmControlUnitInput.*

  final case class Config(pin: String, exitDelay: FiniteDuration, entryDelay: FiniteDuration)

  val ControlUnitKey: ServiceKey[AlarmControlUnitInput] = ServiceKey[AlarmControlUnitInput]("AlarmControlUnitService")

  def apply(
    config: Config,
    siren: ActorRef[SirenActor.Command],
    isRecovery: Boolean = false
  ): Behavior[AlarmControlUnitInput] =
    Behaviors.setup: context =>
      context.system.receptionist ! Receptionist.Register(ControlUnitKey, context.self)
      Behaviors.withTimers: timers =>
        if (isRecovery) {
          context.log.warn("Actor recreated due to failure. Entering Safe Recovery State.")
          safeRecovery(config, timers, siren)
        } else {
          context.log.info("Normal system startup. Entering Disarmed State.")
          disarmed(config, timers, siren)
        }

  private def safeRecovery(
    config: Config,
    timers: TimerScheduler[AlarmControlUnitInput],
    siren: ActorRef[SirenActor.Command]
  ): Behavior[AlarmControlUnitInput] =
    Behaviors.receive: (context, message) =>
      message match
        case PinEntered(pin) if pin == config.pin =>
          context.log.info("[Safe Recovery] Correct PIN entered. System safely cleared to Disarmed state.")
          disarmed(config, timers, siren)
        case PinEntered(_) =>
          context.log.info("[Safe Recovery] Invalid PIN entered during recovery challenge.")
          Behaviors.same
        case GetState(replyTo) =>
          replyTo ! AlarmState.SafeRecovery
          Behaviors.same
        case _ =>
          context.log.info("System is in Safe Recovery Mode. Ignoring sensor triggers and commands.")
          Behaviors.same

  private def disarmed(
    config: Config,
    timers: TimerScheduler[AlarmControlUnitInput],
    siren: ActorRef[SirenActor.Command]
  ): Behavior[AlarmControlUnitInput] =
    Behaviors.receive: (context, message) =>
      message match
        case ArmRequest(pin, zones) if pin == config.pin =>
          val zonesDescription = zones.mkString(", ")
          context.log.info("PIN is correct. Arming zones: [{}] in {}", zonesDescription, config.exitDelay)
          timers.startSingleTimer(ExitTimeout, ExitTimeout, config.exitDelay)
          exitDelay(config, timers, siren, zones)
        case ArmRequest(_, _) =>
          context.log.info("PIN is wrong")
          Behaviors.same
        case GetState(replyTo) =>
          replyTo ! AlarmState.Disarmed
          Behaviors.same
        case _ =>
          context.log.info("System is currently disarmed. Ignoring sensors")
          Behaviors.same

  private def exitDelay(
    config: Config,
    timers: TimerScheduler[AlarmControlUnitInput],
    siren: ActorRef[SirenActor.Command],
    zonesToArm: Set[Zone]
  ): Behavior[AlarmControlUnitInput] =
    Behaviors.receive: (context, message) =>
      message match
        case ExitTimeout =>
          context.log.info("System is now armed")
          armed(config, timers, siren, zonesToArm)
        case PinEntered(pin) if pin == config.pin =>
          context.log.info("PIN is correct. Cancelling transition to armed status")
          timers.cancel(ExitTimeout)
          disarmed(config, timers, siren)
        case PinEntered(_) =>
          context.log.info("PIN is wrong")
          Behaviors.same
        case GetState(replyTo) =>
          replyTo ! AlarmState.ExitDelay
          Behaviors.same
        case _ =>
          context.log.info("System is currently arming itself up. Ignoring sensors")
          Behaviors.same

  private def armed(
    config: Config,
    timers: TimerScheduler[AlarmControlUnitInput],
    siren: ActorRef[SirenActor.Command],
    activeZones: Set[Zone]
  ): Behavior[AlarmControlUnitInput] =
    Behaviors.receive: (context, message) =>
      message match
        case PinEntered(pin) if pin == config.pin =>
          context.log.info("PIN is correct. Transitioning to disarmed status")
          disarmed(config, timers, siren)
        case PinEntered(_) =>
          context.log.info("PIN is wrong")
          Behaviors.same
        case SensorTriggered(sensor) if activeZones.contains(sensor.zone) =>
          context.log.info("Intrusion detected in active zone [{}] by [{}]! Starting countdown of {} before sounding the alarm",
            sensor.zone, sensor.id, config.entryDelay)
          timers.startSingleTimer(EntryTimeout, EntryTimeout, config.entryDelay)
          entryDelay(config, timers, siren)
        case SensorTriggered(sensor) =>
          context.log.info("Ignoring sensor [{}] in inactive zone [{}]", sensor.id, sensor.zone)
          Behaviors.same
        case GetState(replyTo) =>
          replyTo ! AlarmState.Armed(activeZones)
          Behaviors.same
        case _ =>
          Behaviors.same

  private def entryDelay(
    config: Config,
    timers: TimerScheduler[AlarmControlUnitInput],
    siren: ActorRef[SirenActor.Command]
  ): Behavior[AlarmControlUnitInput] =
    Behaviors.receive: (context, message) =>
      message match
        case PinEntered(pin) if pin == config.pin =>
          context.log.info("PIN is correct. Countdown to sound the alarm has been stopped. Transitioning to disarmed status")
          timers.cancel(EntryTimeout)
          disarmed(config, timers, siren)
        case PinEntered(_) =>
          context.log.info("PIN is wrong")
          Behaviors.same
        case EntryTimeout =>
          context.log.info("Time is up. Starting alarm")
          siren ! SirenActor.Start
          alarm(config, timers, siren)
        case GetState(replyTo) =>
          replyTo ! AlarmState.EntryDelay
          Behaviors.same
        case _ =>
          Behaviors.same

  private def alarm(
    config: Config,
    timers: TimerScheduler[AlarmControlUnitInput],
    siren: ActorRef[SirenActor.Command]
  ): Behavior[AlarmControlUnitInput] =
    Behaviors.receive: (context, message) =>
      message match
        case PinEntered(pin) if pin == config.pin =>
          context.log.info("PIN is correct. Deactivating alarm. Transitioning to disarmed status")
          siren ! SirenActor.Stop
          disarmed(config, timers, siren)
        case PinEntered(_) =>
          context.log.info("PIN is wrong")
          Behaviors.same
        case GetState(replyTo) =>
          replyTo ! AlarmState.Alarm
          Behaviors.same
        case _ =>
          Behaviors.same
}
