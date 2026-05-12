package it.unibo

import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpecLike

class KeypadActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  import SmartHomeAlarmSystemProtocol.*
  import SmartHomeAlarmSystemProtocol.AlarmSystemInput.*
  import actors.KeypadActor
  import actors.KeypadActor.Command.*

  private def createKeypad() = {
    val controlProbe = createTestProbe[AlarmSystemInput]()
    val keypad = spawn(KeypadActor(controlProbe.ref))
    (keypad, controlProbe)
  }

  "The Keypad Actor" should {
    "accumulate digits and forward the PIN on PressEnter" in {
      val (keypad, controlProbe) = createKeypad()
      keypad ! PressDigit(1)
      keypad ! PressDigit(2)
      keypad ! PressDigit(3)
      keypad ! PressDigit(4)
      keypad ! PressEnter
      controlProbe.expectMessage(PinEntered("1234"))
    }

    "correctly handle interleaved digits and clears" in {
      val (keypad, controlProbe) = createKeypad()
      keypad ! PressDigit(1)
      keypad ! PressClear
      keypad ! PressDigit(2)
      keypad ! PressEnter
      controlProbe.expectMessage(PinEntered("2"))
    }

    "forward the PIN with zones on PressEnterWithZones" in {
      val (keypad, controlProbe) = createKeypad()
      val zones = Set(Zone.Perimeter, Zone.GroundFloor)
      keypad ! PressDigit(1)
      keypad ! PressDigit(2)
      keypad ! PressEnterWithZones(zones)
      controlProbe.expectMessage(ArmRequest("12", zones))
    }

    "clear the buffer after PressEnter so a consecutive PressEnter sends nothing" in {
      val (keypad, controlProbe) = createKeypad()
      keypad ! PressDigit(1)
      keypad ! PressEnter
      keypad ! PressEnter
      controlProbe.expectMessage(PinEntered("1"))
      controlProbe.expectNoMessage(100.milliseconds)
    }

    "clear the buffer after PressEnterWithZones so a consecutive PressEnter sends nothing" in {
      val (keypad, controlProbe) = createKeypad()
      keypad ! PressDigit(1)
      keypad ! PressEnterWithZones(Set(Zone.Perimeter))
      keypad ! PressEnter
      controlProbe.expectMessage(ArmRequest("1", Set(Zone.Perimeter)))
      controlProbe.expectNoMessage(100.milliseconds)
    }

    "not send anything on PressEnter with an empty buffer" in {
      val (keypad, controlProbe) = createKeypad()
      keypad ! PressEnter
      controlProbe.expectNoMessage(100.milliseconds)
    }

    "not send anything on PressEnterWithZones with an empty buffer" in {
      val (keypad, controlProbe) = createKeypad()
      keypad ! PressEnterWithZones(Set(Zone.Perimeter))
      controlProbe.expectNoMessage(100.milliseconds)
    }

    "not send anything on PressEnter after on PressClear" in {
      val (keypad, controlProbe) = createKeypad()
      keypad ! PressDigit(1)
      keypad ! PressDigit(1)
      keypad ! PressClear
      keypad ! PressEnter
      controlProbe.expectNoMessage(100.milliseconds)
    }
  }
}