package org.senkbeil.grus

import java.nio.file.Paths

import org.senkbeil.grus.Config.CommandSkeletonOptions

/**
 * Represents a producer of outlines based on a configuration.
 *
 * @param skeletonOptions The skeleton-specific options to use
 */
class Skeleton(
  private val skeletonOptions: CommandSkeletonOptions
) extends Runnable {
  /** Logger for this class. */
  private lazy val logger = new Logger(this.getClass)

  /**
   * Creates a skeleton using the stock `config.skeleton` options.
   *
   * @param config The configuration to use when producing skeleton outlines
   * @return The new skeleton instance
   */
  def this(config: Config) = this(config.skeleton)

  /**
   * Runs the skeleton producer.
   */
  def run(): Unit = {
    val projectDir = skeletonOptions.projectDir()
    val isForTheme = skeletonOptions.forTheme()
    val isForWebsite = !isForTheme

    val rootPath = Paths.get(projectDir)
    logger.verbose(s"Root path: $rootPath")

    if (isForTheme) {
      logger.error("TODO: Implement theme support")
    } else if (isForWebsite) {
      logger.error("TODO: Implement website support")
    }
  }
}
