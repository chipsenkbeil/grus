package org.senkbeil.sitegen.utils

import org.apache.commons.codec.binary.Base64

/**
 * Loads images from resources.
 */
object ResourceImageLoader {
  private lazy val cl = getClass.getClassLoader

  /**
    * Retrieves the image as a text representation. This is only useful for
    * SVGs, which are naturally XML documents.
    * @param name The name of the image
    * @param rootPath The root path in resources containing the image
    * @return The string representing the image
    */
  def imageText(name: String, rootPath: String = ""): String = {
    val byteArray = imageBytes(name, rootPath)
    new String(byteArray)
  }

  /**
   * Retrieves an image as a byte array.
   * @param name The name of the image
   * @param rootPath The root path in resources containing the image
   * @return The array of bytes representing the image
   */
  def imageBytes(name: String, rootPath: String = ""): Array[Byte] = {
    val inputStream = cl.getResourceAsStream(rootPath + name)
    Stream.continually(inputStream.read)
      .takeWhile(_ >= 0)
      .map(_.toByte).toArray
  }

  /**
   * Retrieves an image as a base64 encoded string.
   * @param name The name of the image
   * @param rootPath The root path in resources containing the image
   * @return The base64 encoded string representing the image
   */
  def imageBase64(name: String, rootPath: String = ""): String = {
    val byteArray = imageBytes(name, rootPath)
    Base64.encodeBase64String(byteArray)
  }
}
