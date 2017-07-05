import sbt.Keys._

object SbtPlugin {
  /** sbt plugin-specific project settings. */
  val settings = Seq(
    sbtPlugin := true,

    // Force respect (using sbt-doge) of cross scala versions
    scalaVersion := "2.10.6",
    crossScalaVersions := Seq("2.10.6")
  )
}
