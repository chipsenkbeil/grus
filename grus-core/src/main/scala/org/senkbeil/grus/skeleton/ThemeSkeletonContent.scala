package org.senkbeil.grus.skeleton
import java.nio.file.{Path, Paths}
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date

import org.senkbeil.grus.Config.CommandSkeletonOptions

/**
 * Represents skeleton content for a theme.
 *
 * @param skeletonOptions The options provided to use when generating
 *                        the skeleton outline
 */
class ThemeSkeletonContent(
  private val skeletonOptions: CommandSkeletonOptions
) extends SkeletonContent {
  import SkeletonContent.Implicits._
  private lazy val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  private lazy val dateString = dateFormat.format(Date.from(Instant.now()))

  val DefaultReadmeFile: Path = Paths.get("README.md")
  val DefaultProjectFile: Path = Paths.get("build.sbt")
  val DefaultResourcePath: Path = Paths.get("src", "main", "resources")
  val DefaultScalaPath: Path = Paths.get("src", "main", "scala")
  val DefaultExamplePackagePath: Path = Paths.get("com", "example")

  private val SrcRoot = DefaultScalaPath.resolve(DefaultExamplePackagePath)
  private val LayoutsRoot = SrcRoot.resolve("layouts")
  private val StylesRoot = SrcRoot.resolve("styles")

  private lazy val ReadmeText = """
  |# Your Grus Theme
  |
  |TODO: Provide description of content
  """.stripMargin

  private lazy val LayoutBI = org.senkbeil.grus.layouts.BuildInfo
  private lazy val BuildSbtText = s"""
  |/**
  | * Grus theme build.sbt
  | * Generated $dateString
  | */
  |name := "grus-example-theme"
  |
  |libraryDependencies ++= Seq(
  |  "${LayoutBI.organization}" %% "${LayoutBI.name}" % "${LayoutBI.version}"
  |)
  """.stripMargin
  private lazy val ExamplePageText = """
package com.example.layouts

import com.example.styles.PageStyle
import org.senkbeil.grus.layouts.{Context, Page}
import org.senkbeil.grus.layouts.Implicits._

import scalatags.Text.all._
import scalatags.Text

/**
 * Represents the layout for a common site page.
 *
 * @param selectedMenuItems Will mark each menu item whose name is provided
 *                          as selected
 * @param syntaxHighlightTheme The theme to use for syntax highlighting; themes
 *                             are from the highlight.js list
 */
class SitePage(
  val selectedMenuItems: Seq[String] = Nil,
  val syntaxHighlightTheme: String = "agate"
) extends Page {
  override protected def preHeadContent(context: Context): Seq[Modifier] =
    super.preHeadContent(context) ++ Seq(
      PageStyle.styleSheetText.toStyleTag
    )
}
""".stripMargin

  private lazy val ExampleStyleText = """
package com.example.styles

import scalatags.Text.all._
import scalatags.stylesheet._

/**
 * Represents stylesheet for all pages.
 */
object PageStyle extends CascadingStyleSheet {
  initStyleSheet()

  import scalatags.Text.styles2.{content => afterContent}

  /* Provide your CSS stylings here */
}
  """.stripMargin

  /**
   * Transforms the content into a mapping of relative paths to the
   * corresponding data.
   *
   * @return Map of NIO paths to byte arrays of content
   */
  override def toMap: Map[Path, Array[Byte]] = Map(
    // Top level content
    DefaultReadmeFile -> ReadmeText,
    DefaultProjectFile -> BuildSbtText,

    // src/main/scala content
    LayoutsRoot.resolve("ExamplePage.scala") -> ExamplePageText,
    StylesRoot.resolve("ExampleStyle.scala") -> ExampleStyleText
  )
}
