package net.petitviolet.akka_prac.typed

import akka.actor.typed._
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util._

class EchoActor {

}

object EchoActor {
  case class Message(value: String, from: ActorRef[Reply])
  case class Reply(value: String, from: ActorRef[Message])

  val echo: Behavior[Message] = Behaviors.receive {
    case (ctx, Message(message, from)) =>
      from ! Reply(message, ctx.self)
      Behaviors.same
  }
}

object EchoActorMain extends App {
  case class Start(msg: String)
  val start = Behaviors.setup[Start] { ctx =>
    val echo: ActorRef[EchoActor.Message] = ctx.spawn(EchoActor.echo, "echo")

    Behaviors.receiveMessage { case Start(message) =>
      implicit val timeout = Timeout(5.seconds)
      implicit val scheduler = ctx.system.scheduler
      implicit val ec = ctx.executionContext
      val reply: Future[EchoActor.Reply] = echo ? { ref => EchoActor.Message(message, ref) }
      reply.onComplete {
        case Success(EchoActor.Reply(value, from)) => ctx.log.info(s"reply success: {}. from: {}", value, from.path)
        case Failure(exception) => ctx.log.error("reply fail: {}", exception.getMessage)
      }
      Behaviors.same
    }
  }

  val system = ActorSystem(start, "EchoActorMain")

  val msg = scala.io.StdIn.readLine("Input > ")
  system ! Start(msg)

  concurrent.Await.ready(system.terminate(), 3.seconds)
}