package scriptedutils

import sbt._
import sbt.Keys.{logManager, state}
import sbt.internal.LogManager
import sbt.internal.util.MainAppender

import java.io.PrintWriter

object ScriptedUtilsPlugin extends AutoPlugin {
  //import scriptedutils.PluginCompat._
  override def trigger = allRequirements

  object autoImport {
    val checkLogContains = inputKey[Boolean]("Check the log contains a message.")
    val checkLogNotContains = inputKey[Boolean]("Check the log does not contain a message.")
    val clearLog = taskKey[Unit]("Clear the current log.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    checkLogContains := checkLogContainsTask.evaluated,
    checkLogNotContains := checkLogNotContainsTask.evaluated,
    clearLog := clearLogTask.value,
    logManager := {
      // Adapted reflection code from https://github.com/avdv/sbt-hyperlink/blob/b84c5a347e025b91eb43efb6691ceac8fce1497e/src/main/scala/sbthyperlink/HyperlinkPlugin.scala

      //  MIT License
      //
      //  Copyright (c) 2018 Claudio Bley
      //
      //  Permission is hereby granted, free of charge, to any person obtaining a copy
      //  of this software and associated documentation files (the "Software"), to deal
      //  in the Software without restriction, including without limitation the rights
      //  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
      //  copies of the Software, and to permit persons to whom the Software is
      //  furnished to do so, subject to the following conditions:
      //
      //    The above copyright notice and this permission notice shall be included in all
      //    copies or substantial portions of the Software.
      //
      //  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
      //  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
      //  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
      //  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
      //  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
      //  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
      //  SOFTWARE.

      // we use internal sbt APIs, which are incompatible between < 1.4 and >= 1.4
      // see https://github.com/sbt/sbt/issues/5931 and https://github.com/sbt/sbt/pull/5731
      // work-around using reflection
      import scala.reflect.runtime.{ universe => ru }

      val mirror = ru.runtimeMirror(getClass.getClassLoader)

      val mainAppenderType = mirror.typeOf[MainAppender.type]
      val mainAppenderModuleSymbol = mainAppenderType.termSymbol.asModule
      val mainAppender = mirror.reflect(mirror.reflectModule(mainAppenderModuleSymbol).instance)
      val defaultScreenMethodSymbol =
        mainAppenderType.decl(ru.TermName("defaultScreen")).asTerm.alternatives.collectFirst {
          case m if m.asMethod.paramLists.foldLeft(0)(_ + _.size) == 1 => m.asMethod
        }
      val defaultScreen = mainAppender.reflectMethod(defaultScreenMethodSymbol.get)

      val logManagerType = mirror.typeOf[LogManager.type]
      val logManagerModuleSymbol = logManagerType.termSymbol.asModule
      val logManager = mirror.reflect(mirror.reflectModule(logManagerModuleSymbol).instance)

      val withScreenLoggerMethodSymbol = logManagerType.decl(ru.TermName("withScreenLogger")).asMethod
      val withScreenLogger = logManager.reflectMethod(withScreenLoggerMethodSymbol)

      // TODO: Something special about scripted that the logging contents isn't being sent to its' logger
      withScreenLogger { (_: ScopedKey[_], state: State) =>
        defaultScreen(ConsoleOut.printWriterOut(new PrintWriter(System.out) {
          override def print(s: String): Unit = {
            BufferLogger.print(s)
            super.print(s)
          }

          override def println(s: String): Unit = {
            BufferLogger.println(s)
            super.println(s)
          }

          override def println(): Unit = {
            BufferLogger.println()
            super.println()
          }
        }))
      }.asInstanceOf[LogManager]
    }
  )

  override lazy val buildSettings = Seq()

  override lazy val globalSettings = Seq()

  private def checkLogContainsTask: Def.Initialize[InputTask[Boolean]] = {
    import sbt.complete.DefaultParsers._

    /**
     * Adapted from https://github.com/playframework/playframework/blob/53e55f017f508f0dcdf2a70327bd73153fe87e5e/dev-mode/sbt-plugin/src/sbt-test/play-sbt-plugin/play-position-mapper/build.sbt#L20
     * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
     * License: Apache License 2.0 - https://github.com/playframework/playframework/blob/53e55f017f508f0dcdf2a70327bd73153fe87e5e/LICENSE
     */
    InputTask.separate[String, Boolean]((_: State) => Space ~> any.+.map(_.mkString(""))) {
      state(_ => (msg: String) => task {
        if (BufferLogger.messages.forall(!_.contains(msg))) {
          sys.error(
            s"""Did not find log message:
               |    '$msg'
               |in output:
               |    ${BufferLogger.messages.reverse.mkString("\n    ")}""".stripMargin
          )
        }
        true
      })
    }
  }

  private def checkLogNotContainsTask: Def.Initialize[InputTask[Boolean]] = {
    import sbt.complete.DefaultParsers._

    /**
     * Adapted from https://github.com/playframework/playframework/blob/53e55f017f508f0dcdf2a70327bd73153fe87e5e/dev-mode/sbt-plugin/src/sbt-test/play-sbt-plugin/play-position-mapper/build.sbt#L20
     * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
     * License: Apache License 2.0 - https://github.com/playframework/playframework/blob/53e55f017f508f0dcdf2a70327bd73153fe87e5e/LICENSE
     */
    InputTask.separate[String, Boolean]((_: State) => Space ~> any.+.map(_.mkString(""))) {
      state(_ => (msg: String) => task {
        val foundMessage = BufferLogger.messages.find(_.contains(msg))

        if (foundMessage.isDefined) {
          sys.error(
            s"""Found log message:
               |    '$msg'
               |in output line:
               |${foundMessage.get}""".stripMargin
          )
        }
        true
      })
    }
  }

  private def clearLogTask: Def.Initialize[Task[Unit]] = Def.task {
    BufferLogger.clear()
  }
}
