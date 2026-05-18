package it.unibo.actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*

object SensorActor {
  import it.unibo.SmartHomeAlarmSystemProtocol.*
  import it.unibo.SmartHomeAlarmSystemProtocol.AlarmControlUnitInput.*

  enum Command:
    case DetectIntrusion

  export Command.*

  def apply(sensor: Sensor, controlUnit: ActorRef[AlarmControlUnitInput]): Behavior[Command] =
    active(sensor, controlUnit)

  private def active(sensor: Sensor, controlUnit: ActorRef[AlarmControlUnitInput]): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match {
        case DetectIntrusion =>
          context.log.info("Sensor [{}] detected movement/opening!", sensor.id)
          controlUnit ! SensorTriggered(sensor)
          Behaviors.same
      }
}
