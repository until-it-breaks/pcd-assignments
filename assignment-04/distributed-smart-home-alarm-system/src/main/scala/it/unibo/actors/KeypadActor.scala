package it.unibo.actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.receptionist.Receptionist

object KeypadActor:
  import it.unibo.SmartHomeAlarmSystemProtocol.*
  import it.unibo.SmartHomeAlarmSystemProtocol.AlarmControlUnitInput.*

  sealed trait Command
  object Command:
    case class PressDigit(digit: Int)                         extends Command with CborSerializable
    case object PressEnter                                    extends Command with CborSerializable
    case class PressEnterWithZones(zones: Set[Zone])          extends Command with CborSerializable
    case object PressClear                                    extends Command with CborSerializable
    case class ListingResponse(listing: Receptionist.Listing) extends Command

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
            case Some(controlUnitRef) =>
              context.log.info("Keypad discovered Alarm Control Unit")
              active(controlUnitRef, currentBuffer)
            case None => Behaviors.same
        case PressDigit(digit) =>
          val updatedBuffer = currentBuffer + digit.toString
          context.log.info("[Disconnected] Current PIN: {}", "*".repeat(updatedBuffer.length))
          standby(updatedBuffer)
        case PressClear =>
          context.log.info("[Disconnected] PIN cleared")
          standby("")
        case PressEnter | PressEnterWithZones(_) =>
          context.log.info("[Disconnected] Cannot submit PIN. Not connected to Alarm Control Unit yet")
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
            case Some(controlUnitRef) =>
              context.log.warn("Alarm Control Unit updated (possibly restarted)")
              active(controlUnitRef, currentBuffer)
            case None =>
              context.log.warn("Alarm Control Unit connection lost! Reverting to standby.")
              standby(currentBuffer)
        case PressDigit(digit) =>
          val updatedBuffer = currentBuffer + digit.toString
          context.log.info("Current PIN: {}", "*".repeat(updatedBuffer.length))
          active(controlUnit, updatedBuffer)
        case PressEnter =>
          if currentBuffer.nonEmpty then {
            context.log.info("Submitting PIN to Alarm Control Unit")
            controlUnit ! PinEntered(currentBuffer)
            active(controlUnit, "")
          } else {
            context.log.info("Pressed ENTER with empty PIN")
            Behaviors.same
          }
        case PressEnterWithZones(zones) =>
          if currentBuffer.nonEmpty then {
            context.log.info("Submitting PIN with zones to Alarm Control Unit")
            controlUnit ! ArmRequest(currentBuffer, zones)
            active(controlUnit, "")
          } else {
            context.log.info("Pressed ENTER with zones and empty PIN")
            Behaviors.same
          }
        case PressClear =>
          context.log.info("PIN cleared")
          active(controlUnit, "")
        case _ => Behaviors.same