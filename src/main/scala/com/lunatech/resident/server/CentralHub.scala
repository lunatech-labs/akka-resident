package com.lunatech.resident.server

import akka.actor.Actor
import akka.actor.ActorRef
import akka.remoteinterface._
import com.lunatech.resident.messages._
import com.lunatech.resident.server._
import java.net.InetSocketAddress
import akka.event.EventHandler

case class Disconnected(address : InetSocketAddress)

class CentralHub(val residentActor : ActorRef) extends Actor {
  
  residentActor ! AddListener(self)
  
  var recipients : List[(ActorRef, InetSocketAddress, MessageFilters.MessageFilter)] = Nil
  
  def receive = {
    case msg : ReceivedMessage => {
      recipients.filter(_._3.matches(msg)).removeDuplicates.foreach(rec => {
        EventHandler.debug(this, "Sending message to actor " + rec._1)
        rec._1 ! rec._3.build(msg)
      }) 
    }
    case msg : SendMessage => residentActor ! msg
    case Signup(filter) => {
      EventHandler.debug(this, "Received messagefilter " + filter)
      self.sender.foreach(recipient => { recipients = (recipient, self.sender.get.getHomeAddress, filter) :: recipients})
    }
    case Disconnected(address : InetSocketAddress) => {
      recipients = recipients.filterNot(arg => arg._2 == address)
    }
    case otherMsg => EventHandler.debug(this, "Received other message type : " + otherMsg)
  }
}

