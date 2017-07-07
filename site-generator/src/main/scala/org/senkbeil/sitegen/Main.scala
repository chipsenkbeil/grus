package org.senkbeil.sitegen

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * Main entrypoint for generating and serving site.
 */
object Main {
  /** Logger for this class. */
  private lazy val logger = new Logger(this.getClass)

  def main(args: Array[String]): Unit = {
    val config = new Config(args)
    if (config.isQuickExit) return // Hack to get around sys.exit(...)

    // Set global logger used throughout program
    import Config.Implicits._ // For logLevel()
    Logger.setDefaultLevel(config.logLevel())

    // Generate before other actions if indicated
    if (config.usingGenerateCommand) {
      if (config.generate.isQuickExit) return
      new Generator(config).run()
    }

    // Serve generated content
    if (config.usingServeCommand) {
      if (config.serve.isQuickExit) return
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

      new Server(config).run()

      watcherThread.foreach(t => {
        logger.verbose("Shutting down watcher thread")
        t.interrupt()
      })

    // Publish generated content
    } else if (config.usingPublishCommand) {
      if (config.publish.isQuickExit) return
      new Publisher(config).run()

    // Print help info
    } else if (!config.usingGenerateCommand) {
      config.printHelp()
    }
  }
}
