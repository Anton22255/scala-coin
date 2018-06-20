package com.wanari.coin

import java.math.BigInteger

import spray.json._

import scala.annotation.tailrec

case class Block(index: Long, prevBlockHash: String, data: JsObject, difficulty: Double, timeStamp: Long, nonce: String = "1") {
  override def toString: String = this.toJson.toString
  def hash: String = HashHelpers.sha256Hash(this.toString)
}

object Block extends DefaultJsonProtocol {
  implicit val formatter: RootJsonFormat[Block] = jsonFormat6(Block.apply)

  val genesis = Block(0, "", JsObject(), 1, 0)

  @tailrec
  def mine(block: Block): Block = {
    val b = block.copy(nonce = (BigInt(block.nonce, 16) + BigInteger.ONE).toString(16))
    if(DifficultyHelper.checkHash(b.hash, b.difficulty)) {
      b
    } else {
      mine(b)
    }
  }
}
