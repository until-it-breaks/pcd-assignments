package it.unibo.actors

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.*

object SirenActor {
  enum Command:
    case Start
    case Stop

  export Command.*

  private enum Status:
    case Sounding
    case Silenced

  def apply(): Behavior[Command] =
    silenced()

  private def silenced(): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match {
        case Start =>
          context.log.info("Siren started")
          sounding()
        case Stop =>
          Behaviors.same
      }

  private def sounding(): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match {
        case Start =>
          Behaviors.same
        case Stop =>
          context.log.info("Siren silenced")
          silenced()
      }
}
