package org.senkbeil.sitegen

import java.net.URL
import java.nio.file._

import org.apache.commons.io.FileUtils
import org.senkbeil.sitegen.Config.CommandGenerateOptions
import org.senkbeil.sitegen.layouts.Context
import org.senkbeil.sitegen.structures.{MenuItem, Page}
import org.senkbeil.sitegen.utils.FileHelper

/**
 * Represents a generator of content based on a configuration.
 *
 * @param generateOptions The generate-specific options to use
 */
class Generator(
  private val generateOptions: CommandGenerateOptions
) extends Runnable {
  /** Logger for this class. */
  private lazy val logger = new Logger(this.getClass)

  /** Used for Github pages. */
  private val NoJekyllFile = ".nojekyll"

  /** Used for Google search. */
  private val SitemapFile = "sitemap.xml"

  /** Used for statistics about total theme sources. */
  private lazy val themeSourceCount =
    generateOptions.sbtProjectThemes().size +
    generateOptions.classDirThemes().size +
    generateOptions.jarThemes().size +
    generateOptions.mavenThemes().size

  /** Represents the theme manager used for generating the site. */
  lazy val themeManager: ThemeManager = {
    val manager = new ThemeManager(generateOptions)

    // Explicitly load themes
    generateOptions.sbtProjectThemes().foreach(t => {
      logger.trace(s"Loading theme from sbt project: ${t.file.getPath}")
      manager.loadTheme(t)
    })
    generateOptions.classDirThemes().foreach(t => {
      logger.trace(s"Loading theme from class directory: ${t.file.getPath}")
      manager.loadTheme(t)
    })
    generateOptions.jarThemes().foreach(t => {
      logger.trace(s"Loading theme from jar: ${t.file.getPath}")
      manager.loadTheme(t)
    })
    generateOptions.mavenThemes().foreach(t => {
      logger.trace(s"Loading theme from Maven: ${t.depString}")
      manager.loadTheme(t)
    })

    manager
  }

  /**
   * Creates a generator using the stock `config.generate` options.
   *
   * @param config The configuration to use when generating files
   * @return The new generator instance
   */
  def this(config: Config) = this(config.generate)

  /**
   * Runs the generator.
   */
  def run(): Unit = logger.time(Logger.Level.Info, "Gen finished after ") {
    // Provide warning about site host not being set when generating
    if (!generateOptions.siteHost.supplied) logger.warn(
      "No site host provided when generating! " +
      s"${generateOptions.siteHost()} will be used!"
    )

    logger.info(s"Loading theme data from $themeSourceCount sources")
    themeManager // Explicitly load themes

    val outputDir = generateOptions.outputDir()

    val inputDir = generateOptions.inputDir()
    val srcDir = generateOptions.srcDir()
    val staticDir = generateOptions.staticDir()

    val outputDirPath = Paths.get(outputDir)
    outputDirPath.getFileName

    // Re-create the output directory
    logger.trace(s"Deleting and recreating $outputDirPath")
    FileUtils.deleteDirectory(outputDirPath.toFile)
    Files.createDirectories(outputDirPath)

    // Copy all static content
    val staticDirPath = Paths.get(inputDir, staticDir)
    logger.trace(s"Copying static files from $staticDirPath to $outputDirPath")
    FileUtils.copyDirectory(staticDirPath.toFile, outputDirPath.toFile)

    // Generate .nojekyll file
    if (generateOptions.doNotGenerateNoJekyllFile()) {
      logger.trace(s"Not generating $NoJekyllFile")
    } else {
      logger.trace(s"Generating $NoJekyllFile")
      val noJekyllFilePath = outputDirPath.resolve(NoJekyllFile)
      Files.createFile(noJekyllFilePath)
    }

    // Process all markdown files
    val srcDirPath = Paths.get(inputDir, srcDir)
    logger.trace(s"Processing markdown files from $srcDirPath")

    val linkedMainMenuItems = MenuItem.fromPath(
      generateOptions,
      themeManager,
      srcDirPath,
      dirUseFirstChild = true
    ).map(_.copy(children = Nil))
    val linkedSideMenuItems = MenuItem.fromPath(
      generateOptions,
      themeManager,
      srcDirPath
    )

    // Create our layout context
    val context = Context(
      mainMenuItems = linkedMainMenuItems,
      sideMenuItems = linkedSideMenuItems
    )

    // For each markdown file, generate its content and produce a file
    val mdFiles = FileHelper.markdownFiles(srcDirPath)
    val pages = mdFiles.map(f =>
      Page.Session.newInstance(generateOptions, themeManager, f)
    ).toSeq

    pages.foreach(page => {
      @inline def markMenuItem(menuItems: Seq[MenuItem]): Seq[MenuItem] = {
        menuItems.map(menuItem => {
          menuItem.copy(
            selected = menuItem.representsPage(page),
            children = markMenuItem(menuItem.children)
          )
        })
      }

      val markedSideMenuItems = markMenuItem(context.sideMenuItems)
      val markedMainMenuItems = context.mainMenuItems.map(menuItem => {
        val matchingItem = markedSideMenuItems.find(_.name == menuItem.name)
        val isSelected = matchingItem.exists(_.isDirectlyOrIndirectlySelected)
        menuItem.copy(selected = isSelected)
      })

      page.render(context.copy(
        title = Some(page.title),
        mainMenuItems = markedMainMenuItems,
        sideMenuItems = markedSideMenuItems
      ))
    })

    // Produce a sitemap.xml representing the links
    if (generateOptions.doNotGenerateSitemapFile()) {
      logger.trace(s"Not generating $SitemapFile")
    } else {
      logger.trace(s"Generating $SitemapFile")
      createSitemapFile(
        generateOptions.siteHost(),
        pages,
        outputDirPath.resolve(SitemapFile)
      )
    }
  }

  /**
   * Creates a sitemap file based on the given pages.
   *
   * @param hostUrl The host to use for all pages such as http://www.example.com
   * @param pages The collection of pages whose links to use in the sitemap
   * @param outputPath The path to the sitemap file
   */
  private def createSitemapFile(
    hostUrl: URL,
    pages: Seq[Page],
    outputPath: Path
  ): Unit = {
    val dateString = {
      val format = new java.text.SimpleDateFormat("yyyy-MM-dd")
      format.format(new java.util.Date())
    }

    val xmlUrls = pages.filterNot(p =>
      p.metadata.fake || !p.metadata.render
    ).map(p => {
      <url>
        <loc>{ hostUrl.toURI.resolve(p.absoluteLink).toString }</loc>
        <lastmod>{ dateString }</lastmod>
        <changefreq>daily</changefreq>
        <priority>{ if (p.isIndexPage && p.isAtRoot) 1.0 else 0.5 }</priority>
      </url>
    })

    val sitemapXml =
      <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
        { xmlUrls }
      </urlset>

    import scala.xml.XML
    val writer = Files.newBufferedWriter(
      outputPath,
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE
    )
    XML.write(writer, sitemapXml, "UTF-8", xmlDecl = true, doctype = null)
    writer.flush()
    writer.close()
  }
}
