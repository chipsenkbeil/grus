package org.senkbeil.grus.layouts

import org.senkbeil.grus.structures.{MenuItem, Metadata}

/**
 * Represents the contextual information passed to all layouts.
 *
 * @param title If provided, will be Some title of the page, otherwise None
 * @param metadata All front matter associated with the current entity
 * @param mainMenuItems The menu items for the main menu
 * @param sideMenuItems The menu items for the side menu
 */
case class Context(
  title: Option[String] = None,
  metadata: Option[Metadata] = None,
  mainMenuItems: Seq[MenuItem] = Nil,
  sideMenuItems: Seq[MenuItem] = Nil
)
