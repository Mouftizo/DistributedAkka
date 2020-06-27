package part2actors
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ActorCapabilities.Counter

object Exercises extends App {

  // can do it with case classes but not encapsulated
  //  case class Increment()
  //  case class Decrement()
  //  case class Print()

  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import Counter._
    val count = new AtomicInteger()

    override def receive: Receive = {
      case Increment => count.incrementAndGet()
      case Decrement => count.decrementAndGet()
      case Print     => println(count)
    }
  }

  import Counter._
  // Create actor system
  val actorSystem = ActorSystem("actorSystem")
  // Create an actor instance
  val actorInst = actorSystem.actorOf(Props[Counter], "actorInst")
  // Send messages
  actorInst ! Increment
  actorInst ! Increment
  actorInst ! Increment
  actorInst ! Decrement
  actorInst ! Print

}
