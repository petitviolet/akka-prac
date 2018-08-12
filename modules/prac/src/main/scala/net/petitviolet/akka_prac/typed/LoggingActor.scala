package net.petitviolet.akka_prac.typed

import akka.actor.typed._
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object LoggingActor extends App {

  val actor: Behavior[String] = Behaviors.receive[String] {
    case (ctx, msg) =>
      ctx.log.info(s"self: ${ctx.self}, message: $msg")
      Behaviors.same
  }

  val logging: ActorSystem[String] with ActorRef[String] =
    ActorSystem(LoggingActor.actor, "logging")

  logging ! "Hello"
  logging ! "World"

  Thread.sleep(1000L)
  Await.result(logging.terminate(), Duration.Inf)
}
