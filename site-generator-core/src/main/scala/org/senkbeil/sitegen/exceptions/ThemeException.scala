package org.senkbeil.sitegen.exceptions

import java.io.File

import coursier.FileError

/** Represents a generic exception related to a theme. */
sealed trait ThemeException extends Exception

/**
 * Represents a failure to resolve a dependency (using Coursier) that
 * acts as a theme.
 *
 * @param organization The organization of the dependency
 * @param artifact The artifact of the dependency
 * @param version The version of the dependency
 * @param errors The textual errors describing the failure
 */
case class FailedResolutionThemeException(
  organization: String,
  artifact: String,
  version: String,
  errors: Seq[String]
) extends ThemeException {
  override def toString: String = {
    s"Failed to resolve $organization/$artifact/$version\n" +
      errors.mkString("\n")
  }
}

/**
 * Represents a failure related to an invalid file format (extension)
 * relating to a theme.
 *
 * @param file The file with an invalid format (extension)
 * @param expectedFormat Optional expected format for the file
 */
case class InvalidFileFormatThemeException(
  file: File,
  expectedFormat: String = ""
) extends ThemeException {
  /** Represents the file extension of the file .*/
  private def fileExt: String =
    if (file.isDirectory) "<directory>"
    else fileName.split(".").lastOption.getOrElse("")

  /** Rperesents the name of the file. */
  private def fileName: String = file.getPath

  override def toString: String = {
    if (expectedFormat.nonEmpty)
      s"$fileExt was found, but expected $expectedFormat ($fileName)"
    else
      s"$fileExt is not a supported theme extension ($fileName)"
  }
}

/**
 * Represents a failure due to unavailable or missing content of a theme.
 *
 * @param content Description of missing content
 */
case class MissingContentThemeException(
  content: String
) extends ThemeException {
  override def toString: String = s"Missing theme content: $content"
}

/**
 * Represents a failure related to accessing/loading a file (using Coursier)
 * that is tied to a theme.
 *
 * @param fileError The information about the error related to the file
 */
case class FileErrorThemeException(
  fileError: FileError
) extends ThemeException {
  override def toString: String = fileError.describe
}

/**
 * Represents a bundle (or collection) of exceptions related to a theme.
 *
 * @param exceptions The collection of related theme exceptions
 */
case class BundledThemeException(
  exceptions: Seq[ThemeException]
) extends ThemeException {
  override def toString: String = {
    s"Bundle of ${exceptions.size} theme exceptions\n" +
      exceptions.mkString("\n")
  }
}
