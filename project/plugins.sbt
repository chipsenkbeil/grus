logLevel := Level.Warn

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers += Resolver.sonatypeRepo("releases")

// Used for building fat jars
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")

// Use to respect cross-compilation settings
addSbtPlugin("com.eed3si9n" % "sbt-doge" % "0.1.5")

// Use to inject version into code itself
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")
