package com.wanari.coin

import org.scalatest.{Matchers, WordSpecLike}

class DifficultyHelperSpec extends WordSpecLike with Matchers {


  import DifficultyHelper._

  "#checkHash" should {

    "ok if the difficulty is close to 0" in {
      checkHash("0c7aae56ebe5d422f7f0f5b97da9856b135de81ac462c9c1a85ee53850fec479", 0.00001) shouldBe true
      checkHash("0b918943df0962bc7a1824c0555a389347b4febdc7cf9d1254406d80ce44e3f9", 0.00001) shouldBe true
    }

    "works ok if the difficulty is 4" in {
      checkHash("0000000000000000000000000000000000000000000000000000000000000000", 4) shouldBe true
      checkHash("3000000000000000000000000000000000000000000000000000000000000000", 4) shouldBe true
      checkHash("3ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe", 4) shouldBe true
      checkHash("4000000000000000000000000000000000000000000000000000000000000000", 4) shouldBe false
      checkHash("f000000000000000000000000000000000000000000000000000000000000000", 4) shouldBe false
    }

    "bigger difficulty is harder" in {
      val two = (0 to 65535).count( i =>
        checkHash(String.format("%064x", BigInt(i).bigInteger).reverse, 2)
      )
      val eight = (0 to 65535).count { i =>
        checkHash(String.format("%064x", BigInt(i).bigInteger).reverse, 8)
      }
      two should be > eight
      (two / 4) shouldBe eight
    }

  }

  "#adjustDifficulty" should {

    "stay" in {
      adjustDifficulty(100, 100, 10) shouldBe 10
    }

    "scale up" in {
      adjustDifficulty(1100, 1000, 10) shouldBe 11
    }

    "scale down" in {
      adjustDifficulty(900, 1000, 10) shouldBe 9
    }

  }
}
