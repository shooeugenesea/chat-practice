import ChatRoom.{CachedChatMessage, ChatMessage, GetChatMessages}
import akka.actor.{Actor, ActorRef, Props}

import scala.collection.mutable

object ChatRoom {
  case class ChatMessage(seqId:Long, message:String)
  case class CachedChatMessage(seqId:Long, message:String)
  case class GetChatMessages(lastMsgId:Long)

  def props():Props = Props[ChatRoom]
}

class ChatRoom extends Actor {

  var seqId = 0;
  var cachedChatMessages = new mutable.Queue[CachedChatMessage]

  override def receive: Receive = {
    case GetChatMessages(lastMsgId:Long) =>
      var result = ""
      cachedChatMessages.filter(_.seqId > lastMsgId).foreach(result += _.message + "\n")
      result = seqId + ":" + result
      sender() ! result
    case chatMessage @ ChatMessage(userOwnLastSeqId:Long, message:String) =>
      seqId += 1
      cachedChatMessages = cachedChatMessages :+ new CachedChatMessage(seqId, message)
      dequeueMessageFromQueueToFixedSize
  }

  private def dequeueMessageFromQueueToFixedSize(): Unit = {
    while (cachedChatMessages.size > 5) {
      println(self.path + ": drop old message:" + cachedChatMessages.dequeue)
    }
  }

}


