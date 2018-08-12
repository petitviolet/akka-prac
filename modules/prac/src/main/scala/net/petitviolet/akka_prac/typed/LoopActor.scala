package net.petitviolet.akka_prac.typed
import akka.NotUsed
import akka.actor.typed._
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.Await
import scala.concurrent.duration._

object LoopActor extends App {

  def countUp(count: Int = 0): Behavior[NotUsed] = Behaviors.receive {
    (ctx, _) =>
      ctx.log.info(s"countUp: $count")
      countUp(count + 1)
  }

  case class Count(value: Int)
  val loop = Behaviors.receive[Count] {
    case (ctx, Count(current)) =>
      ctx.log.info(s"loop: $current")
      ctx.self ! Count(current + 1)
      Behaviors.same
  }

  val setUp = Behaviors.setup[NotUsed] { ctx =>
    ctx.spawn(countUp(), "countUp") ! NotUsed
    ctx.spawn(loop, "loop") ! Count(0)
    Behaviors.empty
  }

  val system = ActorSystem(setUp, "loopActorMain")
  system ! NotUsed
  Thread.sleep(100)
  Await.ready(system.terminate(), Duration.Inf)
}
