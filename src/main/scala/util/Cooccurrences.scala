package dash4twitter.util

object Cooccurrences {

  import collection.mutable.{HashMap, Set}

  //An index of words to all the words with which each word co-occurred
  private[this] lazy val invertedIndex = HashMap[String,Set[String]]()
  private[this] lazy val occurrences = HashMap[String,Double]()
  private[this] var totalOccurrences: Double = 0

  def update(word1: String, word2: String) = {
    if(word1 != word2) {
      val occurrence = Vector(word1, word2).sorted.mkString("+")
      occurrences(occurrence) = 
        if(occurrences.contains(occurrence)) {
          occurrences(occurrence) + 1
        } else {
          if(!invertedIndex.contains(word1)) invertedIndex(word1) = Set()
          if(!invertedIndex.contains(word2)) invertedIndex(word2) = Set()
          invertedIndex(word1) += word2
          invertedIndex(word2) += word1
          1
        }
        totalOccurrences += 1
    }
  }

  def candidates(queryTerm: String) = invertedIndex(queryTerm)

  def prob(word1: String, word2: String) = {
    val occurrence = Vector(word1, word2).sorted.mkString("+")
    val numerator = 
      if(occurrences.contains(occurrence) && occurrences(occurrence) > 2.0) occurrences(occurrence)
      else 0.0
    numerator/totalOccurrences
  }
}
