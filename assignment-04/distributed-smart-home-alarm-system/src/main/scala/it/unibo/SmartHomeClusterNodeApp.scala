package it.unibo

import com.typesafe.config.ConfigFactory
import it.unibo.SmartHomeAlarmSystemProtocol.*
import it.unibo.actors.*
import org.apache.pekko.actor.typed.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration.*

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
        val system = ActorSystem(AlarmSystemGuardian.controlUnitNode(alarmConfig), clusterName, config)
        Await.result(system.whenTerminated, Duration.Inf)

      case "keypad" =>
        val system = ActorSystem(AlarmSystemGuardian.keypadNode(), clusterName, config)
        Thread.sleep(5000)
        system ! AlarmSystemGuardian.InputPin("1234")
        Thread.sleep(1000)
        system ! AlarmSystemGuardian.ArmCommand("1234", Set(Zone.Perimeter, Zone.SleepingArea))
        Thread.sleep(50000)
        system ! AlarmSystemGuardian.InputPin("1234")
        Await.result(system.whenTerminated, Duration.Inf)

      case "sensors" =>
        val sampleSensors = List(Sensor("front-door", Zone.Perimeter), Sensor("window", Zone.SleepingArea))
        val system = ActorSystem(AlarmSystemGuardian.sensorsNode(sampleSensors), clusterName, config)
        Thread.sleep(5000)
        system ! AlarmSystemGuardian.SignalDetection("window")
        Thread.sleep(35000)
        system ! AlarmSystemGuardian.SignalDetection("front-door")
        Await.result(system.whenTerminated, Duration.Inf)
    }
  }
}