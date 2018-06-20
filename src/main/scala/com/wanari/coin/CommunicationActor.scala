package com.wanari.coin

import akka.actor.{ActorPath, ActorRef, FSM, Props}
import akka.cluster.{Cluster, MemberStatus}
import com.wanari.coin.CommunicationActor._

import scala.util.Random

class CommunicationActor[E](rnd: Random, path: String, emptyMessage: E) extends FSM[State, ActorRef] {

  val cluster = Cluster(context.system)

  startWith(Uninitialized, null)
  initialize()

  when(Uninitialized) {
    case Event(SendToRandomNode(msg), _) =>
      sendToRandomNode(msg)
      goto(Initialized) using sender
    case _ =>
      //drop other cluster msges
      stay
  }


  when(Initialized) {
    case Event(SendToRandomNode(msg), _) =>
      sendToRandomNode(msg)
      stay
    case Event(SendToRandomNodes(msg), _) =>
      sendToRandomNodes(msg)
      stay
    case Event(msg, ref) =>
      ref.tell(msg, sender)
      stay
  }

  def sendToRandomNodes[T](msg: T) = {
    val members = upMembers
    if(members.nonEmpty) {
      rnd.shuffle(members).take((members.size / 5) + 1).foreach { randomMember =>
        val destinationAddr = randomMember.address + (if (path.startsWith("/")) path else "/" + path)
        context.actorSelection(ActorPath.fromString(destinationAddr)) ! msg
      }
    }
  }

  def sendToRandomNode[T](msg: T) = {
    val members = upMembers
    if(members.nonEmpty) {
      val randomMember = members.toVector(rnd.nextInt(members.size))
      val destinationAddr = randomMember.address + (if (path.startsWith("/")) path else "/" + path)
      context.actorSelection(ActorPath.fromString(destinationAddr)) ! msg
    } else {
      sender ! emptyMessage
    }
  }

  def upMembers = cluster.state.members.filter(_.status == MemberStatus.Up).filter(_.uniqueAddress != cluster.selfUniqueAddress)
}

object CommunicationActor {

  case class SendToRandomNode[T](msg: T)
  case class SendToRandomNodes[T](msg: T)

  trait State
  case object Uninitialized extends State
  case object Initialized extends State

  def props[E](rnd: Random, path: String, emptyMessage: E) = Props(new CommunicationActor[E](rnd, path, emptyMessage))
}
