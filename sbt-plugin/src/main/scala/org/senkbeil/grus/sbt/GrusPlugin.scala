package org.senkbeil.grus.sbt

import sbt._
import sbt.Keys._

object GrusPlugin extends AutoPlugin {
  override def requires = sbt.plugins.JvmPlugin
  override def trigger: PluginTrigger = allRequirements

  override val buildSettings = Seq(commands ++= Seq(
    generateCommand,
    serveCommand,
    publishCommand,
    rawCommand
  ))

  lazy val generateCommand: Command =
    Command.args("grusGenerate", "<arg>") { (state: State, args: Seq[String]) =>
      org.senkbeil.grus.Main.main(("generate" +: args).toArray)
      state
    }

  lazy val serveCommand: Command =
    Command.args("grusServe", "<arg>") { (state: State, args: Seq[String]) =>
      org.senkbeil.grus.Main.main(("serve" +: args).toArray)
      state
    }

  lazy val publishCommand: Command =
    Command.args("grusPublish", "<arg>") { (state: State, args: Seq[String]) =>
      org.senkbeil.grus.Main.main(("publish" +: args).toArray)
      state
    }

  lazy val rawCommand: Command =
    Command.args("grusRaw", "<arg>") { (state: State, args: Seq[String]) =>
      org.senkbeil.grus.Main.main(args.toArray)
      state
    }
}

