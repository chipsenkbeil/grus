//
// MAIN PROJECT CONFIGURATION
//
lazy val root = project
  .in(file("."))
  .settings(Common.settings: _*)
  .settings(
    name := "root",
    // Do not publish the aggregation project
    publishArtifact := false,
    publishLocal := {}
  ).aggregate(
    siteGenerator,
    sbtPlugin
  ).enablePlugins(CrossPerProjectPlugin)

//
// SITE GENERATOR PROJECT CONFIGURATION
//
lazy val siteGenerator = project
  .in(file("site-generator"))
  .configs(IntegrationTest)
  .settings(Common.settings: _*)
  .settings(Defaults.itSettings: _*)
  .settings(Dependencies.settings: _*)
  .enablePlugins(
    BuildInfoPlugin,
    CrossPerProjectPlugin
  ).settings(
    name := "site-generator",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "org.senkbeil.sitegen"
  )

//
// SBT PLUGIN PROJECT CONFIGURATION
//
lazy val sbtPlugin = project
  .in(file("sbt-plugin"))
  .configs(IntegrationTest)
  .settings(Common.settings: _*)
  .settings(Defaults.itSettings: _*)
  .settings(SbtPlugin.settings: _*)
  .enablePlugins(
    BuildInfoPlugin,
    CrossPerProjectPlugin
  ).settings(
    name := "sbt-site-generator",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "org.senkbeil.sitegen.sbt"
  ).dependsOn(siteGenerator)

