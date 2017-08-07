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
    publishLocal := {},
    assembly := { null } // Stub out assembly on root
  ).aggregate(
    grusCore,
    grusLayouts,
    sbtPlugin
  ).enablePlugins(CrossPerProjectPlugin)

//
// SITE GENERATOR PROJECT CONFIGURATION
//
lazy val grusCore = project
  .in(file("grus-core"))
  .configs(IntegrationTest)
  .settings(Common.settings: _*)
  .settings(Defaults.itSettings: _*)
  .settings(Core.settings: _*)
  .enablePlugins(
    BuildInfoPlugin,
    CrossPerProjectPlugin
  ).settings(
    name := "grus-core",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "org.senkbeil.grus",
    mainClass in assembly := Some("org.senkbeil.grus.Main"),
    assemblyJarName in assembly := "grus.jar",
    test in assembly := {}
  ).dependsOn(
    grusLayouts % "compile->compile;test->compile;it->compile"
  )

//
// SITE GENERATOR LAYOUTS PROJECT CONFIGURATION
//
lazy val grusLayouts = project
  .in(file("grus-layouts"))
  .configs(IntegrationTest)
  .settings(Common.settings: _*)
  .settings(Defaults.itSettings: _*)
  .settings(Layouts.settings: _*)
  .enablePlugins(
    BuildInfoPlugin,
    CrossPerProjectPlugin
  ).settings(
    name := "grus-layouts",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "org.senkbeil.grus.layouts",
    assembly := { null } // Stub out assembly on layouts
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
    name := "sbt-grus",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "org.senkbeil.grus.sbt",
    assembly := { null } // Stub out assembly on sbt plugin
  ).dependsOn(grusCore)

