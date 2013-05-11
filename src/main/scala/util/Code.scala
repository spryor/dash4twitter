package dash4twitter.util

object Code {

  import collection.mutable.HashMap

  private[this] lazy val codes = HashMap[String,Int]()
  private[this] lazy val inverted_codes = HashMap[Int,String]()
  private[this] var nextCode = 1

  def apply(word: String) = encode(word)
  def apply(code: Int) = inverted_codes(code)
  def contains(word: String) = codes.contains(word)
  def contains(code: Int) = inverted_codes.contains(code)
  def encode(word: String) = {
    if(!codes.contains(word)) {
      codes(word) = nextCode
      inverted_codes(nextCode) = word
      nextCode += 1
    }
    codes(word)
  }
}
