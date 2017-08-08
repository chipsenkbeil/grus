package org.senkbeil.grus.commands

import org.senkbeil.grus.{Config, Publisher}

/**
 * Represents the command to publish content.
 */
object PublishCommand extends Command {
  /**
   * Executes publish command using the specified configuration.
   *
   * @param config The config tied to the server command
   */
  def execute(config: Config): Unit = {
    new Publisher(config).run()
  }
}
