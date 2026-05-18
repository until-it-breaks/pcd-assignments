package it.unibo

import it.unibo.SmartHomeAlarmSystemProtocol.*
import it.unibo.SmartHomeAlarmSystemProtocol.Zone.*
import it.unibo.actors.*
import org.apache.pekko.actor.typed.ActorSystem

import scala.concurrent.duration.DurationInt

object SmartHomeAlarmSystem:
  @main def app(): Unit =
    val config = AlarmControlUnitActor.Config(
      pin = "1234",
      exitDelay = 20.seconds,
      entryDelay = 10.seconds
    )
    val sensors = List(
      Sensor("front-door", Perimeter),
      Sensor("kitchen-window", Perimeter),
      Sensor("motion-living-room", GroundFloor)
    )

    val system: ActorSystem[AlarmSystemGuardian.Command] =
      ActorSystem(AlarmSystemGuardian(config, sensors), "SmartHomeAlarm")

    system ! AlarmSystemGuardian.SignalDetection("front-door")                    // Ignored since the system is still disarmed
    system ! AlarmSystemGuardian.ArmCommand("1234", Set(Perimeter, SleepingArea)) // Arms the alarm system
    Thread.sleep(1000)
    system ! AlarmSystemGuardian.SignalDetection("front-door")                    // Ignored since the system is in exit delay
    Thread.sleep(24000)                                                           // Wait for the system to arm itself
    system ! AlarmSystemGuardian.SignalDetection("motion-living-room")            // Should be ignored since not part of the active zones
    Thread.sleep(2000)
    system ! AlarmSystemGuardian.SignalDetection("front-door")                    // Actually triggers the entry delay state
    Thread.sleep(12000)                                                           // Waits for alarm state to start
    system ! AlarmSystemGuardian.InputPin("1233")                                 // Fails to stop the alarm
    Thread.sleep(1000)
    system ! AlarmSystemGuardian.InputPin("1234")                                 // Actually turns off the alarm
    system.terminate()