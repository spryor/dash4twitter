package dash4twitter.util

class TokenStats {

  private[this] var count: Double = 1
  //TODO add temporal data for sentiment test
  
  def incr() { count += 1 }
  def count(): Double = count

}

object Lexicon {

  import collection.mutable.HashMap

  private[this] lazy val lexicon = HashMap[String, TokenStats]()
  private[this] var count: Double = 0

  def apply(token: String) = lexicon(token)

  def update(token: String) {
    if(lexicon.contains(token)) lexicon(token).incr()
    else lexicon(token) = new TokenStats()
    count += 1
  }

  def prob(token: String) = lexicon(token).count/count

  def size() = lexicon.size

  def contains(token: String) = lexicon.contains(token)

}
