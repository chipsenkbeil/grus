package org.senkbeil.sitegen.layouts

import org.senkbeil.sitegen.structures.MenuItem

/**
 * Represents the contextual information passed to all layouts.
 *
 * @param title If provided, will be Some title of the page, otherwise None
 * @param mainMenuItems The menu items for the main menu
 * @param sideMenuItems The menu items for the side menu
 */
case class Context(
  title: Option[String] = None,
  mainMenuItems: Seq[MenuItem] = Nil,
  sideMenuItems: Seq[MenuItem] = Nil
)
