package dash4twitter.util

class TokenStats {

  private[this] var count: Double = 1
  //TODO add temporal data for sentiment test
  
  def incr() { count += 1 }
  def count(): Double = count
}

object Lexicon {

  import collection.mutable.HashMap

  private[this] lazy val lexicon = HashMap[Int, TokenStats]()
  private[this] var count: Double = 0

  def apply(token: Int) = lexicon(token)
  def apply(token: String) = lexicon(Code(token))

  def update_and_encode(tokens: Vector[String]) = tokens.map(add) 

  def add(token: String) = {
    val code = Code(token)
    if(lexicon.contains(code)) lexicon(code).incr()
    else lexicon(code) = new TokenStats()
    count += 1
    code
  }

  def count(): Double = count

  def contains(token: String) = Code.contains(token)
  def contains(token: Int) = Code.contains(token)

}
