package org.senkbeil.sitegen

import java.nio.file.{Path, StandardWatchEventKinds, WatchEvent}
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{ConcurrentLinkedQueue, TimeUnit}

import scala.collection.JavaConverters._
import scala.language.existentials
import scala.util.Try

object Watcher {
  /** Represents the default time to wait. */
  lazy val DefaultWaitTime: Long = DefaultWaitUnit.convert(3, TimeUnit.SECONDS)

  /** Represents the default unit for waiting. */
  lazy val DefaultWaitUnit: TimeUnit = TimeUnit.MILLISECONDS

  object EventType extends Enumeration {
    type EventType = Value
    val Create, Modify, Delete = Value

    /**
     * Converts the watch event to an event enumeration.
     *
     * @param watchEvent The watch event to convert
     * @return Some event type if a match for the watch event is found,
     *         otherwise None
     */
    def fromWatchEvent(watchEvent: WatchEvent[_]): Option[EventType] = {
      watchEvent.kind() match {
        case StandardWatchEventKinds.ENTRY_CREATE => Some(Create)
        case StandardWatchEventKinds.ENTRY_MODIFY => Some(Modify)
        case StandardWatchEventKinds.ENTRY_DELETE => Some(Delete)
        case _                                    => None
      }
    }
  }

  import EventType._

  case class Event(
    path: Path,
    `type`: EventType,
    watchEvent: WatchEvent[_]
  )

  object Event {
    /**
     * Converts the watch event to an event.
     *
     * @param watchEvent The watch event to convert
     * @return Some event if a match for the watch event is found,
     *         otherwise None
     */
    def fromWatchEvent(watchEvent: WatchEvent[_]): Option[Event] = {
      EventType.fromWatchEvent(watchEvent).map(eventType => {
        // NOTE: Context should always be a path for our desired events
        val path = watchEvent.context().asInstanceOf[Path]

        Event(
          path = path,
          `type` = eventType,
          watchEvent = watchEvent
        )
      })
    }
  }
}

/**
 * Watches the specified path for changes, calling the notification function
 * when a change occurs.
 *
 * @param path The path to watch
 * @param callback Called when new events are received
 * @param waitTime The duration to wait from first received watch event
 *                 until invoking the callback
 * @param waitUnit The unit of time associated with the wait time
 */
class Watcher(
  private val path: Path,
  private val callback: (Path, Seq[Watcher.Event]) => Unit,
  private val waitTime: Long = Watcher.DefaultWaitTime,
  private val waitUnit: TimeUnit = Watcher.DefaultWaitUnit
) { self =>
  /** Logger for this class. */
  private lazy val logger = new Logger(this.getClass)

  /** Starts the watcher (blocking the current thread). */
  def run(): Unit = {
    val rootPath = path.toAbsolutePath
    val watchService = rootPath.getFileSystem.newWatchService()
    val watchKey = rootPath.register(
      watchService,
      StandardWatchEventKinds.ENTRY_CREATE,
      StandardWatchEventKinds.ENTRY_MODIFY,
      StandardWatchEventKinds.ENTRY_DELETE
    )

    val startTime = new AtomicLong(-1)
    val eventQueue = new ConcurrentLinkedQueue[Seq[Watcher.Event]]()
    while (!Thread.interrupted()) {
      val events = watchKey.pollEvents().asScala.flatMap(
        Watcher.Event.fromWatchEvent
      )

      // Queue up all provided events
      if (events.nonEmpty) eventQueue.add(events)

      // If starting a new "debounce", set our start time
      if (startTime.get() < 0 && events.nonEmpty) {
        val waitTimeNanos = TimeUnit.NANOSECONDS.convert(waitTime, waitUnit)
        val waitString = nanoToString(waitTimeNanos)

        logger.trace(s"Received watch event, debouncing for $waitString...")

        startTime.set(System.nanoTime())
      }

      val timeDiff = System.nanoTime() - startTime.get()
      val timeExceeded = timeDiff >= TimeUnit.NANOSECONDS.convert(
        waitTime,
        waitUnit
      )

      // Finished "debouncing"
      if (startTime.get() > 0 && timeExceeded) {
        logger.trace(s"Debouncing finished after ${nanoToString(timeDiff)}")

        // Dump all events
        val allEvents = eventQueue.asScala.toSeq.flatten
        eventQueue.clear()

        Try(callback(rootPath, allEvents)).failed.foreach(logger.error)

        // Clear start time
        startTime.set(-1)
      }

      watchKey.reset()

      // Relieve CPU
      Thread.sleep(1)
    }
  }

  /**
   * Starts the watcher in a separate thread.
   *
   * @return The thread running the watcher
   */
  def runAsync(): Thread = {
    val thread = new Thread(new Runnable {
      override def run(): Unit = try {
        self.run()
      } catch {
        case _: InterruptedException  => /* IGNORE */
        case t: Throwable             => throw t
      }
    })

    thread.start()

    thread
  }

  /**
   * Converts nanoseconds to MM.NN milliseconds as a string.
   *
   * @param nanos The nanoseconds to convert
   * @return MMM.NNN where MMM is milliseconds part and NNN is nanoseconds
   *         remainder part
   */
  private def nanoToString(nanos: Long): String = {
    val millisDiff = TimeUnit.NANOSECONDS.toMillis(nanos)
    val nanosDiff = nanos - TimeUnit.MILLISECONDS.toNanos(millisDiff)
    s"$millisDiff.${nanosDiff}ms"
  }
}
