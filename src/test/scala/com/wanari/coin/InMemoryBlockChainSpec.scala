package com.wanari.coin

import com.wanari.coin.InMemoryBlockChain.DifficultyConf
import org.scalatest.{Matchers, WordSpecLike}
import spray.json.JsObject

class InMemoryBlockChainSpec extends WordSpecLike with Matchers {

  "BlockChain" must {

    implicit val now: () => Long = System.currentTimeMillis
    implicit val diffConf = DifficultyConf(20, 300)

    "#addNewBlock" should {

      "add a block to genesis" in {
        val firstBlock = Block.mine(Block(1, Block.genesis.hash, JsObject(), 1, 1))
        InMemoryBlockChain(List(Block.genesis)).addBlock(firstBlock) shouldBe (true, InMemoryBlockChain(List(firstBlock, Block.genesis)))
      }

      "fail if bad id" in {
        val firstBlock = Block.mine(Block(2, Block.genesis.hash, JsObject(), 1, 1))
        InMemoryBlockChain(List(Block.genesis)).addBlock(firstBlock) shouldBe (false, InMemoryBlockChain(List(Block.genesis)))
      }

      "fail if bad parent hash" in {
        val firstBlock = Block.mine(Block(1, "", JsObject(), 1, 1))
        InMemoryBlockChain(List(Block.genesis)).addBlock(firstBlock) shouldBe (false, InMemoryBlockChain(List(Block.genesis)))
      }

      "fail if bad timeStamp" in {
        val firstBlock = Block.mine(Block(1, "", JsObject(), 1, -1))
        InMemoryBlockChain(List(Block.genesis)).addBlock(firstBlock) shouldBe (false, InMemoryBlockChain(List(Block.genesis)))
      }

      "fail if bad hash" in {
        val firstBlock = Block(1, "", JsObject(), 1, 1, nonce = "")
        InMemoryBlockChain(List(Block.genesis)).addBlock(firstBlock) shouldBe (false, InMemoryBlockChain(List(Block.genesis)))
      }

      "fail if bad difficulty" in {
        val firstBlock = Block.mine(Block(1, Block.genesis.hash, JsObject(), 100, 1))
        InMemoryBlockChain(List(Block.genesis)).addBlock(firstBlock) shouldBe (false, InMemoryBlockChain(List(Block.genesis)))
      }

      "difficulty adjust working fine too" in {
        implicit val diffConf = DifficultyConf(2, 2)
        val firstBlock = Block.mine(Block(1, Block.genesis.hash, JsObject(), 1, 1))
        val secondBlock = Block.mine(Block(2, firstBlock.hash, JsObject(), 1, 2))
        val thirdBlock = Block.mine(Block(3, secondBlock.hash, JsObject(), 4.0, 3))
        InMemoryBlockChain(List(secondBlock, firstBlock, Block.genesis)).addBlock(thirdBlock) shouldBe (true, InMemoryBlockChain(List(thirdBlock, secondBlock, firstBlock, Block.genesis)))
      }

    }

    "#addNewBlocks" should {

      "add a sequence of blocks" in {
        val firstBlock = Block.mine(Block(1, Block.genesis.hash, JsObject(), 1, 1))
        val secondBlock = Block.mine(Block(2, firstBlock.hash, JsObject(), 1, 2))
        InMemoryBlockChain(List(Block.genesis)).addBlocks(Seq(firstBlock, secondBlock)) shouldBe (true, InMemoryBlockChain(List(secondBlock, firstBlock, Block.genesis)))
      }

      "add a sequence of blocks from deeper parent" in {
        val firstBlock = Block.mine(Block(1, Block.genesis.hash, JsObject(), 1, 1))
        val anotherFirstBlock = Block.mine(Block(1, Block.genesis.hash, JsObject(), 1, 3))
        val secondBlock = Block.mine(Block(2, anotherFirstBlock.hash, JsObject(), 1, 5))
        InMemoryBlockChain(List(firstBlock, Block.genesis)).addBlocks(Seq(anotherFirstBlock, secondBlock)) shouldBe (true, InMemoryBlockChain(List(secondBlock, anotherFirstBlock, Block.genesis)))
      }

      "fails if no parent find (drop blocks from the ret but keep genesis)" in {
        val firstBlock = Block.mine(Block(1, Block.genesis.hash, JsObject(), 1, 1))
        val anotherFirstBlock = Block.mine(Block(1, Block.genesis.hash, JsObject(), 1, 3))
        val secondBlock = Block.mine(Block(2, anotherFirstBlock.hash, JsObject(), 1, 5))
        InMemoryBlockChain(List(firstBlock, Block.genesis)).addBlocks(Seq(secondBlock)) shouldBe (false, InMemoryBlockChain(List(Block.genesis)))
      }

      "fails if invalid" in {
        val firstBlock = Block.mine(Block(1, Block.genesis.hash, JsObject(), 1, 1))
        val anotherFirstBlock = Block.mine(Block(1, Block.genesis.hash, JsObject(), 1, 3))
        val secondBlock = Block(2, anotherFirstBlock.hash, JsObject(), 1, 2, nonce = "")
        InMemoryBlockChain(List(firstBlock, Block.genesis)).addBlocks(Seq(secondBlock, anotherFirstBlock)) shouldBe (false, InMemoryBlockChain(List()))
      }

    }


  }





}
