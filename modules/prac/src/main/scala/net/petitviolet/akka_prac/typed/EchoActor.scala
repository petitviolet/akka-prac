package net.petitviolet.akka_prac.typed

import akka.actor.typed._
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.util._

object EchoActor {
  case class Message(value: String, from: ActorRef[Reply])
  case class Reply(value: String)

  val echo: Behavior[Message] = Behaviors.receiveMessage {
    case Message(message, from) =>
      from ! Reply(message)
      Behaviors.same
  }
}

object EchoActorMain extends App {
  private implicit val timeout = Timeout(5.seconds)
  case class Start(msg: String)
  val start = Behaviors.setup[Start] { ctx =>
    implicit val scheduler = ctx.system.scheduler
    implicit val ec = ctx.executionContext

    val echo: ActorRef[EchoActor.Message] = ctx.spawn(EchoActor.echo, "echo")

    Behaviors.receiveMessage { case Start(message) =>
      val reply: Future[EchoActor.Reply] = echo ? { ref => EchoActor.Message(message, ref) }
      reply.onComplete {
        case Success(EchoActor.Reply(value)) => ctx.log.info(s"reply success: {}.", value)
        case Failure(exception) => ctx.log.error("reply fail: {}", exception.getMessage)
      }
      Behaviors.same
    }
  }

  val system = ActorSystem(start, "EchoActorMain")
  implicit val ec = system.executionContext
  implicit val scheduler = system.scheduler

  val msg = scala.io.StdIn.readLine("Input > ")
  system ! Start(msg)

//  val replyF: Future[EchoActor.Reply] = system.systemActorOf(EchoActor.echo, "echoActor") flatMap {
//    echoActor: ActorRef[EchoActor.Message] =>
//      val msg = scala.io.StdIn.readLine("Input > ")
//      echoActor ? { self => EchoActor.Message(msg, self) }
//  }
//  replyF onComplete {
//    case Success(EchoActor.Reply(value, from)) => system.log.info(s"reply success: {}. from: {}", value, from.path)
//    case Failure(exception) => system.log.error("reply fail: {}", exception.getMessage)
//  }
//
//  concurrent.Await.ready(replyF flatMap { _ =>system.terminate() }, 3.seconds)
  Await.ready(system.terminate(), 3.seconds)
}
