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
    siteGeneratorCore,
    siteGeneratorLayouts,
    sbtPlugin
  ).enablePlugins(CrossPerProjectPlugin)

//
// SITE GENERATOR PROJECT CONFIGURATION
//
lazy val siteGeneratorCore = project
  .in(file("site-generator-core"))
  .configs(IntegrationTest)
  .settings(Common.settings: _*)
  .settings(Defaults.itSettings: _*)
  .settings(Core.settings: _*)
  .enablePlugins(
    BuildInfoPlugin,
    CrossPerProjectPlugin
  ).settings(
    name := "site-generator-core",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "org.senkbeil.sitegen"
  ).dependsOn(
    siteGeneratorLayouts % "compile->compile;test->compile;it->compile"
  )

//
// SITE GENERATOR LAYOUTS PROJECT CONFIGURATION
//
lazy val siteGeneratorLayouts = project
  .in(file("site-generator-layouts"))
  .configs(IntegrationTest)
  .settings(Common.settings: _*)
  .settings(Defaults.itSettings: _*)
  .settings(Layouts.settings: _*)
  .enablePlugins(
    BuildInfoPlugin,
    CrossPerProjectPlugin
  ).settings(
    name := "site-generator-layouts",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "org.senkbeil.sitegen.layouts"
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
  ).dependsOn(siteGeneratorCore)

