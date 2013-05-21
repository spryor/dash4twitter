package dash4twitter.util

import dash4twitter.app.LexicalPolarityDetector

class TimeWindow {
  var occurrences = 1
  var created: Long = System.currentTimeMillis / 1000
}

class TokenStats {

  import scala.collection.mutable.Queue

  private[this] var count: Double = 1
  private[this] val past = Queue[TimeWindow]()
  private[this] val polarityDistribution = Array(0, 0, 0)
  def incr() { count += 1 }
  def count(): Double = count
  def updatePolarity(polarity: String) { polarityDistribution(LexicalPolarityDetector.toIndex(polarity)) += 1 }
  def getPolarityDistribution() = polarityDistribution.toVector
}

object Lexicon {

  import collection.mutable.HashMap

  private[this] lazy val lexicon = HashMap[String, TokenStats]()
  private[this] var count: Double = 0

  def apply(token: String) = lexicon(token)

  def update(token: String, polarity: String) {
    if(lexicon.contains(token)) lexicon(token).incr()
    else lexicon(token) = new TokenStats()
    lexicon(token).updatePolarity(polarity)
    count += 1
  }

  def prob(token: String) = lexicon(token).count/count

  def size() = lexicon.size

  def contains(token: String) = lexicon.contains(token)

}
