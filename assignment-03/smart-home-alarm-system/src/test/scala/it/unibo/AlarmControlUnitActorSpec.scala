package it.unibo

import it.unibo.SmartHomeAlarmSystemProtocol.*
import it.unibo.SmartHomeAlarmSystemProtocol.AlarmControlUnitInput.*
import it.unibo.actors.*
import org.apache.pekko.actor.testkit.typed.scaladsl.*
import org.apache.pekko.actor.typed.ActorRef
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpecLike

class AlarmControlUnitActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  private val CORRECT_PIN = "1234"
  private val WRONG_PIN = "1235"
  private val EXIT_DELAY = 200.milliseconds
  private val ENTRY_DELAY = 100.milliseconds
  private val EXTRA_TIME = 100.milliseconds

  private val config = AlarmControlUnitActor.Config(
    pin = CORRECT_PIN,
    exitDelay = EXIT_DELAY,
    entryDelay = ENTRY_DELAY
  )

  private val allZones = Zone.values.toSet
  private val onlyOneZone = Set(Zone.Perimeter)

  private def setup(): (ActorRef[AlarmControlUnitInput], TestProbe[SirenActor.Command]) = {
    val sirenProbe = createTestProbe[SirenActor.Command]()
    val alarmControlUnitActor = spawn(AlarmControlUnitActor(config, sirenProbe.ref))
    (alarmControlUnitActor, sirenProbe)
  }

  private def setupFullyArmed(): (ActorRef[AlarmControlUnitInput], TestProbe[SirenActor.Command]) = {
    val (alarmControlUnitActor, sirenProbe) = setup()
    alarmControlUnitActor ! ArmRequest(CORRECT_PIN, allZones)
    sirenProbe.expectNoMessage(EXIT_DELAY + EXTRA_TIME)
    (alarmControlUnitActor, sirenProbe)
  }

  private def setupPartiallyArmed(): (ActorRef[AlarmControlUnitInput], TestProbe[SirenActor.Command]) = {
    val (alarmControlUnitActor, sirenProbe) = setup()
    alarmControlUnitActor ! ArmRequest(CORRECT_PIN, onlyOneZone)
    sirenProbe.expectNoMessage(EXIT_DELAY + EXTRA_TIME)
    (alarmControlUnitActor, sirenProbe)
  }

  "The Alarm Control Unit" should {
    "reject wrong PIN and remain in current state" in {
      val (alarmControlUnitActor, sirenProbe) = setup()
      alarmControlUnitActor ! ArmRequest(WRONG_PIN, allZones)
      alarmControlUnitActor ! SensorTriggered(Sensor("door", Zone.Perimeter))
      sirenProbe.expectNoMessage(ENTRY_DELAY + EXTRA_TIME)
    }

    "ignore all sensor triggers while in the Exit Delay state" in {
      val (alarmControlUnitActor, sirenProbe) = setup()
      alarmControlUnitActor ! ArmRequest(CORRECT_PIN, allZones)
      alarmControlUnitActor ! SensorTriggered(Sensor("door", Zone.Perimeter))
      alarmControlUnitActor ! SensorTriggered(Sensor("window", Zone.Perimeter))
      sirenProbe.expectNoMessage(ENTRY_DELAY + EXTRA_TIME)
    }

    "transition to Armed after the Exit Delay expires" in {
      val (alarmControlUnitActor, sirenProbe) = setup()
      alarmControlUnitActor ! ArmRequest(CORRECT_PIN, allZones)
      sirenProbe.expectNoMessage(EXIT_DELAY + EXTRA_TIME)
      alarmControlUnitActor ! SensorTriggered(Sensor("door", Zone.Perimeter))
      sirenProbe.expectMessage(ENTRY_DELAY + EXTRA_TIME, SirenActor.Start)
    }

    "ignore sensor triggers from inactive zones" in {
      val (alarmControlUnitActor, sirenProbe) = setupPartiallyArmed()
      alarmControlUnitActor ! SensorTriggered(Sensor("motion", Zone.GroundFloor))
      sirenProbe.expectNoMessage()
    }

    "allow disarming during the Entry Delay to prevent an alarm" in {
      val (alarmControlUnitActor, sirenProbe) = setupFullyArmed()
      alarmControlUnitActor ! SensorTriggered(Sensor("door", Zone.Perimeter))
      alarmControlUnitActor ! PinEntered(CORRECT_PIN)
      sirenProbe.expectNoMessage(ENTRY_DELAY + EXTRA_TIME)
    }

    "sound the alarm if Entry Delay times out" in {
      val (alarmControlUnitActor, sirenProbe) = setupFullyArmed()
      alarmControlUnitActor ! SensorTriggered(Sensor("door", Zone.Perimeter))
      sirenProbe.expectMessage(ENTRY_DELAY + EXTRA_TIME, SirenActor.Start)
    }

    "stop the siren if the correct PIN is entered" in {
      val (alarmControlUnitActor, sirenProbe) = setupFullyArmed()
      alarmControlUnitActor ! SensorTriggered(Sensor("door", Zone.Perimeter))
      sirenProbe.expectMessage(ENTRY_DELAY + EXTRA_TIME, SirenActor.Start)
      alarmControlUnitActor ! PinEntered(WRONG_PIN)
      sirenProbe.expectNoMessage()
      alarmControlUnitActor ! PinEntered(CORRECT_PIN)
      sirenProbe.expectMessage(SirenActor.Stop)
    }
  }
}
