package org.senkbeil.sitegen.layouts

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date

import org.senkbeil.sitegen.BuildInfo

import scalatags.Text.all._

/**
 * Represents the layout for a common site page.
 */
class Page extends Layout {
  private lazy val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  private lazy val newHeadContent = (content: Seq[Modifier], date: Date) =>
    preHeadContent(context) ++
    Seq(
      meta(charset := "utf-8"),
      meta(name := "generator", attr("content") := BuildInfo.name + " " + BuildInfo.version),
      meta(name := "generation-date", attr("content") := dateFormat.format(date))
    ) ++ content ++
    postHeadContent(context) ++
    context.title.map(t => tag("title")(t))

  private lazy val newBodyContent = (content: Seq[Modifier]) =>
    preBodyContent(context) ++
    content ++
    postBodyContent(context)

  /**
   * Renders a generic page.
   *
    * @param bodyContent The content to render as HTML body using this layout
    * @param headContent The content to render as HTML head using this layout
   * @return The rendered content
   */
  override def render(
    bodyContent: Seq[Modifier],
    headContent: Seq[Modifier]
  ): Modifier = {
    html(htmlModifiers(context): _*)(
      head(newHeadContent(headContent, Date.from(Instant.now())): _*),
      body(bodyModifiers(context): _*)(newBodyContent(bodyContent): _*)
    )
  }

  /**
   * Returns content to be added at the beginning of the <head> tag.
   *
   * @param context The context of the layout to use when generating the content
   * @return The content to be added
   */
  protected def preHeadContent(context: Context): Seq[Modifier] = Nil

  /**
   * Returns content to be added at the end of the <head> tag.
   *
   * @param context The context of the layout to use when generating the content
   * @return The content to be added
   */
  protected def postHeadContent(context: Context): Seq[Modifier] = Nil

  /**
   * Returns content to be added at the beginning of the <body> tag.
   *
   * @param context The context of the layout to use when generating the content
   * @return The content to be added
   */
  protected def preBodyContent(context: Context): Seq[Modifier] = Nil

  /**
   * Returns content to be added at the end of the <body> tag.
   *
   * @param context The context of the layout to use when generating the content
   * @return The content to be added
   */
  protected def postBodyContent(context: Context): Seq[Modifier] = Nil

  /**
   * Returns modifiers to be applied on the <html> tag.
   *
   * @param context The context of the layout to use when generating
   *                the modifiers
   * @return The modifiers to be added
   */
  protected def htmlModifiers(context: Context): Seq[Modifier] = Nil

  /**
   * Returns modifiers to be applied on the <body> tag.
   *
   * @param context The context of the layout to use when generating
   *                the modifiers
   * @return The modifiers to be added
   */
  protected def bodyModifiers(context: Context): Seq[Modifier] = Nil
}
