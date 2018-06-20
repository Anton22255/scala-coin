package com.wanari.coin

import scala.math.BigDecimal.RoundingMode

object DifficultyHelper {

  val tMax = BigDecimal(BigInt("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16))

  def checkHash(hash: String, difficulty: Double): Boolean = {
    //https://bitcoin.stackexchange.com/a/35807
    val t = (tMax / difficulty).toBigInt
    val h = BigInt(hash, 16)
    h < t
  }

  def adjustDifficulty(currentDiff: Double, elapsedTime: Long, expectedTime: Long): Double = {
    BigDecimal(expectedTime.toDouble / elapsedTime.toDouble * currentDiff).setScale(2, RoundingMode.DOWN).toDouble
  }
}
