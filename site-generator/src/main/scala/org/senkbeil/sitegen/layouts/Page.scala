package org.senkbeil.sitegen.layouts

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date

import org.senkbeil.sitegen.BuildInfo

import scalatags.Text.all._

/**
 * Represents the layout for a common site page.
 *
 * @param preHeadContent Content to be added at the beginning of the <head>
 * @param postHeadContent Content to be added at the end of the <head>
 * @param preBodyContent Content to be added at the beginning of the <body>
 * @param postBodyContent Content to be added at the end of the <body>
 * @param htmlModifiers Modifiers to apply on the <html> tag
 * @param bodyModifiers Modifiers to apply on the <body> tag
 */
abstract class Page(
  val preHeadContent: Seq[Modifier] = Nil,
  val postHeadContent: Seq[Modifier] = Nil,
  val preBodyContent: Seq[Modifier] = Nil,
  val postBodyContent: Seq[Modifier] = Nil,
  val htmlModifiers: Seq[Modifier] = Nil,
  val bodyModifiers: Seq[Modifier] = Nil
) extends Layout {
  private lazy val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  private lazy val newHeadContent = (content: Seq[Modifier], date: Date) =>
    preHeadContent ++
    Seq(
      meta(charset := "utf-8"),
      meta(name := "generator", attr("content") := BuildInfo.name + " " + BuildInfo.version),
      meta(name := "generation-date", attr("content") := dateFormat.format(date))
    ) ++ content ++
    postHeadContent ++
    context.title.map(t => tag("title")(t))

  private lazy val newBodyContent = (content: Seq[Modifier]) =>
    preBodyContent ++
    content ++
    postBodyContent

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
    html(htmlModifiers: _*)(
      head(newHeadContent(headContent, Date.from(Instant.now())): _*),
      body(bodyModifiers: _*)(newBodyContent(bodyContent): _*)
    )
  }
}
