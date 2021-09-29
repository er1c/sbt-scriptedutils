package scriptedutils

object PluginCompat {
  val LogManager = sbt.internal.LogManager
  type LogManager = sbt.internal.LogManager

  val MainAppender = sbt.internal.util.MainAppender
}
