package com.wanari.coin

import akka.actor.{ActorRef, FSM, PoisonPill, Props}
import com.wanari.coin.CommunicationActor.{SendToRandomNode, SendToRandomNodes}
import com.wanari.coin.SyncActor._
import com.wanari.coin.TickerActor.Tick
import spray.json.JsObject

class SyncActor(data: JsObject, startupChain: BlockChain, communicator: ActorRef) extends FSM[State, Data] {

  startWith(Syncing, SyncData(startupChain, startupChain))
  initialize()

  when(Syncing) {
    case Event(SyncDown(idx), d: SyncData) =>
      sendToRandomNode(SyncRequest(idx))
      stay using d
    case Event(NewBlockCreated(newBlock), d: SyncData) =>
      self.tell(NewBlockCreated(newBlock), sender)
      goto(Ready) using StableData(d.oldChain)
    case Event(SyncRequest(idx), d: SyncData) =>
      self.tell(SyncRequest(idx), sender)
      goto(Ready) using StableData(d.oldChain)
    case Event(SyncResponse(newBlocks), d: SyncData) if newBlocks.isEmpty =>
      goto(Ready) using StableData(d.oldChain)
    case Event(SyncResponse(newBlocks), d: SyncData) if newBlocks.nonEmpty =>
      val (ok, newChain) = d.inprogressChain.addBlocks(newBlocks)
      log.info(s"sync in progress, get ${newBlocks.size} new block, $ok, ${newChain.size}")
      if (ok) {
        if (newChain.size > d.oldChain.size) {
          goto(Ready) using StableData(newChain)
        } else {
          goto(Ready) using StableData(d.oldChain)
        }
      } else if (!newChain.isEmpty) {
        sender ! SyncRequest(newChain.latest.index)
        stay using SyncData(d.oldChain, newChain)
      } else {
        goto(Ready) using StableData(d.oldChain) //somebody just messing with us
      }
  }

  when(Ready) {
    case Event(SyncDown(idx), StableData(chain)) =>
      sendToRandomNode(SyncRequest(idx))
      goto(Syncing) using SyncData(chain, chain)
    case Event(SyncRequest(idx), StableData(chain)) =>
      sender ! SyncResponse(chain.newerThen(idx))
      stay using StableData(chain)
    case Event(NewBlockCreated(newBlock), StableData(chain)) =>
      val (isValid, newChain) = chain.addBlock(newBlock)
      if (isValid) {
        startMining(newChain)
        sendToRandomNodes(NewBlockCreated(newBlock))
        stay using StableData(newChain)
      } else {
        if (newBlock.index > chain.latest.index) {
          sender ! SyncRequest(chain.latest.index - 1)
          goto(Syncing) using SyncData(chain, chain)
        } else {
          //its just wrong...
          stay using StableData(chain)
        }
      }
  }

  whenUnhandled {
    case Event(Tick, s: Data) =>
      self ! SyncDown(s.actualChain.latest.index)
      stay
    case Event(e, s) =>
      log.warning("received unhandled request {} in state {}", e, stateName)
      stay
  }

  onTransition {
    case Syncing -> Ready =>
      nextStateData match {
        case StableData(stableChain) =>
          log.info(s"last block is: ${stableChain.latest}")
          startMining(stableChain)
        case _ => log.error(s"inconsistent state change, $nextStateData")
      }
    case Ready -> Syncing =>
      log.info(s"sync started")
  }

  private def sendToRandomNode[T](msg: T) = {
    communicator ! SendToRandomNode(msg)
  }
  private def sendToRandomNodes[T](msg: T) = {
    communicator ! SendToRandomNodes(msg)
  }

  private def startMining(chain: BlockChain) = {
    stopMining()
    val miner = context.actorOf(MinerActor.props())
    miner ! MinerActor.Mine(data, chain.nextEmptyBlock)
  }

  private def stopMining() = {
    context.children.foreach(_ ! PoisonPill)
  }
}

object SyncActor {

  sealed trait SyncMessage
  case class SyncDown(lastLocalBlockIndex: Long) extends SyncMessage
  case class SyncRequest(idx: Long) extends SyncMessage
  case class SyncResponse(blocks: Seq[Block]) extends SyncMessage
  case class NewBlockCreated(block: Block) extends SyncMessage


  sealed trait State
  case object Syncing extends State
  case object Ready extends State

  sealed trait Data{
    def actualChain: BlockChain
  }
  case class StableData(chain: BlockChain) extends Data {
    override def actualChain: BlockChain = chain
  }
  case class SyncData(oldChain: BlockChain, inprogressChain: BlockChain) extends Data {
    override def actualChain: BlockChain = inprogressChain
  }

  def props(data: JsObject, startupChain: InMemoryBlockChain, communicator: ActorRef) = Props(new SyncActor(data, startupChain, communicator))
}
