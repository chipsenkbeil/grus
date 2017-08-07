package org.senkbeil.grus

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * Main entrypoint for generating and serving site.
 */
object Main {
  /** Logger for this class. */
  private lazy val logger = new Logger(this.getClass)

  def main(args: Array[String]): Unit = {
    val configManager = new ConfigManager
    val config = configManager.loadFullConfig(args)

    // Hack to get around sys.exit(...)
    if (config.shouldExit) return

    // Set global logger used throughout program
    import Config.Implicits._ // For logLevel()
    Logger.setDefaultLevel(config.logLevel())

    // Generate before other actions if indicated
    if (config.usingGenerateCommand) {
      new Generator(config).run()

    // Serve generated content
    } else if (config.usingServeCommand) {
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

      // Run server in new thread
      val serverThread = new Server(config).runAsync()

      // Block while server is still running
      serverThread.join()

      watcherThread.foreach(t => {
        logger.verbose("Shutting down watcher thread")
        t.interrupt()
      })

    // Publish generated content
    } else if (config.usingPublishCommand) {
      new Publisher(config).run()

    // Print help info
    } else {
      config.printHelp()
    }
  }
}
