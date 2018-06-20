package com.wanari.coin

import org.scalatest.{Matchers, WordSpecLike}

class HashHelpersSpec extends WordSpecLike with Matchers {

  import HashHelpers._

  "#sha256Hash" should {

    //sources:
    // http://www.xorbin.com/tools/sha256-hash-calculator
    // https://www.freeformatter.com/sha256-generator.html
    // https://passwordsgenerator.net/sha256-hash-generator/
    "hash correctly 'apples'" in {
      val hash = "f5903f51e341a783e69ffc2d9b335048716f5f040a782a2764cd4e728b0f74d9"
      sha256Hash("apples") shouldBe hash
    }

    "hash correctly 'oranges'" in {
      val hash = "0c7aae56ebe5d422f7f0f5b97da9856b135de81ac462c9c1a85ee53850fec479"
      sha256Hash("oranges") shouldBe hash
    }

    "hash correctly 'bananas'" in {
      val hash = "E4BA5CBD251C98E6CD1C23F126A3B81D8D8328ABC95387229850952B3EF9F904".toLowerCase
      sha256Hash("bananas") shouldBe hash
    }

    "hash correctly '39' (leading zero)" in {
      val hash = "0b918943df0962bc7a1824c0555a389347b4febdc7cf9d1254406d80ce44e3f9"
      sha256Hash("39") shouldBe hash
    }

  }

}
