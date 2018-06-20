package com.wanari.coin

trait BlockChain {
  def newerThen(idx: Long): Seq[Block]
  def latest: Block
  def size: Int
  def isEmpty: Boolean
  def nextEmptyBlock: Block
  def addBlock(newBlock: Block): (Boolean, BlockChain)
  def addBlocks(newBlocks: Seq[Block]): (Boolean, BlockChain)
}
