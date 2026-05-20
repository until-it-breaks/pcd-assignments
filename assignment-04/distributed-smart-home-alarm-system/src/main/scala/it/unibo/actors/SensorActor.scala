package it.unibo.actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.receptionist.Receptionist

object SensorActor:
  import it.unibo.SmartHomeAlarmSystemProtocol.*
  import it.unibo.SmartHomeAlarmSystemProtocol.AlarmControlUnitInput.*

  sealed trait Command
  object Command:
    case object DetectIntrusion                               extends Command with CborSerializable
    case class ListingResponse(listing: Receptionist.Listing) extends Command

  export Command.*

  def apply(sensor: Sensor): Behavior[Command] =
    Behaviors.setup: context =>
      val listingAdapter = context.messageAdapter[Receptionist.Listing](ListingResponse.apply)
      context.system.receptionist ! Receptionist.Subscribe(AlarmControlUnitActor.ControlUnitKey, listingAdapter)
      standby(sensor)

  private def standby(sensor: Sensor): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        case ListingResponse(AlarmControlUnitActor.ControlUnitKey.Listing(listings)) =>
          listings.headOption match
            case Some(controlUnitRef) =>
              context.log.info("Sensor [{}] discovered Alarm Control Unit", sensor.id)
              active(sensor, controlUnitRef)
            case None => Behaviors.same
        case DetectIntrusion =>
          context.log.warn("[Disconnected] Sensor [{}] detected movement/opening but it's not yet connected to Alarm Control Unit", sensor.id)
          Behaviors.same
        case _ => Behaviors.same

  private def active(sensor: Sensor, controlUnit: ActorRef[AlarmControlUnitInput]): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        case ListingResponse(AlarmControlUnitActor.ControlUnitKey.Listing(listings)) =>
          listings.headOption match
            case Some(controlUnitRef) =>
              context.log.warn("Alarm Control Unit updated (possibly restarted)")
              active(sensor, controlUnitRef)
            case None =>
              context.log.warn("Sensor [{}] lost connection to the Alarm Control Unit! Reverting to standby.", sensor.id)
              standby(sensor)
        case DetectIntrusion =>
          context.log.info("Sensor [{}] detected movement/opening!", sensor.id)
          controlUnit ! SensorTriggered(sensor)
          Behaviors.same
        case _ => Behaviors.same
