package org.senkbeil.grus.commands

import org.senkbeil.grus.{Config, Skeleton}

/**
 * Represents the command to produce initial theme/website outline.
 */
object SkeletonCommand extends Command {
  /**
   * Executes skeleton command using the specified configuration.
   *
   * @param config The config tied to the server command
   */
  def execute(config: Config): Unit = {
    new Skeleton(config).run()
  }
}
