package org.senkbeil.sitegen.structures

import java.nio.file.Path

import org.senkbeil.sitegen.layouts.Context

/** Represents a page of content. */
trait Page {
  /** @return The path to the raw page content */
  def path: Path

  /** @return The metadata for the page */
  def metadata: Metadata

  /** @return Whether or not this page represents a directory */
  def isDirectory: Boolean

  /** @return An absolute web path link to this file, ignoring any overrides */
  def absoluteLink: String

  /**
   * @return The title of the page, which either comes from the
   *         metadata of the page or the name of the file the page is
   *         associated with
   */
  def title: String

  /** @return The name of the page, which is based on the file name */
  def name: String

  /** @return The output path when the page is rendered */
  def outputPath: Path

  /** @return Whether or not this page represents an index page */
  def isIndexPage: Boolean

  /**
   * @return Whether or not this page is at the root of the website.
   *
   * E.g. /example.html is at the root while /my/example.html is not.
   */
  def isAtRoot: Boolean

  /**
   * Renders the page and writes it to the output path.
   *
   * @param context The context to feed into this page's layout
   * @param path The path to render the file
   * @return True if successfully rendered the page, otherwise false
   */
  def render(context: Context, path: Path = outputPath): Boolean

  /**
   * Returns whether or not the page is using the default layout.
   *
   * @return True if using the default layout, otherwise false
   */
  def isUsingDefaultLayout: Boolean = metadata.usingDefaultLayout

  /**
   * Compares this page with another page. Two pages are equal if they have
   * the same path.
   *
   * @param obj The other page to compare
   * @return True if the two pages have the same path, otherwise false
   */
  override def equals(obj: Any): Boolean = obj match {
    case p: Page  => p.path == this.path
    case _        => false
  }
}

object Page {
  /**
   * Represents a redirection page.
   */
  object Redirect {
    import scalatags.Text.all._

    /**
     * Generates a redirect page using the provided url.
     *
     * @param url The url that the page should redirect to
     * @return The page content
     */
    def apply(url: String): Modifier = {
      val q = "\""
      html(lang := "en-US")(
        head(
          meta(charset := "UTF-8"),
          meta(httpEquiv := "refresh", content := s"1; url=$url"),
          script(`type` := "text/javascript")(
            s"window.location.href = $q$url$q;"
          ),
          tag("title")("Page Redirection")
        ),
        body(
          raw("If you are not redirected automatically, "),
          a(href := url)("follow this link"),
          raw(".")
        )
      )
    }
  }
}
