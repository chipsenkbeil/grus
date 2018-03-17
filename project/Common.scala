import sbt._
import Keys._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._

import scala.util.Try

object Common {
  lazy val scalaTestSpanScaleFactor: SettingKey[Double] = settingKey[Double](
    "Sets scaling factor of running tests that are wrapped in scale(...)"
  )

  def settings: Seq[Setting[_]] = Seq(
    version := "0.1.1",

    organization := "org.senkbeil",

    licenses += (
      "Apache-2.0",
      url("https://www.apache.org/licenses/LICENSE-2.0.html")
    ),

    homepage := Some(url("https://get-grus.io")),

    // Default version when not cross-compiling
    scalaVersion := "2.10.6",

    crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.1"),

    scalacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-deprecation", "-unchecked", "-feature",
      "-Xfatal-warnings",
      "-language:reflectiveCalls"
    ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) => Seq(
        "-Ywarn-all", "-target:jvm-1.6"
      )
      case Some((2, 11)) => Seq(
        "-target:jvm-1.6"
      )
      case Some((2, 12)) => Seq(
        "-target:jvm-1.8"
      )
      case _ => Nil
    }),

    scalacOptions in (Test, compile) ++= Seq(
    ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) => Seq()
      case Some((2, 11)) => Seq()
      case Some((2, 12)) => Seq(
        "-deprecation:false" // Ignore deprecation warnings for now to do with
        // eta-expansion of zero-argument function tied to
        // ScalaMock quirks
      )
      case _ => Nil
    }),

    javacOptions ++= Seq(
      "-source", "1.6", "-target", "1.6", "-Xlint:all", "-Werror",
      "-Xlint:-options", "-Xlint:-path", "-Xlint:-processing"
    ),

    scalacOptions in (Compile, doc) ++= Seq(
      "-no-link-warnings" // Suppress problems with Scaladoc @throws links
    ),

    // Options provided to forked JVMs through sbt, based on our .jvmopts file
    javaOptions ++= Seq(
      "-Xms1024M", "-Xmx4096M", "-Xss2m", "-XX:MaxPermSize=256M",
      "-XX:ReservedCodeCacheSize=256M", "-XX:+TieredCompilation",
      "-XX:+CMSPermGenSweepingEnabled", "-XX:+CMSClassUnloadingEnabled",
      "-XX:+UseConcMarkSweepGC", "-XX:+HeapDumpOnOutOfMemoryError"
    ),

    scalaTestSpanScaleFactor := {
      Try(System.getenv("SCALATEST_SPAN_SCALE_FACTOR").toDouble).getOrElse(1.0)
    },

    concurrentRestrictions in Global := {
      val limited = scala.util.Properties.envOrElse(
        "SBT_TASK_LIMIT", "4"
      ).toInt

      // Only limit parallel if told to do so
      if (limited > 0) Seq(Tags.limitAll(limited))
      else Nil
    },

    testOptions in Test += Tests.Argument("-oDF"),

    testOptions in IntegrationTest += Tests.Argument("-oDF"),

    testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest,
      "-F", scalaTestSpanScaleFactor.value.toString
    ),

    testOptions in IntegrationTest += Tests.Argument(TestFrameworks.ScalaTest,
      "-F", scalaTestSpanScaleFactor.value.toString
    ),

    // Run tests in parallel
    // NOTE: Needed to avoid ScalaTest serialization issues
    parallelExecution in Test := true,
    testForkedParallel in Test := true,

    // Run integration tests in parallel
    parallelExecution in IntegrationTest := true,
    testForkedParallel in IntegrationTest := true,


    // Properly handle Scaladoc mappings
    autoAPIMappings := true,

    // Prevent publishing test artifacts
    publishArtifact in Test := false,

    publishMavenStyle := true,

    pomExtra :=
      <scm>
        <url>git@github.com:chipsenkbeil/grus.git</url>
        <connection>scm:git:git@github.com:chipsenkbeil/grus.git</connection>
      </scm>
        <developers>
          <developer>
            <id>senkwich</id>
            <name>Chip Senkbeil</name>
            <url>https://www.chipsenkbeil.org</url>
          </developer>
        </developers>,

    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publishSigned"),
      setNextVersion,
      commitNextVersion,
      releaseStepCommand("sonatypeReleaseAll"),
      pushChanges
    )
  )
}
