import sbt._

object Dependencies {
  lazy val ScalaTags = Seq(
    "com.lihaoyi" %% "scalatags" % "0.6.3" // Main doc generator
  )

  lazy val Scallop = Seq(
    "org.rogach" %% "scallop" % "3.0.3" // CLI Support
  )

  lazy val Flexmark = Seq(
    "com.vladsch.flexmark" % "flexmark" % "0.22.22", // Markdown support
    "com.vladsch.flexmark" % "flexmark-ext-yaml-front-matter" % "0.22.22", // Front matter support
    "com.vladsch.flexmark" % "flexmark-ext-tables" % "0.22.22", // Tables support
    "com.vladsch.flexmark" % "flexmark-ext-abbreviation" % "0.22.22", // Abbreviation support
    "com.vladsch.flexmark" % "flexmark-ext-anchorlink" % "0.22.22" // Anchor link support
  )

  lazy val Toml4j = Seq(
    "com.moandjiezana.toml" % "toml4j" % "0.7.1" // TOML support
  )

  lazy val Jgit = Seq(
    "org.eclipse.jgit" % "org.eclipse.jgit" % "4.8.0.201706111038-r" // Git support
  )

  lazy val CommonsCodec = Seq(
    "commons-codec" % "commons-codec" % "1.10" // Base64 encoding support
  )

  lazy val CommonsIO = Seq(
    "commons-io" % "commons-io" % "2.5" // File copy support
  )

  lazy val Coursier = Seq(
    // For downloading themes via Maven
    "io.get-coursier" %% "coursier" % "1.0.0-RC8",
    "io.get-coursier" %% "coursier-cache" % "1.0.0-RC8"
  )

  lazy val Unfiltered = Seq(
    // For hosting local server containing generated sources
    "ws.unfiltered" %% "unfiltered" % "0.9.1",
    "ws.unfiltered" %% "unfiltered-filter" % "0.9.1",
    "ws.unfiltered" %% "unfiltered-jetty" % "0.9.1"
  )

  lazy val Slf4j = Seq(
    // For logging used with jetty from unfiltered
    "org.slf4j" % "slf4j-log4j12" % "1.7.5"
  )
}
