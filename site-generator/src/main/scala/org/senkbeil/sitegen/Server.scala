package org.senkbeil.sitegen

import java.nio.file.{Files, Paths}

import unfiltered.request.{GET, Mime, Path => UFPath}
import unfiltered.response._

import scala.util.Try

/**
 * Represents a file server.
 *
 * @param config The configuration to use when serving files
 */
class Server(private val config: Config) {
  /** Logger for this class. */
  private lazy val logger = new Logger(this.getClass)

  /**
   * Runs the server.
   */
  def run(): Unit = {
    val outputDir = config.outputDir()
    val indexFiles = config.indexFiles()
    val hostedContent = unfiltered.filter.Planify {
      case GET(UFPath(path)) =>
        val rawPath = Paths.get(outputDir, path)
        val indexPaths = indexFiles.map(f => Paths.get(outputDir, path, f))
        val fileBytes = (rawPath +: indexPaths).filter(p =>
          Try(Files.exists(p)).getOrElse(false)
        ).map(p =>
          (p, Try(Files.readAllBytes(p)))
        ).filter(_._2.isSuccess).map(t => (t._1, t._2.get)).headOption
        fileBytes match {
          case Some(result) =>
            val (filePath, fileBytes) = result
            val fileName = filePath.getFileName.toString

            /** Logs response including status code. */
            val logResponse = (code: Int) =>
              logger.verbose(s"Status $code :: GET $path")

            try {
              val Mime(mimeType) = fileName

              logResponse(Ok.code)
              ContentType(mimeType) ~>
                ResponseBytes(fileBytes)
            } catch {
              case _: MatchError if config.allowUnsupportedMediaTypes() =>
                logResponse(Ok.code)
                ResponseBytes(fileBytes)
              case _: MatchError =>
                logResponse(UnsupportedMediaType.code)
                UnsupportedMediaType ~> ResponseString(fileName)
              case t: Throwable =>
                logResponse(InternalServerError.code)
                InternalServerError ~> ResponseString(t.toString)
            }
          case None => NotFound ~> ResponseString(s"Unknown page: $path")
        }
      case _ => MethodNotAllowed ~> ResponseString("Unknown request")
    }
    unfiltered.jetty.Server.http(config.port()).plan(hostedContent).run()
  }
}
