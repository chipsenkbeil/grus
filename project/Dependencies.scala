import sbt.Keys._
import sbt._

object Dependencies {
  lazy val unfilteredVersion: SettingKey[String] = settingKey[String](
    "Version of Unfiltered used in projects"
  )

  /** Dependency-specific project settings. */
  val settings = Seq(
    // Contains the version of unfiltered used
    unfilteredVersion := "0.9.0-beta2",

    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "scalatags" % "0.6.3", // Main doc generator
      "org.rogach" %% "scallop" % "2.0.5", // CLI Support

      "com.vladsch.flexmark" % "flexmark-java" % "0.10.3", // Markdown support
      "com.vladsch.flexmark" % "flexmark-ext-yaml-front-matter" % "0.10.3", // Front matter support
      "com.vladsch.flexmark" % "flexmark-ext-gfm-tables" % "0.10.3", // Tables support
      "com.vladsch.flexmark" % "flexmark-ext-abbreviation" % "0.10.3", // Abbreviation support
      "com.vladsch.flexmark" % "flexmark-ext-anchorlink" % "0.10.3", // Anchor link support

      "org.eclipse.jgit" % "org.eclipse.jgit" % "4.6.0.201612231935-r", // Git support

      "commons-codec" % "commons-codec" % "1.10", // Base64 encoding support
      "commons-io" % "commons-io" % "2.5", // File copy support

      // For hosting local server containing generated sources
      "ws.unfiltered" %% "unfiltered" % unfilteredVersion.value,
      "ws.unfiltered" %% "unfiltered-filter" % unfilteredVersion.value,
      "ws.unfiltered" %% "unfiltered-jetty" % unfilteredVersion.value,

      // For logging used with jetty from unfiltered
      "org.slf4j" % "slf4j-log4j12" % "1.7.5"
    )
  )
}
