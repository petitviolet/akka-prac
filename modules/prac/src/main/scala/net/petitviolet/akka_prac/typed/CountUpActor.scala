package net.petitviolet.akka_prac.typed

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior }

import scala.concurrent.duration._

object CountUpActor extends App {
  sealed trait Command
  case class Plus(num: Int) extends Command
  case object Increment extends Command
  case class Minus(num:Int) extends Command
  case class TellMe(from: ActorRef[Count]) extends Command

  case class Count(value: Int)

  private def actor(current: Int = 0): Behavior[Command] = Behaviors.receive[Command] {
    (ctx, msg) =>
      msg match {
        case Increment => actor(current + 1)
        case Plus(num) => actor(current + num)
        case Minus(num) => actor(current - num)
        case TellMe(from) =>
          from ! Count(current)
          Behaviors.same
      }
  }

  private def teller = Behaviors.receive[Count] { case (ctx, Count(value)) =>
      ctx.log.info(s"current: $value")
      Behaviors.same
  }

  val main: Behavior[NotUsed] = Behaviors.setup { ctx =>
    val sender = ctx.spawn(teller, "teller")
    Behaviors.receiveMessage { _ =>
      val count = ctx.spawn(actor(), "countUp")
      count ! Plus(10)
      count ! Increment
      count ! Minus(5)
      count ! TellMe(sender)
      Behaviors.same
    }
  }

  val system = ActorSystem(main, "CountUp")
  implicit val ec = system.executionContext
  implicit val scheduler = system.scheduler

  system ! NotUsed

  Thread.sleep(1000L)
  concurrent.Await.ready(system.terminate(), 3.seconds)
}
