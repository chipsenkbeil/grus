package org.senkbeil.grus.layouts

import scalatags.Text.all._
import scalatags.stylesheet._

object Implicits {
  implicit class StyleSheetWrapper(private val styleSheet: StyleSheet) {
    /**
     * Transforms the stylesheet into a <style> tag.
     *
     * @return The style tag representing this stylesheet
     */
    def toStyleTag: Modifier = tag("style")(raw(styleSheet.styleSheetText))
  }

  implicit class StringWrapper(private val string: String) {
    /**
     * Transforms the string into into a <style> tag.
     *
     * @return The style tag containing the raw text
     */
    def toStyleTag: Modifier = tag("style")(raw(string))
  }
}
