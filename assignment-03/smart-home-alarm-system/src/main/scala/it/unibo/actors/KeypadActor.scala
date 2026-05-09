package it.unibo.actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*

object KeypadActor {
  import it.unibo.SmartHomeAlarmSystemProtocol.*
  import it.unibo.SmartHomeAlarmSystemProtocol.AlarmSystemInput.*

  enum Command:
    case PressDigit(digit: Int)
    case PressEnter
    case PressClear

  export Command.*

  def apply(controlUnit: ActorRef[AlarmSystemInput]): Behavior[Command] =
    active(controlUnit, "")

  private def active(
    controlUnit: ActorRef[AlarmSystemInput],
    currentBuffer: String
  ): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match {
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
            context.log.info("ENTER pressed with empty PIN")
            Behaviors.same
          }
        case PressClear =>
          context.log.info("PIN cleared")
          active(controlUnit, "")
      }
}
