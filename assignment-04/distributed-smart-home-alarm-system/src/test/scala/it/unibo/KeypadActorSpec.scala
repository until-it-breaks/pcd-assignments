package it.unibo

import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpecLike

class KeypadActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  import SmartHomeAlarmSystemProtocol.*
  import SmartHomeAlarmSystemProtocol.AlarmControlUnitInput.*
  import actors.KeypadActor
  import actors.KeypadActor.Command.*

  private def createKeypad() = {
    val alarmControlUnitProbe = createTestProbe[AlarmControlUnitInput]()
    val keypadActor = spawn(KeypadActor(alarmControlUnitProbe.ref))
    (keypadActor, alarmControlUnitProbe)
  }

  "The Keypad Actor" should {
    "accumulate digits and forward the PIN on PressEnter" in {
      val (keypadActor, alarmControlUnitProbe) = createKeypad()
      keypadActor ! PressDigit(1)
      keypadActor ! PressDigit(2)
      keypadActor ! PressDigit(3)
      keypadActor ! PressDigit(4)
      keypadActor ! PressEnter
      alarmControlUnitProbe.expectMessage(PinEntered("1234"))
    }

    "correctly handle interleaved digits and clears" in {
      val (keypadActor, alarmControlUnitProbe) = createKeypad()
      keypadActor ! PressDigit(1)
      keypadActor ! PressClear
      keypadActor ! PressDigit(2)
      keypadActor ! PressEnter
      alarmControlUnitProbe.expectMessage(PinEntered("2"))
    }

    "forward the PIN with zones on PressEnterWithZones" in {
      val (keypadActor, alarmControlUnitProbe) = createKeypad()
      val zones = Set(Zone.Perimeter, Zone.GroundFloor)
      keypadActor ! PressDigit(1)
      keypadActor ! PressDigit(2)
      keypadActor ! PressEnterWithZones(zones)
      alarmControlUnitProbe.expectMessage(ArmRequest("12", zones))
    }

    "clear the buffer after PressEnter so a consecutive PressEnter sends nothing" in {
      val (keypadActor, alarmControlUnitProbe) = createKeypad()
      keypadActor ! PressDigit(1)
      keypadActor ! PressEnter
      keypadActor ! PressEnter
      alarmControlUnitProbe.expectMessage(PinEntered("1"))
      alarmControlUnitProbe.expectNoMessage(100.milliseconds)
    }

    "clear the buffer after PressEnterWithZones so a consecutive PressEnter sends nothing" in {
      val (keypadActor, alarmControlUnitProbe) = createKeypad()
      keypadActor ! PressDigit(1)
      keypadActor ! PressEnterWithZones(Set(Zone.Perimeter))
      keypadActor ! PressEnter
      alarmControlUnitProbe.expectMessage(ArmRequest("1", Set(Zone.Perimeter)))
      alarmControlUnitProbe.expectNoMessage(100.milliseconds)
    }

    "not send anything on PressEnter with an empty buffer" in {
      val (keypadActor, alarmControlUnitProbe) = createKeypad()
      keypadActor ! PressEnter
      alarmControlUnitProbe.expectNoMessage(100.milliseconds)
    }

    "not send anything on PressEnterWithZones with an empty buffer" in {
      val (keypadActor, alarmControlUnitProbe) = createKeypad()
      keypadActor ! PressEnterWithZones(Set(Zone.Perimeter))
      alarmControlUnitProbe.expectNoMessage(100.milliseconds)
    }

    "not send anything on PressEnter after on PressClear" in {
      val (keypadActor, alarmControlUnitProbe) = createKeypad()
      keypadActor ! PressDigit(1)
      keypadActor ! PressDigit(1)
      keypadActor ! PressClear
      keypadActor ! PressEnter
      alarmControlUnitProbe.expectNoMessage(100.milliseconds)
    }
  }
}