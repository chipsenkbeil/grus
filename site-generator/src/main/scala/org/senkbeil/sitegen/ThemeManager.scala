package org.senkbeil.sitegen

import java.io.File
import java.net.{URL, URLClassLoader}

import org.senkbeil.sitegen.exceptions._

import scala.annotation.tailrec
import scala.util.Try

/**
 * Loads and manages themes for the site generator.
 *
 * @param config The configuration to use when working with themes
 * @param classLoader The class loader to use as the parent to the
 *                    manager's internal class loader
 */
class ThemeManager(
  private val config: Config,
  private val classLoader: ClassLoader = classOf[ThemeManager].getClassLoader
) {
  /** Represents the internal class loader used to contain all themes. */
  private lazy val internalClassLoader = new URLClassLoader(
    Array[URL](),
    classLoader
  ) {
    /** Exposed to allow adding new classes to be added. */
    def appendUrl(url: URL): Unit = super.addURL(url)
  }

  /**
   * Loads the specified class from the theme manager.
   *
   * @param className The fully-qualified name of the class to load
   *
   * @return The class instance
   * @throws ClassNotFoundException If the class with the specified name is
   *                                not found
   */
  @throws[ClassNotFoundException]
  def loadClass(className: String): Class[_] = Class.forName(
    className,
    true,
    internalClassLoader
  )

  /**
   * Loads the specified class from the theme manager.
   *
   * @param className The fully-qualified name of the class to load
   *
   * @return Some class instance if found, otherwise None
   */
  def loadClassOption(className: String): Option[Class[_]] =
    Try(loadClass(className)).toOption

  /**
   * Loads the file(s) into the internal class loader of the theme manager.
   * Will recursively look through directories for files to load.
   *
   * @param files The files containing the themes to load
   *
   * @throws InvalidFileFormatThemeException If provided a file that is not
   *                                         a jar
   */
  @throws[InvalidFileFormatThemeException]
  @tailrec private def loadThemeJars(files: File*): Unit = {
    val newFiles = files.flatMap(f => {
      if (f.isDirectory) f.listFiles()
      else Seq(f)
    })

    if (newFiles.exists(_.isDirectory)) loadThemeJars(newFiles: _*)
    else newFiles.foreach(f => {
      if (!isJarFile(f)) throw InvalidFileFormatThemeException(f)
      internalClassLoader.appendUrl(f.toURI.toURL)
    })
  }

  /**
   * Loads the relevant files for the specified theme into the internal
   * class loader of the theme manager.
   *
   * @param theme The information about the theme
   *
   * @throws ThemeException If unable to resolve a dependency or access a
   *                        theme-related file
   */
  @throws[ThemeException]
  def loadTheme(theme: Theme): Unit = theme match {
    case LocalSbtProjectTheme(dir) =>
      if (!isSbtProjectDir(dir))
        throw InvalidFileFormatThemeException(dir, "sbt project directory")

      val targetDir = dir.listFiles()
        .find(_.getName == "target").filter(_.isDirectory)
      if (targetDir.isEmpty)
        throw MissingContentThemeException("sbt target directory")

      val majorVersion = BuildInfo.scalaVersion.split('.').take(2).mkString(".")
      val scalaDir = targetDir.get.listFiles()
        .find(_.getName == s"scala-$majorVersion").filter(_.isDirectory)
      if (scalaDir.isEmpty)
        throw MissingContentThemeException(s"sbt scala-$majorVersion directory")

      val classDir = scalaDir.get.listFiles()
        .find(_.getName == "classes").filter(_.isDirectory)
      if (classDir.isEmpty)
        throw MissingContentThemeException("sbt classes directory")

      internalClassLoader.appendUrl(classDir.get.toURI.toURL)

    case LocalClassDirTheme(dir) =>
      if (!isClassDir(dir))
        throw InvalidFileFormatThemeException(dir, "class directory")
      internalClassLoader.appendUrl(dir.toURI.toURL)

    case LocalJarTheme(jar) =>
      if (!isJarFile(jar)) throw InvalidFileFormatThemeException(jar, "jar")
      internalClassLoader.appendUrl(jar.toURI.toURL)

    case mavenTheme: MavenTheme => retrieveThemeFiles(mavenTheme) match {
      case Left(exception) =>
        throw exception
      case Right(files) =>
        files.filter(isJarFile).foreach(f => loadThemeJars(f))
    }
  }
  /**
   * Retrieves relevant files for the specified theme.
   *
   * @param mavenTheme The information about the theme hosted via Maven
   *
   * @return Either the collection of files (jars) tied to the theme or
   *         a collection of theme-specific exceptions that occurred
   */
  protected def retrieveThemeFiles(
    mavenTheme: MavenTheme
  ): Either[BundledThemeException, Seq[File]] = {
    import coursier._

    val MavenTheme(organization, artifact, version) = mavenTheme

    // Mark theme to retrieve
    val start = Resolution(Set(
      Dependency(Module(organization, artifact), version)
    ))

    // Mark repositories to check for theme
    val repositories = Seq(Cache.ivy2Local) ++ config.generate.mavenThemeRepos()

    val fetch = Fetch.from(repositories, Cache.fetch())
    val resolution = start.process.run(fetch).unsafePerformSync
    val resErrors = resolution.metadataErrors.map { case ((mod, ver), errs) =>
      FailedResolutionThemeException(mod.organization, mod.name, ver, errs)
    }

    // If failed to resolve part of theme, exit and report
    if (resErrors.nonEmpty) Left(BundledThemeException(resErrors))
    else {
      import scalaz.\/
      import scalaz.concurrent.Task

      val localArtifacts: Seq[FileError \/ File] = Task.gatherUnordered(
        resolution.artifacts.map(Cache.file(_).run)
      ).unsafePerformSync

      // If failed to access part of the theme's files, exit and report
      if (localArtifacts.exists(_.isLeft))
        Left(BundledThemeException(
          localArtifacts.flatMap(_.swap.toOption)
            .map(FileErrorThemeException.apply)
        ))
      // Otherwise, return the collection of files
      else
        Right(localArtifacts.flatMap(_.toOption))
    }
  }

  /**
   * Determines if the file represents a jar.
   *
   * @param file The file to inspect
   *
   * @return True if a jar file, otherwise false
   */
  private def isJarFile(file: File): Boolean =
    file.isFile && file.getPath.endsWith(".jar")

  /**
   * Determines if the file represents a compiled Java source (class).
   *
   * @param file The file to inspect
   *
   * @return True if a class file, otherwise false
   */
  private def isClassFile(file: File): Boolean =
    file.isFile && file.getPath.endsWith(".class")

  /**
   * Determines if the file represents a directory of class files.
   *
   * @param file The file to inspect
   *
   * @return True if a directory of class files, otherwise false
   */
  private def isClassDir(file: File): Boolean =
    file.isDirectory //&& file.listFiles().forall(isClassFile)

  /**
   * Determines if the file represents an sbt project directory.
   *
   * @param file The file to inspect
   *
   * @return True if a sbt project directory, otherwise false
   */
  private def isSbtProjectDir(file: File): Boolean =
    file.isDirectory //&& file.listFiles().forall(isClassFile)
}
