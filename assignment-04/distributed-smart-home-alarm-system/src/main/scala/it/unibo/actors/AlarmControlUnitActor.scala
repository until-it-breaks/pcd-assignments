package it.unibo.actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.receptionist.*

import scala.concurrent.duration.*

object AlarmControlUnitActor {
  import it.unibo.SmartHomeAlarmSystemProtocol.*
  import it.unibo.SmartHomeAlarmSystemProtocol.AlarmControlUnitInput.*

  final case class Config(pin: String, exitDelay: FiniteDuration, entryDelay: FiniteDuration)

  val ControlUnitKey: ServiceKey[AlarmControlUnitInput] = ServiceKey[AlarmControlUnitInput]("AlarmControlUnitService")

  def apply(
    config: Config,
    siren: ActorRef[SirenActor.Command],
    isRecovery: Boolean = false,
    simulateCrash: Boolean = false
  ): Behavior[AlarmControlUnitInput] =
    Behaviors.setup: context =>
      context.system.receptionist ! Receptionist.Register(ControlUnitKey, context.self)
      if simulateCrash then
        context.scheduleOnce(50.seconds, context.self, ForceRestart)
      Behaviors.withTimers: timers =>
        if (isRecovery)
          context.log.warn("Control Unit restarted due to failure. Entering SAFE RECOVERY state")
          safeRecovery(config, timers, siren)
        else
          context.log.info("Normal system startup. Entering DISARMED state")
          disarmed(config, timers, siren)

  private def safeRecovery(
    config: Config,
    timers: TimerScheduler[AlarmControlUnitInput],
    siren: ActorRef[SirenActor.Command]
  ): Behavior[AlarmControlUnitInput] =
    Behaviors.receive: (context, message) =>
      message match
        case PinEntered(pin) if pin == config.pin =>
          context.log.info("[Safe Recovery] PIN is correct. Entering DISARMED state")
          disarmed(config, timers, siren)
        case PinEntered(_) =>
          context.log.info("[Safe Recovery] Wrong PIN")
          Behaviors.same
        case GetState(replyTo) =>
          replyTo ! AlarmState.SafeRecovery
          Behaviors.same
        case ForceRestart =>
          throw new RuntimeException("Forced restart requested")
        case _ =>
          context.log.info("System is in SAFE RECOVERY state. Ignoring sensor triggers and commands")
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
        case ForceRestart =>
          throw new RuntimeException("Forced restart requested")
        case _ =>
          context.log.info("System is currently DISARMED. Ignoring sensor triggers and commands")
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
          context.log.info("PIN is correct. Cancelling transition to ARMED state")
          timers.cancel(ExitTimeout)
          disarmed(config, timers, siren)
        case PinEntered(_) =>
          context.log.info("PIN is wrong")
          Behaviors.same
        case GetState(replyTo) =>
          replyTo ! AlarmState.ExitDelay
          Behaviors.same
        case ForceRestart =>
          throw new RuntimeException("Forced restart requested")
        case _ =>
          context.log.info("System is arming itself up. Ignoring sensor triggers and commands")
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
          context.log.info("PIN is correct. Entering DISARMED state")
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
        case ForceRestart =>
          throw new RuntimeException("Forced restart requested")
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
          context.log.info("PIN is correct. Countdown to sound the alarm has been stopped. Entering disarmed state")
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
        case ForceRestart =>
          throw new RuntimeException("Forced restart requested")
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
          context.log.info("PIN is correct. Deactivating alarm. Entering DISARMED state")
          siren ! SirenActor.Stop
          disarmed(config, timers, siren)
        case PinEntered(_) =>
          context.log.info("PIN is wrong. Alarm stays on")
          Behaviors.same
        case GetState(replyTo) =>
          replyTo ! AlarmState.Alarm
          Behaviors.same
        case ForceRestart =>
          throw new RuntimeException("Forced restart requested")
        case _ =>
          Behaviors.same
}
