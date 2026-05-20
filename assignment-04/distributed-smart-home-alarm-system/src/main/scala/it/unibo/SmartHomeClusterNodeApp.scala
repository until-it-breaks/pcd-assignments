package it.unibo

import com.typesafe.config.ConfigFactory
import it.unibo.SmartHomeAlarmSystemProtocol.*
import it.unibo.actors.*
import org.apache.pekko.actor.typed.ActorSystem

import scala.concurrent.duration.DurationInt
import scala.io.StdIn

object SmartHomeClusterNodeApp {
  def main(args: Array[String]): Unit = {
    val port = args(0).toInt
    val role = args(1)

    val config = ConfigFactory.parseString(s"clustering.port = $port")
      .withFallback(ConfigFactory.load())

    val clusterName = config.getString("clustering.cluster.name")

    role match {
      case "control-unit" =>
        val alarmConfig = AlarmControlUnitActor.Config("1234", 20.seconds, 10.seconds)
        ActorSystem(AlarmSystemGuardian.controlUnitNode(alarmConfig), clusterName, config)

        println(">>> Control Unit Node running. Press ENTER in this window to terminate. <<<")
        StdIn.readLine()

      case "keypad" =>
        val system = ActorSystem(AlarmSystemGuardian.keypadNode(), clusterName, config)
        Thread.sleep(5000)
        system ! AlarmSystemGuardian.InputPin("1234")

        println(">>> Keypad Node simulation completed. Keeping process alive. Press ENTER to terminate. <<<")
        StdIn.readLine()

      case "sensors" =>
        val sampleSensors = List(Sensor("front-door", Zone.Perimeter), Sensor("window", Zone.SleepingArea))
        val system = ActorSystem(AlarmSystemGuardian.sensorsNode(sampleSensors), clusterName, config)
        Thread.sleep(15000)
        system ! AlarmSystemGuardian.SignalDetection("front-door")

        println(">>> Sensors Node simulation completed. Keeping process alive. Press ENTER to terminate. <<<")
        StdIn.readLine()
    }
  }
}