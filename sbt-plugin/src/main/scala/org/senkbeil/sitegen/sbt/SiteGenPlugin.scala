package org.senkbeil.sitegen.sbt

import sbt.Attributed._
import sbt.Def.Initialize
import sbt.Keys._
import sbt._

object SiteGenPlugin extends AutoPlugin {
  lazy val sitegen: Configuration = config("sitegen")

  val sitegenVersion: SettingKey[String] = settingKey[String]("sitegen version")

  override def trigger: PluginTrigger = allRequirements

  override lazy val projectSettings: Seq[Setting[_]] =
    sitegenSettings(sitegen, Compile)

  // Same as Defaults.runTask from sbt, but accepting default arguments too
  def runTask(
    classpath: Initialize[Task[Classpath]],
    mainClassTask: Initialize[Task[Option[String]]],
    scalaRun: Initialize[Task[ScalaRun]],
    defaultArgs: Initialize[Task[Seq[String]]]
  ): Initialize[InputTask[Unit]] = {
    val parser = Def.spaceDelimited()
    Def.inputTask {
      val mainClass = mainClassTask.value getOrElse sys.error("No main class detected.")
      val userArgs = parser.parsed
      val args = if (userArgs.isEmpty) defaultArgs.value else userArgs
      toError(scalaRun.value.run(mainClass, data(classpath.value), args, streams.value.log))
    }
  }

  def defaultArgs(initialCommands: String): Seq[String] =
    if (initialCommands.isEmpty)
      Nil
    else
      Seq(initialCommands)

  def sitegenSettings(sitegenConf: Configuration, underlyingConf: Configuration) = inConfig(sitegenConf)(
    // Getting references to undefined settings when doing sitegen:run without these
    Defaults.compileSettings ++

      // Seems like the class path provided to sitegen:run doesn't take into account the libraryDependencies below
      // without these
      Classpaths.ivyBaseSettings ++

      Seq(
        sitegenVersion := {
          val fromEnv = sys.env.get("SITEGEN_VERSION")
          def fromProps = sys.props.get("sitegen.version")
          val default = BuildInfo.version

          fromEnv
            .orElse(fromProps)
            .getOrElse(default)
        },

        libraryDependencies += "org.senkbeil" %% "site-generator" % sitegenVersion.value cross CrossVersion.binary,
        configuration := underlyingConf,

        /* Overriding run and runMain defined by compileSettings so that they use fullClasspath of this scope (sitegen),
         * taking into account the extra libraryDependencies above, and we can also supply default arguments
         * (initialCommands as predef). */
        run := {
          runTask(fullClasspath, mainClass in run, runner in run, (initialCommands in console).map(defaultArgs)).evaluated
        },
        runMain := {
          Defaults.runMainTask(fullClasspath, runner in run).evaluated
        },

        mainClass := Some("org.senkbeil.sitegen.Main"),

        /* Required for the input to be provided to the site generator tool */
        connectInput := true
      )
  )

}

