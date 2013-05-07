package dash4twitter.util

/*
 * The Resource object is taken from tshrdlu and is 
 * used to read files from the resources directory
 * and read .gz files.
 */
object Resource {

  import java.util.zip.GZIPInputStream
  import java.io.DataInputStream

  def asSource(location: String) = {
    val stream = this.getClass.getResourceAsStream(location)
    if (location.endsWith(".gz"))
      io.Source.fromInputStream(new GZIPInputStream(stream))
    else
      io.Source.fromInputStream(stream)
  }

  def asStream(location: String) = {
    val stream = this.getClass.getResourceAsStream(location)
    val stream2 = if (location.endsWith(".gz")) new GZIPInputStream(stream) else stream
    new DataInputStream(stream2)
  }
}
