package org.senkbeil.sitegen.layouts

import scalatags.Text.all._

object Layout {
  /**
   * Checks if the specified class is a layout.
   *
   * @param klass The class to check
   * @return True if a layout, otherwise false
   */
  def classIsLayout(klass: Class[_]): Boolean = {
    classOf[Layout].isAssignableFrom(klass)
  }
}

/**
 * Represents the base interface that a layout must implement.
 */
trait Layout {
  private var _context: Context = _

  /**
   * Sets the layout's context. One-time use only.
   *
   * @param context The layout's context
   * @throws AssertionError If the context has already been set
   */
  @throws[AssertionError]
  def context_=(context: Context): Unit = {
    assert(_context == null, "Context has already been set!")
    _context = context
  }

  /**
   * Represents the context provided to the layout.
   *
   * @return The layout's context
   * @throws AssertionError If the context has not been set
   */
  @throws[AssertionError]
  def context: Context = {
    assert(_context != null, "Context has not been set!")
    _context
  }

  /**
    * Renders the provided content as HTML using this layout.
    *
    * @param bodyContent The content to render as HTML body using this layout
    * @param headContent The content to render as HTML head using this layout
    * @return The rendered content
    */
  def render(bodyContent: Seq[Modifier], headContent: Seq[Modifier]): Modifier

  /**
   * Renders the provided content as HTML using this layout.
   *
   * @param content The content to render as HTML body using this layout
   * @return The rendered content
   */
  def render(content: Seq[Modifier]): Modifier = render(
    bodyContent = content,
    headContent = Nil
  )

  /**
   * Renders the the layout with no content.
    *
   * @return The rendered layout
   */
  def render(): Modifier = render(content = Nil)

  /**
   * Renders the provided text as HTML using this layout.
   *
   * @param text The text to render as HTML using this layout
   * @return The rendered content
   */
  def render(text: String): Modifier = render(content = Seq(raw(text)))

  /**
   * Renders the layout with text and returns the string representation.
   *
   * @param text The text to render as HTML using this layout
   * @return The string representation of the layout
   */
  def toString(text: String): String = render(text).toString

  /**
   * Renders the layout with content and returns the string representation.
   *
   * @param content The content to fill in the layout
   * @return The string representation of the layout
   */
  def toString(content: Seq[Modifier]): String = render(content).toString

  /**
   * Renders the layout with no content and returns the string representation.
   *
   * @return The string representation of the layout
   */
  override def toString: String = toString(Nil)
}
