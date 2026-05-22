package it.unibo.actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.receptionist.Receptionist

object SensorActor:
  import it.unibo.SmartHomeAlarmSystemProtocol.*
  import it.unibo.SmartHomeAlarmSystemProtocol.AlarmControlUnitInput.*

  enum Command:
    case DetectIntrusion
    case ListingResponse(listing: Receptionist.Listing)

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
            case Some(ref) =>
              context.log.info("Sensor [{}] discovered Control Unit", sensor.id)
              active(sensor, ref)
            case None => Behaviors.same
        case DetectIntrusion =>
          context.log.info("[Disconnected] Sensor [{}] detected something but it's not connected to Control Unit", sensor.id)
          Behaviors.same
        case _ => Behaviors.same

  private def active(sensor: Sensor, controlUnit: ActorRef[AlarmControlUnitInput]): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        case ListingResponse(AlarmControlUnitActor.ControlUnitKey.Listing(listings)) =>
          listings.headOption match
            case Some(ref) =>
              context.log.warn("Connection to Control Unit updated")
              active(sensor, ref)
            case None =>
              context.log.warn("Sensor [{}] lost connection to Control Unit! Reverting to standby", sensor.id)
              standby(sensor)
        case DetectIntrusion =>
          context.log.info("Sensor [{}] detected something", sensor.id)
          controlUnit ! SensorTriggered(sensor)
          Behaviors.same
        case _ => Behaviors.same
