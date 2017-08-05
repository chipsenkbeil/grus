package org.senkbeil.sitegen

import java.io.File

import ConfigManager._
import com.moandjiezana.toml.Toml

import scala.util.{Failure, Success, Try}

object ConfigManager {
  /** Represents the generic config file for the site. */
  lazy val DefaultConfigFileName: String = "sitegen.toml"
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

    // If indicated to exit early, do so
    if (argsConfig.shouldExit) return argsConfig

    val subcommandName = argsConfig.builder.getSubcommandName

    val fileName = Option(configFileName).getOrElse(DefaultConfigFileName)
    val fileConfig = subcommandName.flatMap(
      loadConfigFromFile(new File(fileName), _: String)
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
   * @param category The category within the configuration file to load
   *
   * @return Some configuration if the file and category exist, otherwise None
   */
  private def loadConfigFromFile(
    file: File,
    category: String
  ): Option[Config] = {
    logger.trace(s"Checking if config file exists: ${file.getName}")
    val f = if (file.isFile && file.exists()) Some(file) else None

    logger.info(s"Loading configuration from ${file.getName}")
    val toml = f.flatMap(f => {
      val t = Try((new Toml).read(f))
      t.failed.foreach(logger.error("Config load error", _: Throwable))
      t.toOption
    })

    val tomlSection = toml.flatMap(t => Option(t.getTable(category)))
    if (tomlSection.isEmpty) logger.error(s"Missing '$category' in config!")

    // NOTE: Currently, we just convert all keys to command line arguments.
    //       Is there a better way?
    tomlSection.map(t => {
      import scala.collection.JavaConverters._
      val args = category +: t.toMap.asScala.map { case (key, value) =>
        // NOTE: We are assuming CLI arguments are only in blah-blah form,
        //       so converting any underscores to hyphens to work with CLI
        val kstr = key.replace('_', '-')
        val vstr = value match {
          case a: Array[_] => a.mkString(",")
          case v => v.toString
        }

        Try(vstr.toBoolean) match {
          case Success(v) if v  => s"--$kstr"
          case Success(v)       => ""
          case Failure(_)       => s"--$kstr=$vstr"
        }
      }.toSeq.filter(_.nonEmpty)

      new Config(args)
    })
  }
}
