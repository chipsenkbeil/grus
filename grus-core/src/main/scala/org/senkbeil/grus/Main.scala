package org.senkbeil.grus

import org.senkbeil.grus.commands.{GenerateCommand, PublishCommand, ServeCommand}

/**
 * Main entrypoint for generating and serving site.
 */
object Main {
  def main(args: Array[String]): Unit = {
    val configManager = new ConfigManager
    val config = configManager.loadFullConfig(args)

    // Hack to get around sys.exit(...)
    if (config.shouldExit) return

    // Set global logger used throughout program
    import Config.Implicits._ // For logLevel()
    Logger.setDefaultLevel(config.logLevel())

    // Generate content
    if (config.usingGenerateCommand) {
      GenerateCommand.execute(config)

    // Serve generated content
    } else if (config.usingServeCommand) {
      ServeCommand.execute(config)

    // Publish generated content
    } else if (config.usingPublishCommand) {
      PublishCommand.execute(config)

    // Print help info
    } else {
      config.printHelp()
    }
  }
}
