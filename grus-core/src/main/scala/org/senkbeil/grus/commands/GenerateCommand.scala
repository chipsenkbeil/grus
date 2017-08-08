package org.senkbeil.grus.commands

import org.senkbeil.grus.{Config, Generator}

/**
 * Represents the command to generate content.
 */
object GenerateCommand extends Command {
  /**
   * Executes generate command using the specified configuration.
   *
   * @param config The config tied to the server command
   */
  def execute(config: Config): Unit = {
    new Generator(config).run()
  }
}
