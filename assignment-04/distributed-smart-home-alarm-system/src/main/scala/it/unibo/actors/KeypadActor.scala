package it.unibo.actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.receptionist.Receptionist

object KeypadActor:
  import it.unibo.SmartHomeAlarmSystemProtocol.*
  import it.unibo.SmartHomeAlarmSystemProtocol.AlarmControlUnitInput.*

  enum Command:
    case PressDigit(digit: Int)
    case PressEnter
    case PressEnterWithZones(zones: Set[Zone])
    case PressClear
    case ListingResponse(listing: Receptionist.Listing)

  export Command.*

  def apply(): Behavior[Command] =
    Behaviors.setup: context =>
      val listingAdapter = context.messageAdapter[Receptionist.Listing](ListingResponse.apply)
      context.system.receptionist ! Receptionist.Subscribe(AlarmControlUnitActor.ControlUnitKey, listingAdapter)
      standby("")

  private def standby(currentBuffer: String): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        case ListingResponse(AlarmControlUnitActor.ControlUnitKey.Listing(listings)) =>
          listings.headOption match
            case Some(ref) =>
              context.log.info("Keypad discovered Control Unit")
              active(ref, currentBuffer)
            case None => Behaviors.same
        case PressDigit(digit) =>
          val updatedBuffer = currentBuffer + digit.toString
          context.log.info("[Disconnected] Digit inserted. Current PIN: {}", "*".repeat(updatedBuffer.length))
          standby(updatedBuffer)
        case PressClear =>
          context.log.info("[Disconnected] PIN cleared")
          standby("")
        case PressEnter | PressEnterWithZones(_) =>
          context.log.info("[Disconnected] Cannot submit PIN. Keypad is not connected to Control Unit")
          Behaviors.same
        case _ => Behaviors.same

  private def active(
    controlUnit: ActorRef[AlarmControlUnitInput],
    currentBuffer: String
  ): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        case ListingResponse(AlarmControlUnitActor.ControlUnitKey.Listing(listings)) =>
          listings.headOption match
            case Some(ref) =>
              context.log.warn("Connection to Control Unit updated")
              active(ref, currentBuffer)
            case None =>
              context.log.warn("Connection to Control Unit lost! Reverting to standby")
              standby(currentBuffer)
        case PressDigit(digit) =>
          val updatedBuffer = currentBuffer + digit.toString
          context.log.info("Digit inserted. Current PIN: {}", "*".repeat(updatedBuffer.length))
          active(controlUnit, updatedBuffer)
        case PressEnter =>
          if currentBuffer.nonEmpty then
            context.log.info("Submitting PIN to Control Unit")
            controlUnit ! PinEntered(currentBuffer)
            active(controlUnit, "")
          else
            context.log.info("ENTER pressed with empty PIN")
            Behaviors.same
        case PressEnterWithZones(zones) =>
          if currentBuffer.nonEmpty then
            context.log.info("Submitting PIN with zones to Control Unit")
            controlUnit ! ArmRequest(currentBuffer, zones)
            active(controlUnit, "")
          else
            context.log.info("ENTER pressed with zones and empty PIN")
            Behaviors.same
        case PressClear =>
          context.log.info("PIN cleared")
          active(controlUnit, "")
        case _ => Behaviors.same