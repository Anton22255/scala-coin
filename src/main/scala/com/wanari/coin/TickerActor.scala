package com.wanari.coin

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Timers}
import com.wanari.coin.SyncActor.SyncDown
import com.wanari.coin.TickerActor.{PauseTick, ResumeTick, Tick}

import scala.concurrent.duration.FiniteDuration

class TickerActor(needToTick: ActorRef, interval: FiniteDuration) extends Actor with Timers with ActorLogging{
  override def receive: Receive = {
    case Tick => needToTick ! Tick
    case ResumeTick => timers.startPeriodicTimer("tick", Tick, interval)
    case PauseTick => timers.cancel("tick")
    case msg => log.warning(s"Ticker recieved a msg $msg")
  }

  override def preStart(): Unit = {
    super.preStart()
    timers.startPeriodicTimer("tick", Tick, interval)
  }
}

object TickerActor {
  case object Tick
  case object ResumeTick
  case object PauseTick

  def props(needToTick: ActorRef, interval: FiniteDuration) = Props(new TickerActor(needToTick, interval))
}
