package com.lunatech.resident.demo.clients

import akka.actor.Actor
import akka.actor.ActorRef

import com.lunatech.resident.messages._

case class Start(server : ActorRef)

object Logger extends App {
  val remoteCentralHub = Actor.remote.actorFor("irc-service", "localhost", 2552)
  Actor.remote.start("localhost", 2557)
  val logger = Actor.actorOf(new Logger(remoteCentralHub)).start
}

class Logger(centralHub : ActorRef) extends Actor {
  
  override def preStart = {
    centralHub ! Signup(MessageFilters.All())
  }
  
  def receive = {
    case msg => {
      println("Logging : " + msg)
    }
  }
}