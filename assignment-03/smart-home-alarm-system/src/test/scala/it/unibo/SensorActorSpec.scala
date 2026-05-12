package it.unibo

import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class SensorActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  import SmartHomeAlarmSystemProtocol.*
  import SmartHomeAlarmSystemProtocol.AlarmSystemInput.*
  import actors.SensorActor
  import actors.SensorActor.Command.*

  private def createSensor(sensor: Sensor) = {
    val controlProbe = createTestProbe[AlarmSystemInput]()
    val sensorActor = spawn(SensorActor(sensor, controlProbe.ref))
    (sensorActor, controlProbe)
  }

  "The Sensor Actor" should {
    "forward a SensorTriggered message to the control unit on DetectIntrusion" in {
      val sensor = Sensor("door", Zone.Perimeter)
      val (sensorActor, controlProbe) = createSensor(sensor)
      sensorActor ! DetectIntrusion
      controlProbe.expectMessage(SensorTriggered(sensor))
    }

    "remain active and handle multiple intrusion detections" in {
      val sensor = Sensor("motion", Zone.GroundFloor)
      val (sensorActor, controlProbe) = createSensor(sensor)
      sensorActor ! DetectIntrusion
      sensorActor ! DetectIntrusion
      controlProbe.expectMessage(SensorTriggered(sensor))
      controlProbe.expectMessage(SensorTriggered(sensor))
    }

    "include the correct sensor identity in the triggered message" in {
      val sensor = Sensor("window", Zone.Perimeter)
      val (sensorActor, controlProbe) = createSensor(sensor)
      sensorActor ! DetectIntrusion
      controlProbe.expectMessage(SensorTriggered(sensor))
    }
  }
}