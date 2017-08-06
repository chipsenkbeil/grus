import sbt.Keys._

object Core {
  /** Dependency-specific core project settings. */
  val settings = Seq(
    libraryDependencies ++= Seq(
      Dependencies.ScalaTags,
      Dependencies.Scallop,
      Dependencies.Flexmark,
      Dependencies.Toml4j,
      Dependencies.Jgit,
      Dependencies.CommonsIO,
      Dependencies.CommonsCodec,
      Dependencies.Coursier,
      Dependencies.Unfiltered,
      Dependencies.Slf4j
    ).flatten
  )
}
