package org.senkbeil.sitegen

import java.io.{PrintStream, PrintWriter}
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import scala.util.Try

import Logger._

/**
 * Represents a simplistic logger.
 *
 * @param klass The class using the logger
 * @param level The lowest level of logging output by this logger
 * @param out The output stream to use for logging
 * @param err The error stream to use for logging
 */
class Logger(
  private val klass: Class[_],
  val level: Logger.Level.Level = Logger.defaultLevel,
  val out: PrintStream = Console.out,
  val err: PrintStream = Console.err
) {
  require(klass != null && level != null && out != null && err != null)

  /** Print writer for out channel. */
  private lazy val OutWriter = new PrintWriter(out)

  /** Print writer for err channel. */
  private lazy val ErrWriter = new PrintWriter(err)

  private lazy val NewLine =
    Option(System.getProperty("line.separator")).getOrElse("\n")

  /**
   * Represents an abbreviated fully-qualified class name.
   *
   * @example org.senkbeil.sitegen.Logger => o.s.d.Logger
   */
  lazy val AbbreviatedClassName: String = {
    val classNameParts = klass.getName.split('.')
    val abbreviationPart =
      classNameParts.filter(_.nonEmpty).map(_.charAt(0)).dropRight(1)
    val finalPart = classNameParts.last
    (abbreviationPart :+ finalPart).mkString(".")
  }

  /**
   * Starts a new logger session with the specified name.
   *
   * @param name The name to associate with the logger session
   * @return The new session instance
   */
  def newSession(name: String): Logger.Session = new Session(klass, name, level)

  /**
   * Runs the block of code, logging the time and reporting it before
   * returning the result of the code execution (including thrown errors).
   *
   * @param level The level to use when logging
   * @param prefix The string to prefix before the logged time and unit
   * @param code The block of code to execute
   * @tparam T The return type of the block of code
   * @return The result of executing the block of code
   */
  def time[T](level: Logger.Level.Level, prefix: String = "Took ")(code: => T): T = {
    val startTime = System.nanoTime()
    val result = Try(code)
    val endTime = System.nanoTime()
    val timeTaken = endTime - startTime

    val (main, fraction, unit) = timeFraction(timeTaken, TimeUnit.NANOSECONDS)
    log(level, s"$prefix${main + fraction} ${unit.toString.toLowerCase}")
    result.get
  }

  /**
   * Logs the text content if the provided level is equal to or
   * higher than the logger's level.
   *
   * E.g. Providing a level of WARN when the logger has a level of INFO
   * would log while a providing a level of VERBOSE would not log.
   *
   * @param level The level to associate with this log operation
   * @param text The text to log
   */
  def log(level: Logger.Level.Level, text: String): Unit = {
    if (this.level <= level) log(pickWriter(level), text)
  }

  /**
   * Logs the text content to standard out.
   *
   * @param text The text to log
   */
  def log(text: String): Unit = info(text)

  /**
   * Logs the text content to standard out.
   *
   * @param text The text to log
   */
  def info(text: String): Unit = log(Logger.Level.Info, text)

  /**
   * Logs the text content to standard out.
   *
   * @param text The text to log
   */
  def verbose(text: String): Unit = log(Logger.Level.Verbose, text)

  /**
   * Logs the text content to standard out.
   *
   * @param text The text to log
   */
  def trace(text: String): Unit = log(Logger.Level.Trace, text)

  /**
   * Logs the text content to standard err.
   *
   * @param text The text to log
   */
  def warn(text: String): Unit = log(Logger.Level.Warn, text)

  /**
   * Logs the text content to standard err.
   *
   * @param text The text to log
   */
  def error(text: String): Unit = log(Logger.Level.Error, text)

  /**
   * Logs the throwable to standard err.
   *
   * @param text The text to log before the throwable
   * @param throwable The throwable to log
   */
  def error(text: String, throwable: Throwable): Unit = {
    error(text + NewLine + throwableToString(throwable))
  }

  /**
   * Logs the throwable to standard err.
   *
   * @param throwable The throwable to log
   */
  def error(throwable: Throwable): Unit = {
    error(throwableToString(throwable))
  }

  /**
   * Logs the text content to standard err.
   *
   * @param text The text to log
   */
  def fatal(text: String): Unit = log(Logger.Level.Fatal, text)

  /**
   * Logs the throwable to standard err.
   *
   * @param text The text to log before the throwable
   * @param throwable The throwable to log
   */
  def fatal(text: String, throwable: Throwable): Unit = {
    fatal(text + NewLine + throwableToString(throwable))
  }

  /**
   * Logs the throwable to standard err.
   *
   * @param throwable The throwable to log
   */
  def fatal(throwable: Throwable): Unit = {
    fatal(throwableToString(throwable))
  }

  /**
   * Returns a writer to use when logging based on the given log level.
   *
   * @param level The log level to use to decide the writer
   * @return The picked writer instance
   */
  protected def pickWriter(level: Logger.Level.Level): PrintWriter = {
    if (level <= Logger.Level.Info) OutWriter
    else ErrWriter
  }

  /**
   * Logs the text content, adding a timestamp and abbreviated class name
   * in front of the content.
   *
   * @param printWriter The writer to use when logging
   * @param text The text to log
   */
  protected def log(printWriter: PrintWriter, text: String): Unit = {
    import java.text.SimpleDateFormat
    import java.util.Calendar
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
    val timestamp = format.format(Calendar.getInstance().getTime)
    printWriter.println(s"[$timestamp][$AbbreviatedClassName] $text")
    printWriter.flush()
  }

  /**
   * Converts a throwable to a more human-readable representation.
   *
   * @param throwable The throwable to convert
   * @return The string representation
   */
  private def throwableToString(throwable: Throwable): String = {
    val errorName = throwable.getClass.getName
    val errorMessage = Option(throwable.getLocalizedMessage).getOrElse("")
    val stackTrace = throwable.getStackTrace.mkString(NewLine)
    errorName + ": " + errorMessage + ": " + stackTrace
  }

  /**
   * Calculates the main unit of time and fraction remainder.
   *
   * @param duration The time duration
   * @param timeUnit The unit of time for the duration
   * @return The main length of time, the fraction remainder, and the
   *         unit representing the main length of time
   */
  private def timeFraction(
    duration: Long,
    timeUnit: TimeUnit
  ): (Long, Double, TimeUnit) = {
    val days = timeUnit.toDays(duration)
    val hours = timeUnit.toHours(duration)
    val minutes = timeUnit.toMinutes(duration)
    val seconds = timeUnit.toSeconds(duration)
    val milliseconds = timeUnit.toMillis(duration)
    val microseconds = timeUnit.toMicros(duration)
    val nanoseconds = timeUnit.toNanos(duration)

    if (days > 0) (
      days,
      (hours - TimeUnit.DAYS.toHours(days)) / 24.0,
      TimeUnit.DAYS
    ) else if (hours > 0) (
      hours,
      (minutes - TimeUnit.HOURS.toMinutes(hours)) / 60.0,
      TimeUnit.HOURS
    ) else if (minutes > 0) (
      minutes,
      (seconds - TimeUnit.MINUTES.toSeconds(minutes)) / 60.0,
      TimeUnit.MINUTES
    ) else if (seconds > 0) (
      seconds,
      (milliseconds - TimeUnit.SECONDS.toMillis(seconds)) / 1000.0,
      TimeUnit.SECONDS
    ) else if (milliseconds > 0) (
      milliseconds,
      (microseconds - TimeUnit.MILLISECONDS.toMicros(milliseconds)) / 1000.0,
      TimeUnit.MILLISECONDS
    ) else if (microseconds > 0) (
      microseconds,
      (nanoseconds - TimeUnit.MICROSECONDS.toNanos(microseconds)) / 1000.0,
      TimeUnit.MICROSECONDS
    ) else (
      nanoseconds,
      0,
      TimeUnit.NANOSECONDS
    )
  }
}

object Logger {
  /** Represents the different logging levels for the logger. */
  object Level extends Enumeration {
    type Level = Value
    val Trace, Verbose, Info, Warn, Error, Fatal, Off = Value
  }
  private var _defaultLevel: Level.Level = Level.Info

  /** Represents the default log level. */
  def defaultLevel: Level.Level = _defaultLevel

  /**
   * Overwrites the default log level.
   *
   * @param level The new log level
   */
  def setDefaultLevel(level: Level.Level): Unit = _defaultLevel = level

  /** Represents a logger that logs nothing. */
  lazy val Silent: Logger = new Logger(classOf[Logger], Level.Off)

  /**
   * Represents a logger session, used to
   *
   * @param klass The class using the logger
   * @param name The name associated with the session
   */
  class Session private[Logger](
    private val klass: Class[_],
    val name: String,
    override val level: Level.Level = defaultLevel
  ) extends Logger(klass, level) {
    private val isInitialized: AtomicBoolean = new AtomicBoolean(false)

    /**
     * Initializes the session.
     *
     * @param level The level to use when logging the init message
     * @return The initialized logger
     */
    def init(level: Level.Level = Level.Info): Session = {
      log(level, s"(( $name ))")
      isInitialized.set(true)
      this
    }

    /**
     * Logs the text content.
     *
     * @param printWriter The writer to use when logging
     * @param text        The text to log
     */
    override protected def log(
      printWriter: PrintWriter,
      text: String
    ): Unit = super.log(
      printWriter,
      (if (isInitialized.get()) "\t" else "") + text
    )
  }
}
