package it.unibo.actors

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.*

object SirenActor {
  enum Command:
    case Start
    case Stop
    case GetState(replyTo: ActorRef[SirenState])

  export Command.*

  enum SirenState:
    case Sounding
    case Silenced

  export SirenState.*

  def apply(): Behavior[Command] =
    silenced()

  private def silenced(): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        case Start =>
          context.log.info("Siren started")
          sounding()
        case GetState(replyTo) =>
          replyTo ! SirenState.Silenced
          Behaviors.same
        case _ =>
          Behaviors.same

  private def sounding(): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        case Stop =>
          context.log.info("Siren silenced")
          silenced()
        case GetState(replyTo) =>
          replyTo ! SirenState.Sounding
          Behaviors.same
        case _ =>
          Behaviors.same
}
