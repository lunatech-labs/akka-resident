package com.lunatech.resident.demo.clients;

import com.lunatech.resident.messages.SendChannelMessage;

import akka.actor.ActorRef;
import static akka.actor.Actors.*;

public class JavaClient {
    public static void main(String[] args) {
        ActorRef actor = remote().actorFor("irc-service", "localhost", 2552);
        actor.sendOneWay(new SendChannelMessage("#test", "Hi there, I am a Java client!"));
     }
}
