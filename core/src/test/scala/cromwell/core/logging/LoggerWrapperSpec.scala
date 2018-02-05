package cromwell.core.logging

import akka.event.Logging.LogLevel
import akka.event.{Logging, LoggingAdapter}
import cromwell.core.logging.LoggerWrapperSpec._
import org.apache.commons.lang3.exception.ExceptionUtils
import org.scalatest.prop._
import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.Logger
import org.slf4j.event.Level
import org.specs2.mock.Mockito

class LoggerWrapperSpec extends FlatSpec with Matchers with Mockito with TableDrivenPropertyChecks {

  behavior of "LoggerWrapper"

  val wrapperTests = Table[String, LoggerWrapper => Unit, List[Slf4jMessage], List[AkkaMessage]](
    (
      "description",
      "wrapperFunction",
      "slf4jMessages",
      "akkaMessages"
    ),

    (
      "log error with no args",
      _.error("Hello {} {} {} {}"),
      List(Slf4jMessage(Level.ERROR, List("tag: Hello {} {} {} {}"))),
      List(AkkaMessage(Logging.ErrorLevel, "tag: Hello {} {} {} {}"))
    ),
    (
      "log error with one arg",
      _.error("Hello {} {} {} {}", "arg1"),
      List(Slf4jMessage(Level.ERROR, List("tag: Hello {} {} {} {}", "arg1"))),
      List(AkkaMessage(Logging.ErrorLevel, "tag: Hello arg1 {} {} {}"))
    ),
    (
      "log error with two args",
      _.error("Hello {} {} {} {}", "arg1", "arg2": Any),
      List(Slf4jMessage(Level.ERROR, List("tag: Hello {} {} {} {}", "arg1", "arg2"))),
      List(AkkaMessage(Logging.ErrorLevel, s"tag: Hello arg1 arg2 {} {}"))
    ),
    (
      "log error with three args",
      _.error("Hello {} {} {} {}", "arg1", "arg2", "arg3"),
      List(Slf4jMessage(Level.ERROR, List("tag: Hello {} {} {} {}", "arg1", "arg2", "arg3"))),
      List(AkkaMessage(Logging.ErrorLevel, s"tag: Hello arg1 arg2 arg3 {}"))
    ),
    (
      "log error with one arg one exception",
      _.error("Hello {} {} {} {}", exception),
      List(Slf4jMessage(Level.ERROR, List("tag: Hello {} {} {} {}", exception))),
      List(AkkaMessage(Logging.ErrorLevel, s"tag: Hello {} {} {} {}", Option(exception)))
    ),
    (
      "log error with an exception and no args",
      _.error(exception, "Hello {} {} {} {}"),
      List(Slf4jMessage(Level.ERROR, List("tag: Hello {} {} {} {}", exception))),
      List(AkkaMessage(Logging.ErrorLevel, s"tag: Hello {} {} {} {}", Option(exception)))
    ),
    (
      "log error with an exception and one arg",
      _.error(exception, "Hello {} {} {} {}", "arg1"),
      List(Slf4jMessage(Level.ERROR, List("tag: Hello {} {} {} {}", "arg1", exception))),
      List(AkkaMessage(Logging.ErrorLevel, s"tag: Hello arg1 {} {} {}", Option(exception)))
    ),

    (
      "log warn with no args",
      _.warn("Hello {} {} {} {}"),
      List(Slf4jMessage(Level.WARN, List("tag: Hello {} {} {} {}"))),
      List(AkkaMessage(Logging.WarningLevel, "tag: Hello {} {} {} {}"))
    ),
    (
      "log warn with one arg",
      _.warn("Hello {} {} {} {}", "arg1"),
      List(Slf4jMessage(Level.WARN, List("tag: Hello {} {} {} {}", "arg1"))),
      List(AkkaMessage(Logging.WarningLevel, "tag: Hello arg1 {} {} {}"))
    ),
    (
      "log warn with two args",
      _.warn("Hello {} {} {} {}", "arg1", "arg2": Any),
      List(Slf4jMessage(Level.WARN, List("tag: Hello {} {} {} {}", "arg1", "arg2"))),
      List(AkkaMessage(Logging.WarningLevel, s"tag: Hello arg1 arg2 {} {}"))
    ),
    (
      "log warn with three args",
      _.warn("Hello {} {} {} {}", "arg1", "arg2", "arg3"),
      List(Slf4jMessage(Level.WARN, List("tag: Hello {} {} {} {}", "arg1", "arg2", "arg3"))),
      List(AkkaMessage(Logging.WarningLevel, s"tag: Hello arg1 arg2 arg3 {}"))
    ),
    (
      "log warn with one arg one exception",
      _.warn("Hello {} {} {} {}", exception),
      List(Slf4jMessage(Level.WARN, List("tag: Hello {} {} {} {}", exception))),
      List(AkkaMessage(Logging.WarningLevel, s"tag: Hello {} {} {} {}\n$exceptionMessage"))
    ),

    (
      "log info with no args",
      _.info("Hello {} {} {} {}"),
      List(Slf4jMessage(Level.INFO, List("tag: Hello {} {} {} {}"))),
      List(AkkaMessage(Logging.InfoLevel, "tag: Hello {} {} {} {}"))
    ),
    (
      "log info with one arg",
      _.info("Hello {} {} {} {}", "arg1"),
      List(Slf4jMessage(Level.INFO, List("tag: Hello {} {} {} {}", "arg1"))),
      List(AkkaMessage(Logging.InfoLevel, "tag: Hello arg1 {} {} {}"))
    ),
    (
      "log info with two args",
      _.info("Hello {} {} {} {}", "arg1", "arg2": Any),
      List(Slf4jMessage(Level.INFO, List("tag: Hello {} {} {} {}", "arg1", "arg2"))),
      List(AkkaMessage(Logging.InfoLevel, s"tag: Hello arg1 arg2 {} {}"))
    ),
    (
      "log info with three args",
      _.info("Hello {} {} {} {}", "arg1", "arg2", "arg3"),
      List(Slf4jMessage(Level.INFO, List("tag: Hello {} {} {} {}", "arg1", "arg2", "arg3"))),
      List(AkkaMessage(Logging.InfoLevel, s"tag: Hello arg1 arg2 arg3 {}"))
    ),
    (
      "log info with one arg one exception",
      _.info("Hello {} {} {} {}", exception),
      List(Slf4jMessage(Level.INFO, List("tag: Hello {} {} {} {}", exception))),
      List(AkkaMessage(Logging.InfoLevel, s"tag: Hello {} {} {} {}\n$exceptionMessage"))
    ),

    (
      "log debug with no args",
      _.debug("Hello {} {} {} {}"),
      List(Slf4jMessage(Level.DEBUG, List("tag: Hello {} {} {} {}"))),
      List(AkkaMessage(Logging.DebugLevel, "tag: Hello {} {} {} {}"))
    ),
    (
      "log debug with one arg",
      _.debug("Hello {} {} {} {}", "arg1"),
      List(Slf4jMessage(Level.DEBUG, List("tag: Hello {} {} {} {}", "arg1"))),
      List(AkkaMessage(Logging.DebugLevel, "tag: Hello arg1 {} {} {}"))
    ),
    (
      "log debug with two args",
      _.debug("Hello {} {} {} {}", "arg1", "arg2": Any),
      List(Slf4jMessage(Level.DEBUG, List("tag: Hello {} {} {} {}", "arg1", "arg2"))),
      List(AkkaMessage(Logging.DebugLevel, s"tag: Hello arg1 arg2 {} {}"))
    ),
    (
      "log debug with three args",
      _.debug("Hello {} {} {} {}", "arg1", "arg2", "arg3"),
      List(Slf4jMessage(Level.DEBUG, List("tag: Hello {} {} {} {}", "arg1", "arg2", "arg3"))),
      List(AkkaMessage(Logging.DebugLevel, s"tag: Hello arg1 arg2 arg3 {}"))
    ),
    (
      "log debug with one arg one exception",
      _.debug("Hello {} {} {} {}", exception),
      List(Slf4jMessage(Level.DEBUG, List("tag: Hello {} {} {} {}", exception))),
      List(AkkaMessage(Logging.DebugLevel, s"tag: Hello {} {} {} {}\n$exceptionMessage"))
    ),

    (
      "log trace with no args",
      _.trace("Hello {} {} {} {}"),
      List(Slf4jMessage(Level.TRACE, List("tag: Hello {} {} {} {}"))),
      Nil
    ),
    (
      "log trace with one arg",
      _.trace("Hello {} {} {} {}", "arg1"),
      List(Slf4jMessage(Level.TRACE, List("tag: Hello {} {} {} {}", "arg1"))),
      Nil
    ),
    (
      "log trace with two args",
      _.trace("Hello {} {} {} {}", "arg1", "arg2": Any),
      List(Slf4jMessage(Level.TRACE, List("tag: Hello {} {} {} {}", "arg1", "arg2"))),
      Nil
    ),
    (
      "log trace with three args",
      _.trace("Hello {} {} {} {}", "arg1", "arg2", "arg3"),
      List(Slf4jMessage(Level.TRACE, List("tag: Hello {} {} {} {}", "arg1", "arg2", "arg3"))),
      Nil
    ),
    (
      "log trace with one arg one exception",
      _.trace("Hello {} {} {} {}", exception),
      List(Slf4jMessage(Level.TRACE, List("tag: Hello {} {} {} {}", exception))),
      Nil
    )
  )

  forAll(wrapperTests) { (description, wrapperFunction, expectedSlf4jMessages, expectedAkkaMessages) =>
    it should description in {
      var actualSlf4jMessages = List.empty[Slf4jMessage]

      var actualAkkaMessages = List.empty[AkkaMessage]

      def toList(arguments: Any): List[Any] = {
        arguments match {
          case array: Array[_] => array.toList
          case seq: Seq[_] => seq.toList
          case any => List(any)
        }
      }

      def updateSlf4jMessages(level: Level, arguments: Any): Unit = {
        actualSlf4jMessages :+= Slf4jMessage(level, toList(arguments))
      }

      def updateAkkaMessages(logLevel: LogLevel, message: String, causeOption: Option[Throwable] = None): Unit = {
        actualAkkaMessages :+= AkkaMessage(logLevel, message, causeOption)
      }

      val mockLogger = mock[Logger]

      mockLogger.error(anyString).answers(updateSlf4jMessages(Level.ERROR, _))
      mockLogger.error(anyString, any[Any]).answers(updateSlf4jMessages(Level.ERROR, _))
      mockLogger.error(anyString, any[Any], any[Any]).answers(updateSlf4jMessages(Level.ERROR, _))
      mockLogger.error(anyString, anyVarArg[AnyRef]).answers(updateSlf4jMessages(Level.ERROR, _))
      mockLogger.error(anyString, any[Throwable]).answers(updateSlf4jMessages(Level.ERROR, _))

      mockLogger.warn(anyString).answers(updateSlf4jMessages(Level.WARN, _))
      mockLogger.warn(anyString, any[Any]).answers(updateSlf4jMessages(Level.WARN, _))
      mockLogger.warn(anyString, any[Any], any[Any]).answers(updateSlf4jMessages(Level.WARN, _))
      mockLogger.warn(anyString, anyVarArg[AnyRef]).answers(updateSlf4jMessages(Level.WARN, _))
      mockLogger.warn(anyString, any[Throwable]).answers(updateSlf4jMessages(Level.WARN, _))

      mockLogger.info(anyString).answers(updateSlf4jMessages(Level.INFO, _))
      mockLogger.info(anyString, any[Any]).answers(updateSlf4jMessages(Level.INFO, _))
      mockLogger.info(anyString, any[Any], any[Any]).answers(updateSlf4jMessages(Level.INFO, _))
      mockLogger.info(anyString, anyVarArg[AnyRef]).answers(updateSlf4jMessages(Level.INFO, _))
      mockLogger.info(anyString, any[Throwable]).answers(updateSlf4jMessages(Level.INFO, _))

      mockLogger.debug(anyString).answers(updateSlf4jMessages(Level.DEBUG, _))
      mockLogger.debug(anyString, any[Any]).answers(updateSlf4jMessages(Level.DEBUG, _))
      mockLogger.debug(anyString, any[Any], any[Any]).answers(updateSlf4jMessages(Level.DEBUG, _))
      mockLogger.debug(anyString, anyVarArg[AnyRef]).answers(updateSlf4jMessages(Level.DEBUG, _))
      mockLogger.debug(anyString, any[Throwable]).answers(updateSlf4jMessages(Level.DEBUG, _))

      mockLogger.trace(anyString).answers(updateSlf4jMessages(Level.TRACE, _))
      mockLogger.trace(anyString, any[Any]).answers(updateSlf4jMessages(Level.TRACE, _))
      mockLogger.trace(anyString, any[Any], any[Any]).answers(updateSlf4jMessages(Level.TRACE, _))
      mockLogger.trace(anyString, anyVarArg[AnyRef]).answers(updateSlf4jMessages(Level.TRACE, _))
      mockLogger.trace(anyString, any[Throwable]).answers(updateSlf4jMessages(Level.TRACE, _))

      val mockLoggingAdapter: LoggingAdapter = new LoggingAdapter {
        override val isErrorEnabled: Boolean = true
        override val isWarningEnabled: Boolean = true
        override val isInfoEnabled: Boolean = true
        override val isDebugEnabled: Boolean = true

        override protected def notifyError(message: String): Unit = {
          updateAkkaMessages(Logging.ErrorLevel, message)
        }

        override protected def notifyError(cause: Throwable, message: String): Unit = {
          updateAkkaMessages(Logging.ErrorLevel, message, Option(cause))
        }

        override protected def notifyWarning(message: String): Unit = {
          updateAkkaMessages(Logging.WarningLevel, message)
        }

        override protected def notifyInfo(message: String): Unit = {
          updateAkkaMessages(Logging.InfoLevel, message)
        }

        override protected def notifyDebug(message: String): Unit = {
          updateAkkaMessages(Logging.DebugLevel, message)
        }
      }

      val wrapper = new LoggerWrapper {
        override val tag: String = "tag"
        override val slf4jLoggers: Set[Logger] = Set(mockLogger)
        override val akkaLogger: Option[LoggingAdapter] = Option(mockLoggingAdapter)
      }

      wrapperFunction(wrapper)
      actualAkkaMessages should contain theSameElementsInOrderAs expectedAkkaMessages
      actualSlf4jMessages should contain theSameElementsInOrderAs expectedSlf4jMessages
    }
  }
}

object LoggerWrapperSpec {

  case class Slf4jMessage(level: Level, arguments: List[Any])

  case class AkkaMessage(logLevel: LogLevel, message: String, causeOption: Option[Throwable] = None)

  private val exception = new RuntimeException("just testing")
  private val exceptionMessage = ExceptionUtils.getStackTrace(exception)
}
