package it.unibo

import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class SensorActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  import SmartHomeAlarmSystemProtocol.*
  import SmartHomeAlarmSystemProtocol.AlarmControlUnitInput.*
  import actors.SensorActor
  import actors.SensorActor.Command.*

  private def createSensor(sensor: Sensor) = {
    val alarmControlUnitProbe = createTestProbe[AlarmControlUnitInput]()
    val sensorActor = spawn(SensorActor(sensor, alarmControlUnitProbe.ref))
    (sensorActor, alarmControlUnitProbe)
  }

  "The Sensor Actor" should {
    "forward a SensorTriggered message to the control unit on DetectIntrusion" in {
      val sensor = Sensor("door", Zone.Perimeter)
      val (sensorActor, alarmControlUnitProbe) = createSensor(sensor)
      sensorActor ! DetectIntrusion
      alarmControlUnitProbe.expectMessage(SensorTriggered(sensor))
    }

    "remain active and handle multiple intrusion detections" in {
      val sensor = Sensor("motion", Zone.GroundFloor)
      val (sensorActor, alarmControlUnitProbe) = createSensor(sensor)
      sensorActor ! DetectIntrusion
      sensorActor ! DetectIntrusion
      alarmControlUnitProbe.expectMessage(SensorTriggered(sensor))
      alarmControlUnitProbe.expectMessage(SensorTriggered(sensor))
    }

    "include the correct sensor identity in the triggered message" in {
      val sensor = Sensor("window", Zone.Perimeter)
      val (sensorActor, alarmControlUnitProbe) = createSensor(sensor)
      sensorActor ! DetectIntrusion
      alarmControlUnitProbe.expectMessage(SensorTriggered(sensor))
    }
  }
}