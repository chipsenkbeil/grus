package org.senkbeil.sitegen.utils

import java.nio.file.{Files, Path}

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.{SuffixFileFilter, TrueFileFilter}

/**
 * Contains a collection of utility functions regarding
 * paths and files.
 */
object FileHelper {
  /**
   * Creates an empty file at the specified path.
   *
   * @param path The path to the file
   * @return The resulting path
   */
  def createEmptyFile(path: Path): Path = Files.createFile(path)

  /**
   * Retrieves all markdown files found in the specified
   * directories or any of their subdirectories.
   *
   * @param paths The paths to the directories to traverse
   * @return An iterable collection of paths to markdown files
   */
  def markdownFiles(paths: Path*): Iterable[Path] = {
    import scala.collection.JavaConverters._

    paths.flatMap(p => FileUtils.listFiles(
      p.toFile,
      new SuffixFileFilter(".md"),
      TrueFileFilter.INSTANCE
    ).asScala.map(_.toPath))
  }

  /**
   * Removes the file extension from the name.
   *
   * @param fileName The file name whose extension to remove
   * @return The file name without the extension
   */
  def stripExtension(fileName: String): String = {
    fileName.replaceFirst("[.][^.]+$", "")
  }
}
