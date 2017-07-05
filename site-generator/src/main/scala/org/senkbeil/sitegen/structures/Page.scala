package org.senkbeil.sitegen.structures

import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths, StandardOpenOption}

import com.vladsch.flexmark.ast.Node
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension
import com.vladsch.flexmark.ext.front.matter.{AbstractYamlFrontMatterVisitor, YamlFrontMatterExtension}
import com.vladsch.flexmark.ext.gfm.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.options.MutableDataSet
import org.senkbeil.sitegen.{Config, Logger}
import org.senkbeil.sitegen.layouts.{Context, Layout}
import org.senkbeil.sitegen.utils.FileHelper

import scala.util.Try

/**
 * Represents a page of content.
 *
 * @param config Used to fill in defaults
 * @param path The path to the raw page content
 * @param logger The logger to use with the page
 */
class Page private (
  val config: Config,
  val path: Path,
  private val logger: Logger
) {
  /**
   * Compares this page with another page. Two pages are equal if they have
   * the same path.
   *
   * @param obj The other page to compare
   * @return True if the two pages have the same path, otherwise false
   */
  override def equals(obj: Any): Boolean = obj match {
    case p: Page  => p.path == this.path
    case _        => false
  }

  /** Represents an internal flexmark node used for markdown processing. */
  private lazy val pageNode = {
    assert(!isDirectory, "Page represents a directory, not a markdown file!")
    parseMarkdownFile(path)
  }

  /** Represents the metadata for the page. */
  lazy val metadata: Metadata = parseMetadata(pageNode)

  /** Represents whether or not this page represents a directory. */
  lazy val isDirectory: Boolean = Files.isDirectory(path)

  /**
   * Represents an absolute web path link to this file,
   * ignoring any overrides.
   */
  lazy val absoluteLink: String = {
    val srcDirPath = {
      val inputDir = config.inputDir()
      val srcDir = config.srcDir()
      Paths.get(inputDir, srcDir)
    }

    val relativePath =
      if (isIndexPage) srcDirPath.relativize(path.getParent)
      else srcDirPath.relativize(path)

    val rootPathString = "/" +
      FileHelper.stripExtension(relativePath.toString)
        .replaceAllLiterally(java.io.File.separator, "/")

    if (rootPathString.endsWith("/")) rootPathString
    else rootPathString + "/"
  }

  /**
   * Represents the title of the page, which either comes from the metadata
   * of the page or the name of the file the page is associated with.
   */
  lazy val title: String = metadata.title.getOrElse(name)

  /** Represents the name of the page, which is based on the file name. */
  lazy val name: String = {
    FileHelper.stripExtension(
      path.getFileName.toString
    ).replaceAll("[^\\w\\s\\d]", " ")
    .toLowerCase.split(' ').map(_.capitalize).mkString(" ")
  }

  /**
   * Returns whether or not the page is using the default layout.
   *
   * @return True if using the default layout, otherwise false
   */
  def isUsingDefaultLayout: Boolean = metadata.usingDefaultLayout

  /**
   * Renders the page and writes it to the output path.
   *
   * @param context The context to feed into this page's layout
   * @param path The path to render the file, defaulting to the
   *             page's standard output path
   * @return True if successfully rendered the page, otherwise false
   */
  def render(context: Context, path: Path = outputPath): Boolean = {
    logger.time(Logger.Level.Trace) {
      // Don't render if explicitly turned off
      if (!metadata.render) {
        logger.verbose(s"Skipping rendering (render=${metadata.render})")
        true
      // Don't render fake pages
      } else if (metadata.fake) {
        logger.verbose(s"Skipping rendering (fake=${metadata.fake})")
        true
      } else {
        val _result = Try(writeText(
          path,
          metadata.redirect match {
            case Some(url) =>
              logger.verbose(s"Rendering as redirect to $url")
              Page.Redirect(url).toString
            case None =>
              renderToString(context)
          }
        ))

        _result.failed.foreach(t => {
          val errorName = t.getClass.getName
          val errorMessage = Option(t.getLocalizedMessage).getOrElse("<none>")
          val depth = config.stackTraceDepth()
          val stackTrace =
            if (depth < 0) t.getStackTrace
            else t.getStackTrace.take(depth)

          logger.error(s"!!! Failed: " + errorName)
          logger.error(s"!!! Message: " + errorMessage)
          stackTrace.foreach(ste => logger.error(s"!!! $ste"))
        })

        _result.isSuccess
      }
    }
  }

  /** Represents the output path when the page is rendered. */
  lazy val outputPath: Path = {
    val srcDirPath = {
      val inputDir = config.inputDir()
      val srcDir = config.srcDir()
      Paths.get(inputDir, srcDir)
    }

    val outputDirPath = {
      val outputDir = config.outputDir()
      Paths.get(outputDir)
    }

    val relativePath = srcDirPath.relativize(path)
    val fileName = FileHelper.stripExtension(path.getFileName.toString)

    // Create the output path to the new html file's directory
    val outputPath = Option(relativePath.getParent)
      .map(outputDirPath.resolve)
      .getOrElse(outputDirPath)
    val htmlDirPath =
      if (isIndexPage) outputPath
      else outputPath.resolve(fileName)
    Files.createDirectories(htmlDirPath)

    // Create the path to the html file itself
    htmlDirPath.resolve("index.html")
  }

  /**
   * Represents whether or not this page represents an index page.
   */
  lazy val isIndexPage: Boolean = {
    Files.isRegularFile(path) &&
      FileHelper.stripExtension(path.getFileName.toString) == "index"
  }

  /**
   * Represents whether or not this page is at the root of the website.
   *
   * E.g. /example.html is at the root while /my/example.html is not.
   */
  lazy val isAtRoot: Boolean = {
    val srcDirPath = {
      val inputDir = config.inputDir()
      val srcDir = config.srcDir()
      Paths.get(inputDir, srcDir)
    }
    srcDirPath.relativize(path) == path.getFileName
  }

  /**
   * Renders the page as a string.
   *
   * @param context The context to feed into this page's layout
   * @return The textual representation of the rendering
   */
  private def renderToString(context: Context): String = {
    val layout = newLayoutInstance(context)

    logger.trace(s"Rendering markdown as html")
    val content = Page.renderer.render(pageNode)

    logger.trace(s"Applying layout ${layout.getClass.getName}")
    layout.toString(content)
  }

  /**
   * Generates a new instance of this page's layout using the provided context.
   *
   * @param context The context to feed into this page's layout
   * @return The new layout instance
   */
  private def newLayoutInstance(context: Context): Layout = {
    if (metadata.usingDefaultLayout) {
      logger.trace(s"Loading default layout")
      defaultLayout(context)
    } else {
      val layoutName = metadata.layout
      logger.trace(s"Loading layout $layoutName")
      layoutFromClassName(layoutName, context)
    }
  }

  /**
   * Retrieves the default layout.
   *
   * @param context The context to provide to the default layout
   * @return The default layout instance
   */
  private def defaultLayout(context: Context): Layout = {
    val defaultLayoutClassName = config.defaultPageLayout()
    layoutFromClassName(defaultLayoutClassName, context)
  }

  /**
   * Creates a new instance of the specified layout class.
   *
   * @param className The fully-qualified class name
   * @param context The context to provide to the layout
   * @return The new layout instance
   * @throws ClassNotFoundException If the class is missing
   * @throws RuntimeException If the specified class is not a layout
   */
  @throws[ClassNotFoundException]
  @throws[RuntimeException]
  private def layoutFromClassName(
    className: String,
    context: Context
  ): Layout = {
    // Retrieve the layout class
    // TODO: Replace with support for custom classloader that contains the
    //       stock layout built into the sitegen project as well as any
    //       custom classes for unique themes
    val layoutClass = Class.forName(className)

    // Validate the provided class is of the layout type
    if (!Layout.classIsLayout(layoutClass)) {
      val n = classOf[Layout].getName
      val dn = layoutClass.getName
      throw new RuntimeException(s"$dn is not an instance of $n")
    }

    val layout = layoutClass.newInstance().asInstanceOf[Layout]
    layout.context_=(context)
    layout
  }

  /**
   * Parses a file at the specified path as flexmark node.
   *
   * @param path The path to the markdown file
   * @return The new flexmark node instance
   */
  private def parseMarkdownFile(path: Path): Node = {
    logger.trace(s"Parsing markdown file")
    Page.parser.parseReader(
      Files.newBufferedReader(path, Charset.forName("UTF-8"))
    )
  }

  /**
   * Parses the metadata from the specified flexmark node.
   *
   * @param node The flexmark node whose metadata to parse
   * @return The metadata instance
   */
  private def parseMetadata(node: Node): Metadata = {
    logger.trace(s"Extracting front matter from markdown file")
    val yamlVisitor = new AbstractYamlFrontMatterVisitor
    yamlVisitor.visit(node)
    Metadata.fromJavaMap(config, yamlVisitor.getData)
  }

  /**
   * Writes the text to the specified path.
   *
   * @param path The path to the file to write
   * @param text The text for the file
   */
  private def writeText(path: Path, text: String): Unit = {
    Files.write(
      path,
      text.getBytes("UTF-8"),
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE
    )
  }
}

object Page {
  import scala.collection.JavaConverters._

  /** Represents extensions for the parser and renderer. */
  private lazy val extensions = Seq(
    AbbreviationExtension.create(),
    AnchorLinkExtension.create(),
    TablesExtension.create(),
    YamlFrontMatterExtension.create()
  ).asJava

  /** Represents the Markdown parser. */
  private lazy val parser = Parser.builder().extensions(extensions).build()

  /** Represents options to be fed into the html renderer. */
  private lazy val options = (new MutableDataSet)
    .set[java.lang.Boolean](HtmlRenderer.GENERATE_HEADER_ID, true)

  /** Represents the Markdown => HTML renderer. */
  private lazy val renderer = HtmlRenderer
    .builder(options)
    .extensions(extensions)
    .build()

  object Session {
    /**
     * Creates a new page instance with session logging.
     *
     * @param config Used to provide defaults
     * @param path The path to the raw page content
     * @return The new page instance
     */
    def newInstance(
      config: Config,
      path: Path
    ): Page = new Page(
      config,
      path,
      new Logger(classOf[Page])
        .newSession(path.toString)
        .init(Logger.Level.Verbose)
    )
  }

  /**
   * Represents a redirection page.
   */
  object Redirect {
    import scalatags.Text.all._

    /**
     * Generates a redirect page using the provided url.
     *
     * @param url The url that the page should redirect to
     * @return The page content
     */
    def apply(url: String): Modifier = {
      val q = "\""
      html(lang := "en-US")(
        head(
          meta(charset := "UTF-8"),
          meta(httpEquiv := "refresh", content := s"1; url=$url"),
          script(`type` := "text/javascript")(
            s"window.location.href = $q$url$q;"
          ),
          tag("title")("Page Redirection")
        ),
        body(
          raw("If you are not redirected automatically, "),
          a(href := url)("follow this link"),
          raw(".")
        )
      )
    }
  }

  /**
   * Creates a new page instance with no logging.
   *
   * @param config Used to provide defaults
   * @param path The path to the raw page content
   * @return The new page instance
   */
  def newInstance(
    config: Config,
    path: Path
  ): Page = new Page(config, path, Logger.Silent)
}
