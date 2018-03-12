package org.senkbeil.grus.skeleton

import java.nio.file.{Path, Paths}

import org.senkbeil.grus.Config.CommandSkeletonOptions

/**
 * Represents skeleton content for a website.
 *
 * @param skeletonOptions The options provided to use when generating
 *                        the skeleton outline
 */
class WebsiteSkeletonContent(
  private val skeletonOptions: CommandSkeletonOptions
) extends SkeletonContent {
  import SkeletonContent.Implicits._

  val DefaultReadmeFile: Path = Paths.get("README.md")
  val DefaultGrusTomlFile: Path = Paths.get("grus.toml")
  val DefaultSitePath: Path = Paths.get("site")
  val DefaultSiteSrcPath: Path = DefaultSitePath.resolve("src")
  val DefaultSiteStaticPath: Path = DefaultSitePath.resolve("static")
  val DefaultPlaceholderFile: Path = Paths.get(".placeholder")

  private lazy val RootImgPath = DefaultSiteStaticPath.resolve("img")
  private lazy val RootScriptsPath = DefaultSiteStaticPath.resolve("scripts")
  private lazy val RootStylesPath = DefaultSiteStaticPath.resolve("styles")

  private lazy val ReadmeText = s"""
  |# Your Grus Website
  |
  |Welcome to your Grus-powered website!
  """.stripMargin
  private lazy val GrusTomlText = s"""
  |[generate]
  |
  |[skeleton]
  |
  |[publish]
  |
  |[serve]
  """.stripMargin

  private lazy val IndexMdText = s"""
  |---
  |title: Main Page
  |layout: ${classOf[org.senkbeil.grus.layouts.Page].getName}
  |---
  |
  |Hello world!
  """.stripMargin
  private lazy val CnameText = """
  |example.com
  """.stripMargin
  private lazy val PlaceholderText = ""

  /**
   * Transforms the content into a mapping of relative paths to the
   * corresponding data.
   *
   * @return Map of NIO paths to byte arrays of content
   */
  override def toMap: Map[Path, Array[Byte]] = Map(
    // Top level content
    DefaultReadmeFile -> ReadmeText,
    DefaultGrusTomlFile -> GrusTomlText,

    // Src content
    DefaultSiteSrcPath.resolve("index.md") -> IndexMdText,

    // Static content
    DefaultSiteStaticPath.resolve("CNAME") -> CnameText,
    RootImgPath.resolve(DefaultPlaceholderFile) -> PlaceholderText,
    RootScriptsPath.resolve(DefaultPlaceholderFile) -> PlaceholderText,
    RootStylesPath.resolve(DefaultPlaceholderFile) -> PlaceholderText
  )
}
