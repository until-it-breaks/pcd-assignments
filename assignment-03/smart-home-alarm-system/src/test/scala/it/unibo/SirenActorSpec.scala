package it.unibo

import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class SirenActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  import actors.SirenActor
  import actors.SirenActor.*

  private def createSiren() = {
    val sirenActor = spawn(SirenActor())
    val stateProbe = createTestProbe[SirenActor.SirenState]()
    (sirenActor, stateProbe)
  }

  "The Siren Actor" should {
    "start in the Silenced state" in {
      val (sirenActor, stateProbe) = createSiren()
      sirenActor ! GetState(stateProbe.ref)
      stateProbe.expectMessage(SirenState.Silenced)
    }

    "transition to Sounding on Start" in {
      val (sirenActor, stateProbe) = createSiren()
      sirenActor ! Start
      sirenActor ! GetState(stateProbe.ref)
      stateProbe.expectMessage(SirenState.Sounding)
    }

    "transition back to Silenced on Stop" in {
      val (sirenActor, stateProbe) = createSiren()
      sirenActor ! Start
      sirenActor ! Stop
      sirenActor ! GetState(stateProbe.ref)
      stateProbe.expectMessage(SirenState.Silenced)
    }

    "ignore Start when already Sounding" in {
      val (sirenActor, stateProbe) = createSiren()
      sirenActor ! Start
      sirenActor ! Start
      sirenActor ! GetState(stateProbe.ref)
      stateProbe.expectMessage(SirenState.Sounding)
    }

    "ignore Stop when already Silenced" in {
      val (sirenActor, stateProbe) = createSiren()
      sirenActor ! Stop
      sirenActor ! GetState(stateProbe.ref)
      stateProbe.expectMessage(SirenState.Silenced)
    }
  }
}