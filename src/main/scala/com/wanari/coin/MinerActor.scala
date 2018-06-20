package com.wanari.coin

import java.math.BigInteger

import akka.actor.{Actor, ActorLogging, Props}
import com.wanari.coin.MinerActor.Mine
import com.wanari.coin.SyncActor.NewBlockCreated
import spray.json.JsObject

class MinerActor extends Actor with ActorLogging{

  val start = System.currentTimeMillis()
  var lastLog = start

  override def receive: Receive = {
    case Mine(data, block, cycle) =>
      val b = block.copy(data = data, nonce = (BigInt(block.nonce, 16) + BigInteger.ONE).toString(16))
      if(DifficultyHelper.checkHash(b.hash, b.difficulty)) {
        log.info(s"new block found! $b")
        context.parent ! NewBlockCreated(b)
      } else {
        self ! Mine(data, b, cycle + 1)
      }
      logHashRate(cycle, block.index, block.difficulty)
  }

  def logHashRate(cycle: Int, index: Long, diff: Double) = {
    val now = System.currentTimeMillis()
    val elasped = now - lastLog
    if(elasped>5000) {
      val hashRate = cycle/((now - start)/1000.0)/1000.0
      log.info(s"spd: $hashRate kH/s | idx: $index | diff: $diff")
      lastLog = now
    }
  }
}

object MinerActor {

  case class Mine(data: JsObject, blockBefore: Block, cycle: Int = 0)

  def props() = Props(new MinerActor())
}
