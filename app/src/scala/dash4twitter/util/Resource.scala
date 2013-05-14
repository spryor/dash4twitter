package dash4twitter.util

/*
 * The Resource object is taken from tshrdlu and is 
 * used to read files from the resources directory
 * and read .gz files.
 */
object Resource {

  import java.util.zip.GZIPInputStream
  import java.io.DataInputStream
  import play.api._
  import java.io._
  import play.api.Play.current

  def getFilePath(location: String) = Play.getFile("conf/resources"+location).getAbsolutePath()

  private[this] def getResourceAsStream(location: String) = 
    new FileInputStream(new File(getFilePath(location)))

  def asSource(location: String) = {
    val stream = getResourceAsStream(location)
    if (location.endsWith(".gz"))
      io.Source.fromInputStream(new GZIPInputStream(stream))
    else
      io.Source.fromInputStream(stream)
  }

  def asStream(location: String) = {
    val stream = getResourceAsStream(location)
    val stream2 = if (location.endsWith(".gz")) new GZIPInputStream(stream) else stream
    new DataInputStream(stream2)
  }
}
