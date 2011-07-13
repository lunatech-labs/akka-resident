package com.lunatech.resident.demo.clients

import akka.actor.Actor
import akka.actor.ActorRef

import com.lunatech.resident.messages._

object Echo extends App {
  val remoteCentralHub = Actor.remote.actorFor("irc-service", "localhost", 2552)
  Actor.remote.start("localhost", 2553)
  val logger = Actor.actorOf(new Echo(remoteCentralHub)).start
}

class Echo(centralHub : ActorRef) extends Actor {
  
  override def preStart = {
    val filter = MessageFilters.CommandFilter("echo");
    centralHub ! Signup(filter)
  }
  
  def receive = {
    case command : Command => {
      command.command match {
        case "echo" => centralHub ! Reply(command.origin, command.parameters)
      }
    }
  }
}