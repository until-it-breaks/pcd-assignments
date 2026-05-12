package it.unibo

import it.unibo.SmartHomeAlarmSystemProtocol.*
import it.unibo.SmartHomeAlarmSystemProtocol.AlarmSystemInput.*
import it.unibo.actors.*
import org.apache.pekko.actor.testkit.typed.scaladsl.*
import org.apache.pekko.actor.typed.ActorRef
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpecLike

class AlarmControlUnitSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  private val CORRECT_PIN = "1234"
  private val WRONG_PIN = "1235"
  private val EXIT_DELAY = 200.milliseconds
  private val ENTRY_DELAY = 100.milliseconds
  private val EXTRA_TIME = 100.milliseconds

  private val config = AlarmControlUnit.Config(
    pin = CORRECT_PIN,
    exitDelay = EXIT_DELAY,
    entryDelay = ENTRY_DELAY
  )

  private val allZones = Zone.values.toSet
  private val onlyOneZone = Set(Zone.Perimeter)

  private def setup(): (ActorRef[AlarmSystemInput], TestProbe[SirenActor.Command]) = {
    val sirenProbe = createTestProbe[SirenActor.Command]()
    val alarmControlUnit = spawn(AlarmControlUnit(config, sirenProbe.ref))
    (alarmControlUnit, sirenProbe)
  }

  private def setupFullyArmed(): (ActorRef[AlarmSystemInput], TestProbe[SirenActor.Command]) = {
    val (alarmUnit, sirenProbe) = setup()
    alarmUnit ! ArmRequest(CORRECT_PIN, allZones)
    sirenProbe.expectNoMessage(EXIT_DELAY + EXTRA_TIME)
    (alarmUnit, sirenProbe)
  }

  private def setupPartiallyArmed(): (ActorRef[AlarmSystemInput], TestProbe[SirenActor.Command]) = {
    val (alarmUnit, sirenProbe) = setup()
    alarmUnit ! ArmRequest(CORRECT_PIN, onlyOneZone)
    sirenProbe.expectNoMessage(EXIT_DELAY + EXTRA_TIME)
    (alarmUnit, sirenProbe)
  }

  "The Alarm Control Unit" should {
    "reject wrong PIN and remain in current state" in {
      val (alarmUnit, sirenProbe) = setup()
      alarmUnit ! ArmRequest(WRONG_PIN, allZones)
      alarmUnit ! SensorTriggered(Sensor("door", Zone.Perimeter))
      sirenProbe.expectNoMessage(ENTRY_DELAY + EXTRA_TIME)
    }

    "ignore all sensor triggers while in the Exit Delay state" in {
      val (alarmUnit, sirenProbe) = setup()
      alarmUnit ! ArmRequest(CORRECT_PIN, allZones)
      alarmUnit ! SensorTriggered(Sensor("door", Zone.Perimeter))
      alarmUnit ! SensorTriggered(Sensor("window", Zone.Perimeter))
      sirenProbe.expectNoMessage(ENTRY_DELAY + EXTRA_TIME)
    }

    "transition to Armed after the Exit Delay expires" in {
      val (alarmUnit, sirenProbe) = setup()
      alarmUnit ! ArmRequest(CORRECT_PIN, allZones)
      sirenProbe.expectNoMessage(EXIT_DELAY + EXTRA_TIME)
      alarmUnit ! SensorTriggered(Sensor("door", Zone.Perimeter))
      sirenProbe.expectMessage(ENTRY_DELAY + EXTRA_TIME, SirenActor.Start)
    }

    "ignore sensor triggers from inactive zones" in {
      val (alarmUnit, sirenProbe) = setupPartiallyArmed()
      alarmUnit ! SensorTriggered(Sensor("motion", Zone.GroundFloor))
      sirenProbe.expectNoMessage()
    }

    "allow disarming during the Entry Delay to prevent an alarm" in {
      val (alarmUnit, sirenProbe) = setupFullyArmed()
      alarmUnit ! SensorTriggered(Sensor("door", Zone.Perimeter))
      alarmUnit ! PinEntered(CORRECT_PIN)
      sirenProbe.expectNoMessage(ENTRY_DELAY + EXTRA_TIME)
    }

    "sound the alarm if Entry Delay times out" in {
      val (alarmUnit, sirenProbe) = setupFullyArmed()
      alarmUnit ! SensorTriggered(Sensor("door", Zone.Perimeter))
      sirenProbe.expectMessage(ENTRY_DELAY + EXTRA_TIME, SirenActor.Start)
    }

    "stop the siren if the correct PIN is entered" in {
      val (alarmUnit, sirenProbe) = setupFullyArmed()
      alarmUnit ! SensorTriggered(Sensor("door", Zone.Perimeter))
      sirenProbe.expectMessage(ENTRY_DELAY + EXTRA_TIME, SirenActor.Start)
      alarmUnit ! PinEntered(WRONG_PIN)
      sirenProbe.expectNoMessage()
      alarmUnit ! PinEntered(CORRECT_PIN)
      sirenProbe.expectMessage(SirenActor.Stop)
    }
  }
}
