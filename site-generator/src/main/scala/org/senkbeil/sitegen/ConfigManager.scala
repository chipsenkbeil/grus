package org.senkbeil.sitegen

import java.io.File

import scala.io.Source

import ConfigManager._

object ConfigManager {
  /** Represents extension of config file (minus the .) */
  lazy val DefaultConfigFileExt: String = "config"
}

/**
 * Represents a manager of configurations from CLI and files.
 */
class ConfigManager {
  /** Logger for this class. */
  private lazy val logger = new Logger(this.getClass)

  /**
   * Loads a configuration based on the provided arguments and a config file.
   * The config file's arguments are used first in the new configuration,
   * followed by the CLI arguments.
   *
   * @param args The raw CLI arguments
   * @param configFileName Optional explicit configuration file to load,
   *                       otherwise falls back to chosen subcommand (.config)
   *
   * @return The new configuration instance
   */
  def loadFullConfig(
    args: Array[String] = Array(),
    configFileName: String = null
  ): Config = {
    val argsConfig = loadConfigFromArgs(args)
    val subcommandName = argsConfig.builder.getSubcommandName
    val fileConfig = Option(configFileName).orElse(
      subcommandName.map(_ + s".$DefaultConfigFileExt")
    ).map(fileName => new File(fileName)).flatMap(f =>
      loadConfigFromFile(f, subcommandName.toSeq)
    )

    // Find args including and after subcommand name
    val cliArgs = if (subcommandName.nonEmpty) {
      argsConfig.args.foldLeft(Seq[String]()) { case (l, a) =>
        if (l.nonEmpty)
          l :+ a
        else if (subcommandName.get.trim.toLowerCase() == a.trim.toLowerCase)
          Seq(a)
        else
          l
      }
    } else argsConfig.args

    // Use file arguments first, then new arguments
    val fileArgs = fileConfig.map(_.args).getOrElse(Nil)
    val newArgs = subcommandName match {
      case Some(subcommand) => subcommand +:
        (fileArgs.drop(1) ++ cliArgs.drop(1))
      case None => fileArgs ++ cliArgs
    }

    new Config(newArgs)
  }

  /**
   * Loads a config from command line options provided via an array.
   *
   * @param args The command line arguments
   *
   * @return The configuration resulting from the arguments
   */
  private def loadConfigFromArgs(args: Array[String]): Config = new Config(args)

  /**
   * Loads a configuration from a file.
   *
   * @param file The file to use as the configuration
   * @param prefix Any arguments to prefix in front of loaded args
   *
   * @return Some configuration if the file exists, otherwise None
   */
  private def loadConfigFromFile(
    file: File,
    prefix: Seq[String] = Nil
  ): Option[Config] = {
    logger.trace(s"Checking if config file exists: ${file.getName}")
    if (file.isFile && file.exists()) {
      logger.info(s"Loading configuration from ${file.getName}")
      val args = Source.fromFile(file)
        .getLines.toList
        .flatMap(_.split(" ").filter(_.nonEmpty))
      Some(new Config(prefix ++ args))
    } else None
  }
}
