package org.senkbeil.grus.structures

import scala.annotation.tailrec

/**
 * Represents a generic menu item.
 */
trait MenuItem {
  /** @return The name of the menu item */
  def name: String

  /**
   * @return The link for the menu item, or None if the menu item does not
   *         link to any content
   */
  def link: Option[String]

  /** @return The children of the menu item */
  def children: Seq[MenuItem]

  /** @return Whether or not the menu item is selected */
  def selected: Boolean

  /**
   * @return The weight associated with the menu item, used for ordering
   *         where smaller weights should appear first (left/top) and
   *         larger weights should appear last (right/bottom)
   */
  def weight: Double

  /**
   * @return If marked as fake, indicates that the menu item should not be
   *         used and is around for other purposes
   */
  def fake: Boolean

  /**
   * @return The page associated with the menu item, which will be the fake
   *         child page if representing a directory or the actual page if
   *         representing a normal page
   */
  def page: Option[Page]

  /**
   * Indicates whether or not the menu item represents the specified page.
   *
   * @param page The page to compare to the menu item
   * @return True if the menu item represents the page, otherwise false
   */
  def representsPage(page: Page): Boolean = this.page.exists(_ == page)

  /**
   * Indicates whether or not this menu item or any of its children (or one of
   * its their children, recursive) is selected.
   *
   * @return True if this menu item or any child of this menu item or any one
   *         of their children (recursive) is selected, otherwise false
   */
  def isDirectlyOrIndirectlySelected: Boolean = {
    @tailrec def checkIsSelected(menuItems: MenuItem*): Boolean = {
      if (menuItems.isEmpty) false
      else if (menuItems.exists(_.selected)) true
      else checkIsSelected(menuItems.flatMap(_.children): _*)
    }

    checkIsSelected(this)
  }
}

object MenuItem {
  /** Represents the default weight of menu items. */
  val DefaultWeight: Double = 0
}
