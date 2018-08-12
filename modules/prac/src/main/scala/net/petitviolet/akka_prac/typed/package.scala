package net.petitviolet.akka_prac

import akka.actor.typed.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration.Duration

package object typed {
  def shutdown[T](system: ActorSystem[T]): Unit = {
    Thread.sleep(1000L)
    Await.result(system.terminate(), Duration.Inf)
    ()
  }

}
