package scriptedutils

object PluginCompat {
  val LogManager = sbt.LogManager
  type LogManager = sbt.LogManager

  val MainAppender = sbt.MainLogging
}
