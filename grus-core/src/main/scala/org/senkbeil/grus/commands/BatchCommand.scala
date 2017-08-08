package org.senkbeil.grus.commands

import org.senkbeil.grus.Config

/**
 * Represents a collection of commands to be executed at once.
 *
 * @param commands The collection of commands to execute in order
 */
class BatchCommand private[commands](
  private val commands: Seq[Command]
) extends Command {
  /**
   * Executes the underlying commands using the specified configuration.
   *
   * @param config The configuration to use when executing the commands
   */
  override def execute(config: Config): Unit =
    commands.foreach(_.execute(config))
}

