package com.lunatech.resident.messages

import scala.util.matching.Regex
import java.util.Date

object MessageFilters {
  trait MessageFilter {
    def matches(msg : ReceivedMessage) : Boolean
    def build(msg : ReceivedMessage) : ReceivedMessage = msg
  }

  case class All extends MessageFilter { 
    override def matches(msg : ReceivedMessage) = true
  }
  
  case class MessageLiteral(literal : String) extends MessageFilter {
    override def matches(msg : ReceivedMessage) = literal.equals(msg.message)
  }
  
  case class MessageStartsWith(prefix : String) extends MessageFilter {
    override def matches(msg : ReceivedMessage) = msg.message startsWith prefix
  }
  
  case class MessageContains(literal : String) extends MessageFilter {
    override def matches(msg : ReceivedMessage) = msg.message contains literal  
  }
  
  case class MessageMatches(regex : Regex) extends MessageFilter {
    override def matches(msg : ReceivedMessage) = regex.pattern.matcher(msg.message).matches
  }
  
  case class CommandFilter(prefix : String) extends MessageFilter {
    override def matches(msg : ReceivedMessage) : Boolean = {
      extractCommand(msg).isDefined
    }
    override def build(msg : ReceivedMessage) = {
      val commandAndParams = extractCommand(msg).get
      Command(msg, commandAndParams._1, commandAndParams._2.getOrElse(""))
    }
    def extractCommand(msg : ReceivedMessage) : Option[(String, Option[String])] = {
      val normalizedMessage = msg.origin match {
        case _ : ChannelOrigin => msg.message.dropWhile(_ != ' ').trim
        case _ : PrivateOrigin => msg.message
      }
      
      if(normalizedMessage == prefix) {
        return Some(prefix, None);
      }
      
      if(!normalizedMessage.startsWith(prefix + " ")) {
        return None;
      }
      
      val parts = normalizedMessage.split(" ", 2).map(_.trim)
      
      return Some(parts(0), Some(parts(1)))
    }
  }
}

case class Signup(
    filter : MessageFilters.MessageFilter
)

trait ReceivedMessage {
  val sent : Date
  val origin : MessageOrigin
  val sender : String
  val hostname : String
  val message : String
  val login : String
  def reply(message : String) = Reply(origin, message)
}

case class RawMessage(sent : Date, origin : MessageOrigin, sender : String, login : String, hostname : String, message : String) extends ReceivedMessage
case class Command(sent : Date, origin : MessageOrigin, sender : String, login : String, hostname : String, message : String, command : String, parameters : String) extends ReceivedMessage

object Command {
  def apply(message : ReceivedMessage, command : String, parameters : String) : Command = {
    new Command(message.sent, message.origin, message.sender, message.login, message.hostname, message.message, command, parameters)
  }
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