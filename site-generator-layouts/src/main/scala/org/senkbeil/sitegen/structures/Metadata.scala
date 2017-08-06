package org.senkbeil.sitegen.structures

/**
 * Represents metadata for a page.
 */
trait Metadata {
  /** @return The fully-qualified class name for the layout to use */
  def layout: String

  /** @return If true, indicates that the page is using the default layout */
  def usingDefaultLayout: Boolean

  /** @return A weight used for page ordering in menus and other structures */
  def weight: Double

  /** @return Whether or not to render the page */
  def render: Boolean

  /** @return If not None, represents the title to associate with the page */
  def title: Option[String]

  /**
   * @return If not None, represents an alternative link for the page used
   *         in menus and other renderings
   */
  def link: Option[String]

  /**
   * @return If not None, represents the url the page will
   *         use as the destination for redirection (ignoring any
   *         other settings such as layout); does nothing if
   *         render is false
   */
  def redirect: Option[String]

  /**
   * @return If true, indicates that the page is fake and should not be
   *         rendered or used anywhere
   */
  def fake: Boolean

  /**
   * @return All other metadata properties that were provided that
   *         do not match reserved properties
   */
  def other: Map[String, Seq[String]]
}

object Metadata {
  /**
   * Represents the property names reserved for use in the metadata.
   */
  lazy val reservedKeys: Seq[String] =
    classOf[Metadata].getDeclaredFields.map(_.getName).filterNot(_ == "other")
}
