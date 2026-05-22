package it.unibo

import com.typesafe.config.ConfigFactory
import it.unibo.SmartHomeAlarmSystemProtocol.*
import it.unibo.actors.*
import org.apache.pekko.actor.typed.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration.*

/**
 * Test Scenario Breakdown:
 *
 * - T=0s to T=5s   : Nodes discover each other.
 * - T=5s           : [Control Unit DISARMED] Keypad sends command to arm Perimeter and SleepingArea. EXIT_DELAY starts (lasts 20s).
 * - T=10s          : [Control Unit EXIT_DELAY] "window" sensor detects something -> Ignored.
 * - T=25s          : EXIT_DELAY expires. System transitions to ARMED state.
 * - T=26s          : [Control Unit ARMED] "back-door" sensor in GroundFloor zone (inactive) detects something -> Ignored.
 * - T=28s          : [Control Unit ARMED] "front-door" sensor in Perimeter zone (active) detects something -> ENTRY_DELAY starts (lasts 10s).
 * - T=32s          : [Control Unit ENTRY_DELAY] Keypad sends wrong PIN -> Countdown continues.
 * - T=38s          : ENTRY_DELAY expires. System enters ALARM state.
 * - T=42s          : [Control Unit ALARM] Keypad sends correct PIN -> Siren silenced, system becomes DISARMED.
 * - T=50s          : [Control Unit DISARMED] Control units crashes and reboots into SAFE_RECOVERY.
 * - T=60s          : [Control Unit SAFE_RECOVERY] "front-door" sensor in Perimeter zone detects something -> Ignored.
 * - T=70s          : [Control Unit SAFE_RECOVERY] Keypad sends correct PIN -> System becomes DISARMED.
 */
object SmartHomeClusterNodeApp {
  def main(args: Array[String]): Unit = {
    val role = args(0)

    val config = ConfigFactory.load()
    val clusterName = config.getString("cluster.name")

    role match {
      case "control-unit" =>
        val alarmConfig = AlarmControlUnitActor.Config(
          pin = "1234",
          exitDelay = 20.seconds,
          entryDelay = 10.seconds
        )
        val system = ActorSystem(
          AlarmSystemGuardian.controlUnitNode(alarmConfig, simulateCrash = true),
          clusterName,
          config
        )
        Await.result(system.whenTerminated, Duration.Inf)

      case "keypad" =>
        val system = ActorSystem(
          AlarmSystemGuardian.keypadNode(),
          clusterName,
          config
        )
        // T=0s to T=5s: Wait for cluster formation
        Thread.sleep(5000)

        // T=5s: Tells the systems to begin the arm up phase. Enters EXIT_DELAY (lasts 20s)
        system ! AlarmSystemGuardian.ArmCommand("1234", Set(Zone.Perimeter, Zone.SleepingArea))

        Thread.sleep(27000)

        // T=32s: Input a wrong PIN while system is in ENTRY_DELAY state, failing to stop the alarm countdown
        system ! AlarmSystemGuardian.InputPin("9999")

        Thread.sleep(10000)

        // T=42s: Input the correct PIN while system is in ALARM state. Disables alarm, system enters DISARMED state
        system ! AlarmSystemGuardian.InputPin("1234")

        Thread.sleep(28000)

        // T=70s: Input the correct PIN while system is in SAFE_RECOVERY state. Return to DISARMED state.
        system ! AlarmSystemGuardian.InputPin("1234")

        Await.result(system.whenTerminated, Duration.Inf)

      case "sensors" =>
        val sampleSensors = List(
          Sensor("front-door", Zone.Perimeter),
          Sensor("window", Zone.SleepingArea),
          Sensor("back-door", Zone.GroundFloor)
        )
        val system = ActorSystem(
          AlarmSystemGuardian.sensorsNode(sampleSensors),
          clusterName,
          config
        )
        // T=0s to T=5s: Wait for cluster formation
        Thread.sleep(5000)

        Thread.sleep(5000)

        // T=10s: Trigger sensor during EXIT_DELAY state (Ignored)
        system ! AlarmSystemGuardian.SignalDetection("window")

        Thread.sleep(16000)

        // T=26s: Trigger an inactive zone sensor while system is ARMED (Ignored)
        system ! AlarmSystemGuardian.SignalDetection("back-door")

        Thread.sleep(2000)

        // T=28s: Trigger an active zone sensor while system is ARMED. System enters ENTRY_DELAY state (lasts 10 seconds)
        system ! AlarmSystemGuardian.SignalDetection("front-door")

        Thread.sleep(32000)

        // T=60s: Trigger a sensor 10 seconds into the SAFE_RECOVERY state (Ignored)
        system ! AlarmSystemGuardian.SignalDetection("front-door")

        Await.result(system.whenTerminated, Duration.Inf)
    }
  }
}