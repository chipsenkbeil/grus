import sbt.Keys._
import sbt._

object Dependencies {
  lazy val unfilteredVersion: SettingKey[String] = settingKey[String](
    "Version of Unfiltered used in projects"
  )

  /** Dependency-specific project settings. */
  val settings = Seq(
    // Contains the version of unfiltered used
    unfilteredVersion := "0.9.1",

    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "scalatags" % "0.6.3", // Main doc generator
      "org.rogach" %% "scallop" % "3.0.3", // CLI Support

      "com.vladsch.flexmark" % "flexmark-java" % "0.20.0", // Markdown support
      "com.vladsch.flexmark" % "flexmark-ext-yaml-front-matter" % "0.20.0", // Front matter support
      "com.vladsch.flexmark" % "flexmark-ext-tables" % "0.20.0", // Tables support
      "com.vladsch.flexmark" % "flexmark-ext-abbreviation" % "0.20.0", // Abbreviation support
      "com.vladsch.flexmark" % "flexmark-ext-anchorlink" % "0.20.0", // Anchor link support

      "org.eclipse.jgit" % "org.eclipse.jgit" % "4.8.0.201706111038-r", // Git support

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
