package scriptedutils

///*
// * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
// */
//
//import org.apache.logging.log4j.Level
//import org.apache.logging.log4j.core.{ LogEvent => Log4JLogEvent, _ }
//import org.apache.logging.log4j.core.Filter.Result
//import org.apache.logging.log4j.core.appender.AbstractAppender
//import org.apache.logging.log4j.core.filter.LevelRangeFilter
//import org.apache.logging.log4j.core.layout.PatternLayout
//
//object BufferLogger extends AbstractAppender(
//  "FakeAppender",
//  LevelRangeFilter.createFilter(Level.TRACE, Level.ERROR, Result.NEUTRAL, Result.DENY),
//  PatternLayout.createDefaultLayout()
//) {
//  @volatile var messages = List.empty[String]
//
//  def append(event: Log4JLogEvent): Unit = updateMessages(event.getMessage.getFormattedMessage  :: messages)
//
//  def clear(): Unit = updateMessages(List.empty[String])
//
//  private def updateMessages(messages: List[String]): Unit = synchronized {
//    this.messages = messages
//  }
//
//  start()
//}

object BufferLogger {
  @volatile var messages = List.empty[String]
  private[this] val current = new java.lang.StringBuffer

  def print(s: String): Unit = synchronized { current.append(s); () }
  def println(s: String): Unit = synchronized { current.append(s); println() }
  def println(): Unit = synchronized {
    val msg = current.toString
    updateMessages(msg :: messages)
    current.setLength(0)
  }

  def clear(): Unit = {
    updateMessages(List.empty[String])
  }

  private def updateMessages(messages: List[String]): Unit = synchronized {
    this.messages = messages
  }
}