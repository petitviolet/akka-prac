package net.petitviolet.akka_prac.typed

import akka.actor.Scheduler
import akka.actor.typed._
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.util._

object EchoActor {
  case class Message(value: String, from: ActorRef[Reply])
  case class Reply(value: String, from: ActorRef[Message])

  def echo(count: Int): Behavior[Message] = Behaviors.receive {
    case (ctx, Message(message, from)) =>
      from ! Reply(message, ctx.self)
      ctx.log.info(s"self: ${ctx.self}, count: $count")
//       Behaviors.same
      echo(count + 1)
  }

  case class Letter(msg: String)
  val letter = Behaviors.receive[Letter] {
    case (ctx, Letter(msg)) =>
      ctx.log.info(s"Letter: $msg")
      Behaviors.same
  }

}

object EchoActorMain {
  private implicit val timeout: Timeout = Timeout(5.seconds)
  case class Start(msg: String)

  private def start: Behavior[Start] = Behaviors.setup { ctx =>
    implicit val scheduler: Scheduler = ctx.system.scheduler
    implicit val ec: ExecutionContextExecutor = ctx.executionContext

    val echo: ActorRef[EchoActor.Message] = ctx.spawn(EchoActor.echo(0), "echo")

    Behaviors.receiveMessage[Start] { case Start(message) =>
      def send = {
        val reply: Future[EchoActor.Reply] = echo ? { ref => EchoActor.Message(message, ref) }
        reply onComplete {
          case Success(EchoActor.Reply(value, from)) => ctx.log.info(s"reply success: {}. from: {}", value, from.path)
          case Failure(exception) => ctx.log.error("reply fail: {}", exception.getMessage)
        }
      }
      send
      send
      send
      Behaviors.same
    }
  }


  // the same with `start`, however use `receive` instead of `setup`
  private def start2 = Behaviors.receive[Start] { case (ctx, Start(message)) =>
    implicit val scheduler: Scheduler = ctx.system.scheduler
    implicit val ec: ExecutionContextExecutor = ctx.executionContext

    val echo: ActorRef[EchoActor.Message] = ctx.spawn(EchoActor.echo(0), "echo")

    val reply: Future[EchoActor.Reply] = echo ? { ref => EchoActor.Message(message, ref) }
    reply onComplete {
      case Success(EchoActor.Reply(value, from)) => ctx.log.info(s"reply success: {}. from: {}", value, from.path)
      case Failure(exception) => ctx.log.error("reply fail: {}", exception.getMessage)
    }
    Behaviors.same
  }

  def main(args: Array[String]): Unit = {
    val system = ActorSystem(start, "EchoActorMain")
    // val system = ActorSystem(EchoActor.letter, "letter-actor")
    implicit val ec: ExecutionContextExecutor = system.executionContext
    implicit val scheduler: Scheduler = system.scheduler

    val msg = scala.io.StdIn.readLine("Input > ")
    system ! Start(msg)

    // system ! EchoActor.Letter(msg)

//    val echoActor: Future[ActorRef[EchoActor.Message]] = system.systemActorOf(EchoActor.echo, "echoActor")
//
//    val replyF: Future[EchoActor.Reply] = echoActor flatMap {
//      echoActor: ActorRef[EchoActor.Message] =>
//        val msg = scala.io.StdIn.readLine("Input > ")
//        echoActor ? { self => EchoActor.Message(msg, self) }
//    }
//    replyF onComplete {
//      case Success(EchoActor.Reply(value)) =>
//        system.log.info(s"reply success: {}. from: {}", value)
//      case Failure(exception) => system.log.error("reply fail: {}", exception.getMessage)
//    }

//    concurrent.Await.ready(replyF flatMap { _ =>system.terminate() }, 3.seconds)

    concurrent.Await.ready(system.terminate(), 3.seconds)
  }
}
