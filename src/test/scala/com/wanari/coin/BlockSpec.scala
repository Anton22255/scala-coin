package com.wanari.coin

import org.scalatest.{Matchers, WordSpecLike}
import spray.json._

class BlockSpec extends WordSpecLike with Matchers {

  "Block" should {
    "toString is json" in {
      Block.genesis.toString.parseJson.convertTo[Block] shouldBe Block.genesis
    }

    "have consistent hash" in {
      Block.genesis.hash shouldBe Block.genesis.hash
    }
  }


}
