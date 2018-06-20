package com.wanari.coin

import com.wanari.coin.InMemoryBlockChain.DifficultyConf
import spray.json.JsObject

import scala.annotation.tailrec
import scala.concurrent.duration.FiniteDuration

case class InMemoryBlockChain(protected val blocks: List[Block])(implicit protected val difficultyConf: DifficultyConf, protected val now: () => Long) extends BlockChain {


  def newerThen(idx: Long): Seq[Block] = blocks.filter(_.index > idx)
  def latest: Block = blocks.head
  def size: Int = blocks.size
  def isEmpty: Boolean = blocks.isEmpty
  def nextEmptyBlock: Block = Block(latest.index + 1, latest.hash, JsObject(), calcNextDiff, now())

  def addBlock(newBlock: Block): (Boolean, InMemoryBlockChain) = {
    require(blocks.nonEmpty)
    if (isNewBlockValid(newBlock)) {
      (true, InMemoryBlockChain(newBlock :: blocks))
    } else {
      (false, this)
    }
  }

  def addBlocks(newBlocks: Seq[Block]): (Boolean, InMemoryBlockChain) = {
    @tailrec
    def rec(sortedBlocks: Seq[Block], bc: InMemoryBlockChain): (Boolean, InMemoryBlockChain) = {
      sortedBlocks match {
        case Nil => (true, bc)
        case head :: tail =>
          val (isValid, chain) = bc.addBlock(head)
          if (isValid) rec(tail, chain)
          else (false, InMemoryBlockChain(Nil))
      }
    }

    val sortedNewBlocks = newBlocks.sortBy(_.index).reverse
    val missingNewBlocks = sortedNewBlocks.takeWhile(nb => blocks.exists(ob => ob.hash != nb.hash)).reverse
    if (!blocks.exists(ob => ob.hash == missingNewBlocks.head.prevBlockHash)) {
      val shortened = blocks.drop(15)
      val ret = if (shortened != Nil) shortened else blocks.last :: Nil
      (false, InMemoryBlockChain(ret))
    } else {
      rec(missingNewBlocks, InMemoryBlockChain(blocks.dropWhile(b => b.hash != missingNewBlocks.head.prevBlockHash)))
    }
  }

  private def isNewBlockValid(newBlock: Block) = {
    blocks.head.index == newBlock.index - 1 &&
      blocks.head.hash == newBlock.prevBlockHash &&
      blocks.head.timeStamp <= newBlock.timeStamp &&
      calcNextDiff == newBlock.difficulty &&
      DifficultyHelper.checkHash(newBlock.hash, newBlock.difficulty)
  }

  private def calcNextDiff = {
    if (blocks.size >= difficultyConf.blockPeriod && blocks.size % difficultyConf.blockPeriod == 1) {
      val sample = blocks.take(difficultyConf.blockPeriod)
      val timeElapsed = sample.head.timeStamp - sample.last.timeStamp
      val expectedTime = difficultyConf.expectedTime * difficultyConf.blockPeriod
      DifficultyHelper.adjustDifficulty(latest.difficulty, timeElapsed, expectedTime)
    } else {
      latest.difficulty
    }
  }

}

object InMemoryBlockChain {

  case class DifficultyConf(blockPeriod: Int, expectedTime: Long)

  object DifficultyConf {
    def apply(blockPeriod: Int, expectedTime: FiniteDuration): DifficultyConf = new DifficultyConf(blockPeriod, expectedTime.toMillis)
  }

}
