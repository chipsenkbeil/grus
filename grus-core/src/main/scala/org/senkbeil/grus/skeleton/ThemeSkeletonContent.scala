package org.senkbeil.grus.skeleton
import java.nio.file.{Path, Paths}

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

  val DefaultReadmeFile: Path = Paths.get("README.md")
  val DefaultProjectFile: Path = Paths.get("build.sbt")
  val DefaultResourcePath: Path = Paths.get("src", "main", "resources")
  val DefaultScalaPath: Path = Paths.get("src", "main", "scala")
  val DefaultExamplePackagePath: Path = Paths.get("com", "example")

  private val SrcRoot = DefaultScalaPath.resolve(DefaultExamplePackagePath)
  private val LayoutsRoot = SrcRoot.resolve("layouts")
  private val StylesRoot = SrcRoot.resolve("styles")

  private lazy val ReadmeText = """
  """.stripMargin
  private lazy val BuildSbtText = """
  """.stripMargin
  private lazy val ExamplePageText = """
  """.stripMargin
  private lazy val ExampleStyleText = """
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
