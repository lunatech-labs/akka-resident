package com.lunatech.resident.server

import akka.actor.FSM
import akka.actor.Actor
import akka.actor.Actor._
import akka.actor.ActorRef
import org.jibble.pircbot._
import com.lunatech.resident.messages._
import java.util.Date
import akka.event.EventHandler

sealed trait BotAction
case class JoinChannel(channel : String, key : Option[String] = None) extends BotAction
case class AddListener(actor : ActorRef) extends BotAction

/**
 * This class does not stricly adhere to the Actor model, because PircBot spawns an
 * extra thread that calls the on* methods. We must be careful to not have shared
 * state that is touched by both the receive method and the on* methods.
 */
class ResidentActor(server : String, login : String) extends PircBot with Actor {
  override def preStart = {
    setLogin(login)
    setName(login)
    setFinger("Eeeeeeeeeeeeeeeeeeeeks")
    setVerbose(false);
    setVersion("version++")
    connect(server)
  }
  
  var listeners : List[ActorRef] = Nil  
  
  def receive = {
    case AddListener(actor) => listeners = actor +: listeners 
    case msg : JoinChannel => joinChannel(msg)
    case msg : SendMessage => sendMessage(msg.getRecipient, msg.message)
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
    handleMessage(new RawMessage(new Date(), new ChannelOrigin(channel), sender, login, hostname, message))
  }
  
  override def onPrivateMessage(sender : String, login : String, hostname : String, message : String) {
    handleMessage(new RawMessage(new Date(), new PrivateOrigin(sender), sender, login, hostname, message))
  }
  
  override def onAction(sender : String, login : String, hostname : String, target : String, action : String) {
    val origin = target.substring(1) match {
      case "#" => new ChannelOrigin(target)
      case _   => new PrivateOrigin(target)
    }
    handleMessage(new RawMessage(new Date(), origin, sender, login, hostname, "/me " + action))
  }
   
  override def log(msg : String) {}
}