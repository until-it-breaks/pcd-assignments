package it.unibo

import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class SirenActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  import actors.SirenActor
  import actors.SirenActor.*

  private def createSiren() = {
    val siren = spawn(SirenActor())
    val stateProbe = createTestProbe[SirenActor.SirenState]()
    (siren, stateProbe)
  }

  "The Siren Actor" should {
    "start in the Silenced state" in {
      val (siren, stateProbe) = createSiren()
      siren ! GetState(stateProbe.ref)
      stateProbe.expectMessage(SirenState.Silenced)
    }

    "transition to Sounding on Start" in {
      val (siren, stateProbe) = createSiren()
      siren ! Start
      siren ! GetState(stateProbe.ref)
      stateProbe.expectMessage(SirenState.Sounding)
    }

    "transition back to Silenced on Stop" in {
      val (siren, stateProbe) = createSiren()
      siren ! Start
      siren ! Stop
      siren ! GetState(stateProbe.ref)
      stateProbe.expectMessage(SirenState.Silenced)
    }

    "ignore Start when already Sounding" in {
      val (siren, stateProbe) = createSiren()
      siren ! Start
      siren ! Start
      siren ! GetState(stateProbe.ref)
      stateProbe.expectMessage(SirenState.Sounding)
    }

    "ignore Stop when already Silenced" in {
      val (siren, stateProbe) = createSiren()
      siren ! Stop
      siren ! GetState(stateProbe.ref)
      stateProbe.expectMessage(SirenState.Silenced)
    }
  }
}