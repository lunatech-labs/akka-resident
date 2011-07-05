package com.lunatech.resident.clients

import akka.actor.Actor
import akka.actor.Actor._

import com.lunatech.resident.server._

/*
object Echo extends App {
  val remoteCentralHub = remote.actorFor("irc-service", "localhost", 2552)
  val echo = Actor.actorOf[Echo].start
  remoteCentralHub ! new Signup(MessageFilters.MessageStartsWith("echo"))
}

class Echo extends Actor {
  def receive = {
    case msg : ReceivedMessage => {
      self.reply (msg.reply(msg.message substring 5))
    }
  }
}
*/