package it.unibo

import it.unibo.SmartHomeAlarmSystemProtocol.Sensor
import it.unibo.SmartHomeAlarmSystemProtocol.SensorType.*
import it.unibo.actors.*
import org.apache.pekko.actor.typed.ActorSystem

import scala.concurrent.duration.DurationInt

object SmartHomeAlarmSystem:
  @main def app(): Unit =
    val config = AlarmControlUnit.Config("1234", 20.seconds, 10.seconds)
    val sensors = List(
      Sensor("front-door", Door),
      Sensor("kitchen-window", Window)
    )

    val system: ActorSystem[AlarmSystemGuardian.Command] =
      ActorSystem(AlarmSystemGuardian(config, sensors), "SmartHomeAlarm")

    system ! AlarmSystemGuardian.SignalDetection("front-door")  // Ignored since the system starts disarmed
    system ! AlarmSystemGuardian.InputPin("1234")               // Arms the alarm system

    Thread.sleep(24000)

    system ! AlarmSystemGuardian.SignalDetection("front-door")  // Triggers the entry delay status