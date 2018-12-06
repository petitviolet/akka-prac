package net.petitviolet.akka_prac.typed

import akka.actor.Scheduler
import akka.actor.typed._
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContextExecutor, Future }

object SpawnChildActor {
  private implicit val timeout: Timeout = Timeout(5.seconds)

  private def childBehavior = Behaviors.receive[String] { (ctx, msg) =>
    ctx.log.info(s"child received: $msg")
    Behaviors.same
  }

  def main(args: Array[String]): Unit = {

    val system = ActorSystem(SpawnProtocol.behavior, "EchoActorMain")

    implicit val ec: ExecutionContextExecutor = system.executionContext
    implicit val scheduler: Scheduler = system.scheduler

    val childF: Future[ActorRef[String]] = system ? SpawnProtocol.Spawn(childBehavior, "child", Props.empty)

    val proc = childF.map { child =>
      child ! "Hello World!"
    }

    concurrent.Await.ready(proc flatMap { _ => system.terminate() }, 3.seconds)
  }

}
