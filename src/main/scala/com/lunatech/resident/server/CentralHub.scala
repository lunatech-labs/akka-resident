package com.lunatech.resident.server

import akka.actor.Actor
import akka.actor.ActorRef
import scala.util.matching.Regex

case class Signup(filter : MessageFilters.MessageFilter)

class CentralHub(val residentActor : ActorRef) extends Actor {
  residentActor ! AddListener(self)
  
  var recipients : List[(ActorRef, MessageFilters.MessageFilter)] = Nil
  
  def receive = {
    case msg : ReceivedMessage => recipients.filter(_._2.matches(msg)).removeDuplicates.foreach(_._1 ! msg) 
    case msg : SendMessage => residentActor ! msg
    case Signup(filter) => self.sender.foreach(recipient => { recipients = (recipient, filter) :: recipients})
  }
}

object MessageFilters {
  trait MessageFilter {
    def matches(msg : ReceivedMessage) : Boolean
  }

  case class All extends MessageFilter { 
    override def matches(msg : ReceivedMessage) = true
  }
  
  case class MessageLiteral(literal : String) extends MessageFilter {
    def matches(msg : ReceivedMessage) = literal.equals(msg.message)
  }
  
  case class MessageStartsWith(prefix : String) extends MessageFilter {
    def matches(msg : ReceivedMessage) = msg.message startsWith prefix
  }
  
  case class MessageContains(literal : String) extends MessageFilter {
    def matches(msg : ReceivedMessage) = msg.message contains literal  
  }
  
  case class MessageMatches(regex : Regex) extends MessageFilter {
    def matches(msg : ReceivedMessage) = regex.pattern.matcher(msg.message).matches
  }
}