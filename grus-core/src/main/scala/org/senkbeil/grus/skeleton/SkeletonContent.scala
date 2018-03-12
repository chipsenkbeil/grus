package org.senkbeil.grus.skeleton

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file._

import org.senkbeil.grus.Logger
import org.senkbeil.grus.skeleton.SkeletonContent.DeleteAllFilesVisitor

object SkeletonContent {
  /**
   * Internal visitor used to delete all files and directories in a path.
   * @tparam T The type of content to visit inheriting from Path
   * @param logger The logger to use when performing operations
   */
  private class DeleteAllFilesVisitor[T <: Path](
    private val logger: Logger
  ) extends SimpleFileVisitor[T] {
    override def postVisitDirectory(
      dir: T,
      exc: IOException
    ): FileVisitResult = {
      val result = super.postVisitDirectory(dir, exc)
      logger.trace(s"Deleting $dir")
      Files.delete(dir)
      result
    }

    override def visitFile(
      file: T,
      attrs: BasicFileAttributes
    ): FileVisitResult = {
      val result = super.visitFile(file, attrs)
      logger.trace(s"Deleting $file")
      Files.delete(file)
      result
    }
  }

  object Implicits {
    import scala.language.implicitConversions
    private[skeleton] implicit def implicitStringToSkeletonContent(
      text: String
    ): Array[Byte] = text.trim.getBytes("UTF-8")
  }
}

/**
 * Represents a content generator for skeleton files.
 */
trait SkeletonContent {
  private lazy val logger = new Logger(this.getClass)

  /**
   * Transforms the content into a mapping of relative paths to the
   * corresponding data.
   *
   * @return Map of NIO paths to byte arrays of content
   */
  def toMap: Map[Path, Array[Byte]]

  /**
   * Transforms the content into a mapping of paths to corresponding data using
   * the specified path as the root of all paths.
   *
   * @param root The path to use as the root of all content paths
   * @return Map of NIO paths to byte arrays of content
   */
  def resolveToMap(root: Path): Map[Path, Array[Byte]] = toMap.map {
    case (p, b) => (root.resolve(p), b)
  }

  /**
   * Writes the skeleton content to the specified root path.
   * @param root The path serving as the root of all content
   * @param clearPath If true, removes all content within the path before
   *                  writing the new content
   */
  def writeToPath(root: Path, clearPath: Boolean = true): Unit = {
    if (clearPath) {
      if (Files.isDirectory(root))
        Files.walkFileTree(root, new DeleteAllFilesVisitor(logger))

      Files.deleteIfExists(root)
    }

    resolveToMap(root).foreach {
      case (p, b) =>
        val parent = p.getParent
        if (parent != null && Files.notExists(parent)) {
          logger.verbose(s"Creating directory $parent")
          Files.createDirectories(parent)
        }

        logger.verbose(s"Creating $p")
        Files.write(p, b)
    }
  }
}
