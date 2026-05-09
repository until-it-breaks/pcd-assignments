package it.unibo.actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*

import scala.concurrent.duration.FiniteDuration

object AlarmControlUnit {
  import it.unibo.SmartHomeAlarmSystemProtocol.*
  import it.unibo.SmartHomeAlarmSystemProtocol.AlarmSystemInput.*

  final case class Config(pin: String, exitDelay: FiniteDuration, entryDelay: FiniteDuration)

  def apply(config: Config, siren: ActorRef[SirenActor.Command]): Behavior[AlarmSystemInput] =
    Behaviors.withTimers: timers =>
      disarmed(config, timers, siren)

  private def disarmed(
    config: Config,
    timers: TimerScheduler[AlarmSystemInput],
    siren: ActorRef[SirenActor.Command]
  ): Behavior[AlarmSystemInput] =
    Behaviors.receive: (context, message) =>
      message match
        case PinEntered(pin) if pin == config.pin =>
          context.log.info("PIN is correct. Transitioning to armed status in {}", config.exitDelay)
          timers.startSingleTimer(ExitTimeout, ExitTimeout, config.exitDelay)
          exitDelay(config, timers, siren)
        case PinEntered(_) =>
          context.log.info("PIN is wrong")
          Behaviors.same
        case _ =>
          context.log.info("System is currently disarmed. Ignoring sensors")
          Behaviors.same

  private def exitDelay(
    config: Config,
    timers: TimerScheduler[AlarmSystemInput],
    siren: ActorRef[SirenActor.Command]
  ): Behavior[AlarmSystemInput] =
    Behaviors.receive: (context, message) =>
      message match
        case ExitTimeout =>
          context.log.info("System is now armed")
          armed(config, timers, siren)
        case PinEntered(pin) if pin == config.pin =>
          context.log.info("PIN is correct. Cancelling transition to armed status")
          timers.cancel(ExitTimeout)
          disarmed(config, timers, siren)
        case PinEntered(_) =>
          context.log.info("PIN is wrong")
          Behaviors.same
        case _ =>
          context.log.info("System is currently arming itself up. Ignoring sensors")
          Behaviors.same

  private def armed(
    config: Config,
    timers: TimerScheduler[AlarmSystemInput],
    siren: ActorRef[SirenActor.Command]
  ): Behavior[AlarmSystemInput] =
    Behaviors.receive: (context, message) =>
      message match
        case PinEntered(pin) if pin == config.pin =>
          context.log.info("PIN is correct. Transitioning to disarmed status")
          disarmed(config, timers, siren)
        case PinEntered(_) =>
          context.log.info("PIN is wrong")
          Behaviors.same
        case SensorTriggered(sensor) =>
          context.log.warn("Intrusion detected by {}! Starting countdown of {} before sounding the alarm",
            sensor.id, config.entryDelay)
          timers.startSingleTimer(EntryTimeout, EntryTimeout, config.entryDelay)
          entryDelay(config, timers, siren)
        case _ =>
          Behaviors.same

  private def entryDelay(
    config: Config,
    timers: TimerScheduler[AlarmSystemInput],
    siren: ActorRef[SirenActor.Command]
  ): Behavior[AlarmSystemInput] =
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
        case _ =>
          Behaviors.same

  private def alarm(
    config: Config,
    timers: TimerScheduler[AlarmSystemInput],
    siren: ActorRef[SirenActor.Command]
  ): Behavior[AlarmSystemInput] =
    Behaviors.receive: (context, message) =>
      message match
        case PinEntered(pin) if pin == config.pin =>
          context.log.info("PIN is correct. Deactivating alarm. Transitioning to disarmed status")
          siren ! SirenActor.Stop
          disarmed(config, timers, siren)
        case PinEntered(_) =>
          context.log.info("PIN is wrong")
          Behaviors.same
        case _ =>
          Behaviors.same
}
