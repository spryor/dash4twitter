package dash4twitter.util

class TimeWindow {
  var occurrences = 1
  var created: Long = System.currentTimeMillis / 1000
}

class TokenStats {

  import scala.collection.mutable.Queue

  private[this] var count: Double = 1
  private[this] val past = Queue[TimeWindow]()
  
  def incr() { 
    count += 1
    //val currentTime = (System.currentTimeMillis / 1000)    
    //if(past.isEmpty | (currentTime - past.last.created) > 60) {
    //  past.enqueue(new TimeWindow)
    //  if(past.size > 20) past.dequeue
    //} else {
    //  past.last.occurrences += 1 
    //}
  }
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
