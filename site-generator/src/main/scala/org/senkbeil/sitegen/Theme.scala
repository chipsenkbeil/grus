package org.senkbeil.sitegen

import java.io.File

/** Represents a generic theme. */
sealed trait Theme

/**
 * Represents a theme structure hosted on a Maven-oriented site.
 *
 * @param organization The organization (on Maven) containing the theme
 * @param artifact The name of the theme (on Maven)
 * @param version The version of the theme (on Maven)
 */
case class MavenTheme(
  organization: String,
  artifact: String,
  version: String
) extends Theme {
  /**
   * Represents a human-readable string depicting the Maven dependency.
   *
   * @return Human-readable string
   */
  def depString: String = s"$organization:$artifact:$version"
}

/**
 * Represents a theme structure contained in a single, local jar.
 *
 * @param file The jar file containing the theme
 */
case class LocalJarTheme(file: File) extends Theme

/**
 * Represents a theme structure contained in a directory of class files.
 *
 * @param file The directory containing class files for the theme
 */
case class LocalClassDirTheme(file: File) extends Theme

/**
 * Represents a theme structure represented by a local sbt project.
 *
 * @param file The directory of the sbt project representing the theme
 */
case class LocalSbtProjectTheme(file: File) extends Theme
