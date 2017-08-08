package org.senkbeil.grus.commands

import org.senkbeil.grus.{Config, Logger}

/**
 * Represents a generic command to be executed.
 */
trait Command {
  protected lazy val logger = new Logger(this.getClass)

  /**
   * Executes the command using the specified configuration.
   *
   * @param config The configuration to use when executing the command
   */
  def execute(config: Config): Unit
}
