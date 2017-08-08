package org.senkbeil.grus.commands

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import org.senkbeil.grus._

/**
 * Represents the command to serve content.
 */
object ServeCommand extends Command {
  /**
   * Executes serve command using the specified configuration.
   *
   * @param config The config tied to the serve command
   */
  def execute(config: Config): Unit = {
    val rootPath = Paths.get(config.serve.inputDir())

    val watcherThread = if (config.serve.liveReload()) {
      logger.log(s"Watching $rootPath for changes")
      val watcher = new Watcher(
        path = rootPath,
        callback = (rootPath, events) => {
          logger.verbose(s"Detected ${events.length} change(s) at $rootPath")
          new Generator(config.serve).run()
        },
        waitTime = config.serve.liveReloadWaitTime(),
        waitUnit = TimeUnit.MILLISECONDS
      )

      Some(watcher.runAsync())
    } else None

    if (config.serve.generateOnStart()) {
      logger.log("Regenerating site before server starts")
      new Generator(config.serve).run()
    }

    // Run server in new thread (unfiltered will check for key press
    // for us if not run in main thread)
    val serverThread = new Server(config).runAsync()

    // Block while server is still running
    serverThread.join()

    watcherThread.foreach(t => {
      logger.verbose("Shutting down watcher thread")
      t.interrupt()
    })
  }
}
