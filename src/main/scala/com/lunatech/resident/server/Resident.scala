package com.lunatech.resident.server

import akka.actor.FSM
import akka.actor.Actor
import akka.actor.Actor._
import akka.actor.ActorRef
import org.jibble.pircbot._
import java.util.Date

sealed trait BotAction
case class JoinChannel(channel : String, key : Option[String] = None) extends BotAction
case class AddListener(actor : ActorRef) extends BotAction

case class ReceivedMessage(sent : Date, origin : MessageOrigin, sender : String, login : String, hostname : String, message : String) {
  def reply(message : String) = Reply(origin, message)
}

sealed trait MessageOrigin {
  def getReplyRecipient : String
}
case class PrivateOrigin(nick : String) extends MessageOrigin {
  override def getReplyRecipient = nick
}
case class ChannelOrigin(channel : String) extends MessageOrigin {
  override def getReplyRecipient = channel
}

abstract sealed class SendMessage {
  def getRecipient : String
  def message : String
}
case class SendChannelMessage(channel : String, message : String) extends SendMessage {
  def getRecipient = channel
}
case class SendPrivateMessage(nickname : String, message : String) extends SendMessage {
  def getRecipient = nickname
}
case class Reply(origin : MessageOrigin, message : String) extends SendMessage {
  def getRecipient = origin.getReplyRecipient
}

class ResidentActor(server : String, login : String) extends PircBot with Actor {
  override def preStart = {
    setLogin(login)
    setName(login)
    setFinger("Eeeeeeeeeeeeeeeeeeeeks")
    setVersion("version++")
    connect(server)
  }
  
  var listeners : List[ActorRef] = Nil  
  
  def receive = {
    case AddListener(actor) => listeners = actor +: listeners 
    case msg : JoinChannel => joinChannel(msg)
    case msg : SendMessage => {
      sendMessage(msg.getRecipient, msg.message)
    }
  }
  
  def joinChannel(msg : JoinChannel) {
    msg match {
      case JoinChannel(channel, None) => joinChannel(channel)
      case JoinChannel(channel, Some(key)) => joinChannel(channel, key)
    }
  }
  
  def handleMessage(message : ReceivedMessage) {
    listeners.foreach(_ ! message)
  }
  
  override def onMessage(channel : String, sender : String, login : String, hostname : String, message : String) {
    handleMessage(new ReceivedMessage(new Date(), new ChannelOrigin(channel), sender, login, hostname, message))
  }
  
  override def onPrivateMessage(sender : String, login : String, hostname : String, message : String) {
    handleMessage(new ReceivedMessage(new Date(), new PrivateOrigin(sender), sender, login, hostname, message))
  }
  
  override def onAction(sender : String, login : String, hostname : String, target : String, action : String) {
    val origin = target.substring(1) match {
      case "#" => new ChannelOrigin(target)
      case _   => new PrivateOrigin(target)
    }
    handleMessage(new ReceivedMessage(new Date(), origin, sender, login, hostname, "/me " + action))
  }
   
  override def log(msg : String) {}
}