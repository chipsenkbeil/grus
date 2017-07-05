package org.senkbeil.sitegen

import java.net.URL
import java.nio.file.Paths

import org.rogach.scallop.{ScallopConf, ScallopOption, singleArgConverter}

/**
 * Represents the CLI configuration for the site generator tool.
 *
 * @param arguments The list of arguments fed into the CLI (same
 *                  arguments that are fed into the main method)
 */
class Config(arguments: Seq[String]) extends ScallopConf(arguments) {
  // ===========================================================================
  // = ACTIONS
  // ===========================================================================

  /** Represents whether or not to generate the docs. */
  val generate: ScallopOption[Boolean] = opt[Boolean](
    descr = "If true, regenerates the docs",
    default = Some(false)
  )

  /** Represents whether or not to serve the docs using a local server. */
  val serve: ScallopOption[Boolean] = opt[Boolean](
    descr = "If true, serves the generated docs",
    default = Some(false)
  )

  /** Represents whether or not to publish the built docs. */
  val publish: ScallopOption[Boolean] = opt[Boolean](
    descr = "If true, publishes the generated docs",
    default = Some(false)
  )

  // ===========================================================================
  // = LOGGING
  // ===========================================================================

  /** Aids in converting to a low level value. */
  private implicit val logLevelConverter =
    singleArgConverter[Logger.Level.Level](n =>
      Logger.Level.withName(n.trim.toLowerCase.capitalize), {
      case _: Throwable => Left(
        "Choices are " +
        Logger.Level.values.mkString(", ")
      )
    })

  /** Represents the fully-qualified class name of the default layout. */
  val defaultLogLevel: ScallopOption[Logger.Level.Level] =
    opt[Logger.Level.Level](
      descr = Seq(
        "The lowest level of logging to print:",
        Logger.Level.values.mkString(", ")
      ).mkString(" "),
      default = Some(Logger.defaultLevel)
    )

  // ===========================================================================
  // = CONFIGURABLE DEFAULTS
  // ===========================================================================

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

  // ===========================================================================
  // = SETTINGS FOR ACTIONS
  // ===========================================================================

  /** Represents the port used when serving content. */
  val port: ScallopOption[Int] = opt[Int](
    descr = "The port to use when serving files",
    default = Some(8080)
  )

  /** Represents the output directory of generated content. */
  val outputDir: ScallopOption[String] = opt[String](
    descr = "The output directory where content is generated and served",
    default = Some("out")
  )

  /** Represents the input directory of static and source content. */
  val inputDir: ScallopOption[String] = opt[String](
    descr = "The root input directory where source and static content is found",
    default = Some("docs")
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

  /** Represents whether or not to live regen docs when changed. */
  val liveReload: ScallopOption[Boolean] = opt[Boolean](
    descr = "If true, re-generates files while being served",
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

  val allowUnsupportedMediaTypes: ScallopOption[Boolean] = opt[Boolean](
    descr = Seq(
      "If true, files with unknown MIME types will be served,",
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

  /** Represents the maximum stack trace to print out when errors occur. */
  val stackTraceDepth: ScallopOption[Int] = opt[Int](
    descr = Seq(
      "The maximum depth of the stack trace to print out for errors",
      "(less than zero uses full stack)"
    ).mkString(" "),
    default = Some(-1)
  )

  // ===========================================================================
  // = SETTINGS FOR PUBLISH
  // ===========================================================================

  val publishRemoteName: ScallopOption[String] = opt[String](
    descr = "The remote name used as the destination of the publish",
    default = Some("origin")
  )

  val publishRemoteBranch: ScallopOption[String] = opt[String](
    descr = "The branch to publish the content to",
    default = Some("gh-pages")
  )

  val publishCacheDir: ScallopOption[String] = opt[String](
    descr = "The directory where a copy of the repository lives for publishing",
    default = Some(Paths.get(
      System.getProperty(
        "user.home",
        System.getProperty("java.io.tmpdir")
      ),
      ".scala-site-gen"
    ).toAbsolutePath.toString)
  )

  val publishForceCopy: ScallopOption[Boolean] = opt[Boolean](
    descr = "If provided, forces the copying of the repository to the cache",
    default = Some(false)
  )

  val publishAuthorName: ScallopOption[String] = opt[String](
    descr = "The name to use for both author and committer when publishing",
    default = None
  )

  val publishAuthorEmail: ScallopOption[String] = opt[String](
    descr = "The email to use for both author and committer when publishing",
    default = None
  )

  // ===========================================================================
  // = INITIALIZATION
  // ===========================================================================

  // Display our default values in our help menu
  appendDefaultToDescription = true

  // Process arguments
  verify()
}

