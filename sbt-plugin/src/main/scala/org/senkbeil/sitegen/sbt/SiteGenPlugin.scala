package org.senkbeil.sitegen.sbt

import sbt._
import sbt.Keys._

object SiteGenPlugin extends AutoPlugin {
  override def requires = sbt.plugins.JvmPlugin
  override def trigger: PluginTrigger = allRequirements

  override val buildSettings = Seq(commands ++= Seq(
    generateCommand,
    serveCommand,
    publishCommand,
    rawCommand
  ))

  lazy val generateCommand: Command =
    Command.args("generateSite", "<arg>") { (state: State, args: Seq[String]) =>
      org.senkbeil.sitegen.Main.main(("generate" +: args).toArray)
      state
    }

  lazy val serveCommand: Command =
    Command.args("serveSite", "<arg>") { (state: State, args: Seq[String]) =>
      org.senkbeil.sitegen.Main.main(("serve" +: args).toArray)
      state
    }

  lazy val publishCommand: Command =
    Command.args("publishSite", "<arg>") { (state: State, args: Seq[String]) =>
      org.senkbeil.sitegen.Main.main(("publish" +: args).toArray)
      state
    }

  lazy val rawCommand: Command =
    Command.args("rawSite", "<arg>") { (state: State, args: Seq[String]) =>
      org.senkbeil.sitegen.Main.main(args.toArray)
      state
    }
}

