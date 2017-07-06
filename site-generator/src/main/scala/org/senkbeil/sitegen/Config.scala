package org.senkbeil.sitegen

import java.net.URL
import java.nio.file.Paths

import org.rogach.scallop._

object Config {
  private val DefaultLogLevel: Logger.Level.Level = Logger.defaultLevel
  private val DefaultStackTraceDepth: Int = -1

  /** Aids in converting to a low level value. */
  private implicit val LogLevelConverter: ValueConverter[Logger.Level.Level] =
    singleArgConverter[Logger.Level.Level](n =>
      Logger.Level.withName(n.trim.toLowerCase.capitalize), {
      case _: Throwable => Left(
        "Choices are " +
          Logger.Level.values.mkString(", ")
      )
    })

  /** Represents common options across commands. */
  trait CommandCommonOptions extends ScallopConfBase {
    /** Represents the fully-qualified class name of the default layout. */
    val logLevel: ScallopOption[Logger.Level.Level] =
      opt[Logger.Level.Level](
        descr = Seq(
          "The lowest level of logging to print:",
          Logger.Level.values.mkString(", ")
        ).mkString(" "),
        default = Some(DefaultLogLevel)
      )

    /** Represents the maximum stack trace to print out when errors occur. */
    val stackTraceDepth: ScallopOption[Int] = opt[Int](
      descr = Seq(
        "The maximum depth of the stack trace to print out for errors",
        "(less than zero uses full stack)"
      ).mkString(" "),
      default = Some(DefaultStackTraceDepth)
    )

    appendDefaultToDescription = true
    shortSubcommandsHelp(true)
  }

  /** Represents an implicit wrapper of a Config instance. */
  implicit class ConfigWrapper(config: Config) {
    /**
      * Represents the consolidated log level of the configuration.
      *
      * @return The log level associated with the active subcommand
      */
    def logLevel(): Logger.Level.Level = {
      if (config.usingGenerateCommand) config.generate.logLevel()
      else if (config.usingServeCommand) config.serve.logLevel()
      else if (config.usingPublishCommand) config.publish.logLevel()
      else DefaultLogLevel
    }

    /**
      * Represents the consolidated stack trace depth of the configuration.
      *
      * @return The depth of the stack trace associated with the active subcommand
      */
    def stackTraceDepth(): Int = {
      if (config.usingGenerateCommand) config.generate.stackTraceDepth()
      else if (config.usingServeCommand) config.serve.stackTraceDepth()
      else if (config.usingPublishCommand) config.publish.stackTraceDepth()
      else DefaultStackTraceDepth
    }
  }
}

/**
 * Represents the CLI configuration for the site generator tool.
 *
 * @param arguments The list of arguments fed into the CLI (same
 *                  arguments that are fed into the main method)
 */
class Config(arguments: Seq[String]) extends ScallopConf(arguments) {
  // ===========================================================================
  // = COMMON OPTIONS AND SETTINGS
  // ===========================================================================
  import Config._

  // ===========================================================================
  // = GENERATE SETTINGS
  // ===========================================================================

  /** Represents the command to regenerate the site. */
  val generate = new Subcommand("generate") with CommandCommonOptions {
    descr("Generates the site")

    /** Represents the fully-qualified class name of the default layout. */
    val defaultPageLayout: ScallopOption[String] = opt[String](
      descr = "The class representing the default layout if one is not specified",
      default = Some(classOf[org.senkbeil.sitegen.layouts.DefaultPage].getName)
    )

    /** Represents the weight for a page if not specified. */
    val defaultPageWeight: ScallopOption[Double] = opt[Double](
      descr = "The weight for a page if one is not specified",
      default = Some(0)
    )

    /** Represents whether or not to render a page if not specified. */
    val defaultPageRender: ScallopOption[Boolean] = opt[Boolean](
      descr = Seq(
        "If false, the page will not be rendered if not specified in metadata,",
        "but the page will still appear in menus"
      ).mkString(" "),
      default = Some(true)
    )

    /** Represents whether or not a page is fake if not specified. */
    val defaultPageFake: ScallopOption[Boolean] = opt[Boolean](
      descr = "If true, the page will be fake if not specified in metadata",
      default = Some(false)
    )

    /** Represents a flag to NOT generate a .nojekyll file. */
    val doNotGenerateNoJekyllFile: ScallopOption[Boolean] = opt[Boolean](
      descr = "If provided, will not generate a .nojekyll file in the output",
      default = Some(false)
    )

    /** Represents a flag to NOT generate a sitemap.xml file. */
    val doNotGenerateSitemapFile: ScallopOption[Boolean] = opt[Boolean](
      descr = "If provided, will not generate a sitemap.xml file in the output",
      default = Some(false)
    )

    /** Represents the site's hostname. */
    val siteHost: ScallopOption[URL] = opt[URL](
      descr = Seq(
        "Represents the host used when generating content such as",
        "http://www.example.com"
      ).mkString(" "),
      default = Some(new URL("http://localhost/"))
    )

    /** Represents the output directory of generated content. */
    val outputDir: ScallopOption[String] = opt[String](
      descr = "The output directory where content is generated and served",
      default = Some("out")
    )

    /** Represents the input directory of static and source content. */
    val inputDir: ScallopOption[String] = opt[String](
      descr = "The root input directory where source and static content is found",
      default = Some("site")
    )

    /** Represents the directory of source content. */
    val srcDir: ScallopOption[String] = opt[String](
      descr = "The source directory (relative to input directory) where source content is found",
      default = Some("src")
    )

    /** Represents the directory of static content. */
    val staticDir: ScallopOption[String] = opt[String](
      descr = "The static directory (relative to input directory) where static content is found",
      default = Some("static")
    )
  }
  addSubcommand(generate)

  def usingGenerateCommand: Boolean = subcommand.exists(_ == generate)

  // ===========================================================================
  // = SERVE SETTINGS
  // ===========================================================================

  /** Represents the command to serve the site using a local server. */
  val serve = new Subcommand("serve") with CommandCommonOptions {
    descr("Serves the site using a local server")

    /** Represents the port used when serving content. */
    val port: ScallopOption[Int] = opt[Int](
      descr = "The port to use when serving files",
      default = Some(8080)
    )

    /** Represents whether or not to live regen site when changed. */
    val liveReload: ScallopOption[Boolean] = opt[Boolean](
      descr = "If specified, re-generates files while being served",
      default = Some(true)
    )

    /** Represents the time in milliseconds to wait after first change. */
    val liveReloadWaitTime: ScallopOption[Long] = opt[Long](
      descr = Seq(
        "The number of milliseconds to wait after detecting a change",
        "before performing a live reload"
      ).mkString(" "),
      default = Some(500)
    )

    /** Represents whether or not to allow unknown MIME types. */
    val allowUnsupportedMediaTypes: ScallopOption[Boolean] = opt[Boolean](
      descr = Seq(
        "If specified, files with unknown MIME types will be served,",
        "but without a Content-Type header"
      ).mkString(" "),
      default = Some(false)
    )

    /**
      * Represents files that serve as defaults when accessing a directory.
      *
      * E.g. '/my/path/' becomes '/my/path/index.html'
      */
    val indexFiles: ScallopOption[List[String]] = opt[List[String]](
      descr = "Files that serve as defaults when accessing a directory",
      default = Some(List("index.html", "index.htm"))
    )
  }
  addSubcommand(serve)

  def usingServeCommand: Boolean = subcommand.exists(_ == serve)

  // ===========================================================================
  // = PUBLISH SETTINGS
  // ===========================================================================

  /** Represents the command to publish the built site. */
  val publish = new Subcommand("publish") with CommandCommonOptions {
    descr("Publishes the site to Github Pages")

    val remoteName: ScallopOption[String] = opt[String](
      descr = "The remote name used as the destination of the publish",
      default = Some("origin")
    )

    val remoteBranch: ScallopOption[String] = opt[String](
      descr = "The branch to publish the content to",
      default = Some("gh-pages")
    )

    val cacheDir: ScallopOption[String] = opt[String](
      descr = "The directory where a copy of the repository lives for publishing",
      default = Some(Paths.get(
        System.getProperty(
          "user.home",
          System.getProperty("java.io.tmpdir")
        ),
        ".scala-site-gen"
      ).toAbsolutePath.toString)
    )

    val forceCopy: ScallopOption[Boolean] = opt[Boolean](
      descr = "If provided, forces the copying of the repository to the cache",
      default = Some(false)
    )

    val authorName: ScallopOption[String] = opt[String](
      descr = "The name to use for both author and committer when publishing",
      default = None
    )

    val authorEmail: ScallopOption[String] = opt[String](
      descr = "The email to use for both author and committer when publishing",
      default = None
    )
  }
  addSubcommand(publish)

  def usingPublishCommand: Boolean = subcommand.exists(_ == publish)

  // ===========================================================================
  // = INITIALIZATION
  // ===========================================================================

  // Display our default values in our help menu
  appendDefaultToDescription = true

  // Enable displaying subcommands in help
  shortSubcommandsHelp(true)

  // Mark version of tool
  version(BuildInfo.version)

  // Process arguments
  verify()
}

