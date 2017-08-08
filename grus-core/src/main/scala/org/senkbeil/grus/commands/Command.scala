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

object Command {
  /**
   * Creates a collection of commands to be executed together.
   * @param commands The collection of commands to execute in order
   * @return The command representing the collection
   */
  def batch(commands: Command*): Command = new BatchCommand(commands)

  /**
   * Creates a new command from the provided block of code.
   * @param f The block of code taking the configuration as input
   * @return The command representing the block of code
   */
  def block(f: Config => Unit): Command = new BlockCommand(f)
}
