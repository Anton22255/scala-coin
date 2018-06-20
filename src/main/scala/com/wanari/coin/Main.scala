package com.wanari.coin

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.wanari.coin.InMemoryBlockChain.DifficultyConf
import com.wanari.coin.SyncActor.SyncResponse
import com.wanari.coin.TickerActor.Tick
import spray.json.{JsObject, JsString}

import concurrent.duration._
import scala.util.Random

object Main extends App {

  val port = if(args.length > 0) args(0).toInt else 4500
  val msg = JsObject("port" -> JsString(port.toString))

  implicit val now: () => Long = System.currentTimeMillis
  implicit val diffConf = DifficultyConf(20, 30 seconds)
  val emptyChain = InMemoryBlockChain(Block.genesis :: Nil)

  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.load())

  val actorSystem = ActorSystem("ClusterSystem", config)

  val communicationActor = actorSystem.actorOf(CommunicationActor.props(new Random(),s"user/comm",SyncResponse(Seq())), "comm")
  val syncActor = actorSystem.actorOf(SyncActor.props(msg, emptyChain, communicationActor))
  val tickerActor = actorSystem.actorOf(TickerActor.props(syncActor, 30 seconds))
  syncActor ! Tick
}
