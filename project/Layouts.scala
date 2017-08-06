import sbt.Keys._
import sbt._

object Layouts {
  /** Dependency-specific layout project settings. */
  val settings = Seq(
    libraryDependencies ++= Seq(
      Dependencies.ScalaTags
    ).flatten
  )
}
