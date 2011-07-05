package com.lunatech.resident

import akka.actor.Actor
import akka.actor.Actor._

import clients._
import server._

object ResidentServer extends App {
  if(args.length < 3) {
    println("Usage : resident SERVER NICK CHANNEL+")
    exit
  }
  val residentActor = Actor.actorOf(new ResidentActor(args(0), args(1))).start
  
  args.drop(2).foreach(residentActor ! new JoinChannel(_, None))
  
  val centralHub = Actor.actorOf(new CentralHub(residentActor)).start
  remote.start("localhost", 2552)
  remote.register("irc-service", centralHub)
}