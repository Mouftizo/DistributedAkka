package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object WordCounting extends App {

  object WordCounterMaster {
    case class Initialize(nChildren: Int) // creates nChildren to delegate tasks to
    case class WordCountTask(id: Int, text: String)
    case class WordCountReply(id: Int, count: Int)
  }
  class WordCounterMaster extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case Initialize(n) =>
        println("[master] initializing...")
        val ChildrenRefs = for (x <- 1 to n) yield context.actorOf(Props[WordCounterWorker], name = x.toString)
        context.become(withChildren(ChildrenRefs, currentChildIndex = 0, currentTaskId = 0, taskLedger = Map()))
    }

    def withChildren(childRefs: Seq[ActorRef],
                     currentChildIndex: Int,
                     currentTaskId: Int,
                     taskLedger: Map[Int, ActorRef]): Receive = {

      case text: String =>
        println(s"[master] I have received: $text - I will send it to child $currentChildIndex")
        val originalSender = sender()
        val updatedLedger = taskLedger + (currentTaskId -> originalSender)
        val childRef = childRefs(currentChildIndex)
        childRef ! WordCountTask(currentTaskId, text)
        val nextChildIndex = (currentChildIndex + 1) % childRefs.length
        val nextTaskId = currentTaskId + 1
        context.become(withChildren(childRefs, nextChildIndex, nextTaskId, updatedLedger))

      case WordCountReply(id, count) =>
        println(s"[master] I have received a reply for task id $id with $count")
        val originalSender = taskLedger(id)
        originalSender ! count
        context.become(withChildren(childRefs, currentChildIndex, currentTaskId, taskLedger - id))
    }
  }

  class WordCounterWorker extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case WordCountTask(id, text) =>
        println(s"${self.path} I have received task $id with $text")
        val wordcount = text.split(" ").length
        sender() ! WordCountReply(id, wordcount)
    }
  }

  class TestActor extends Actor {

    import WordCounterMaster._

    override def receive: Receive = {
      case "Go!" =>
        val master = context.actorOf(Props[WordCounterMaster], name = "master")
        master ! Initialize(3)
        val texts = List("I love Akka", "Scala is super dope", "yes", "me too", "LOL!!")
        texts.foreach(text => master ! text)
      case count: Int => // We get back a count
        println(s"[test actor] I received a reply: $count")

    }
  }

  val actSys = ActorSystem("WordCounter")
  val test = actSys.actorOf(Props[TestActor], name = "test")
  test ! "Go!"

}
