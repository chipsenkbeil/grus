package org.senkbeil.grus.commands

import org.senkbeil.grus.Config

/**
 * Represents a block of code to be executed as a command.
 *
 * @param f The function to be executed by the command, takes the config
 *          as input
 */
class BlockCommand private[commands](
  private val f: Config => Unit
) extends Command {
  /**
   * Executes the block of code using the provided configuration.
   *
   * @param config The configuration to use when executing the block of code
   */
  override def execute(config: Config): Unit = f(config)
}

