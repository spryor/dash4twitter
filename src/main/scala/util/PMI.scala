package dash4twitter.util

class PMI {

  import collection.mutable.{HashMap, Set}
  import math.log 

  private[this] val invertedIndex = HashMap[String,Set[String]]()
  private[this] val joint = HashMap[String,Double]()
  private[this] val unigrams = HashMap[String,Double]()
  private[this] var totalUnigrams = 0.0
  private[this] var totalJoint = 0.0

  def update(tweet: Vector[String]) {
    var i, j = 0;
    while(i < tweet.length){
      val word = tweet(i)
      unigrams(word) = 
        if(unigrams.contains(word)) unigrams(word) + 1.0
        else 1.0
      totalUnigrams += 1.0
      j = i;
      while(j < tweet.length) {
        val cWord = tweet(j)
        if(word != cWord) {
          val combined = Vector(word, cWord).sorted.mkString("+")
          joint(combined) = 
            if(joint.contains(combined)) {
              joint(combined) + 1.0
            } else {
              if(!invertedIndex.contains(word)) invertedIndex(word) = Set()
              if(!invertedIndex.contains(cWord)) invertedIndex(cWord) = Set()
              invertedIndex(word) += cWord
              invertedIndex(cWord) += word
              1.0
            }
          totalJoint += 1
        }
        j += 1
      }
      i += 1
    }
  }

  def getKeywords(term: String) = {
    invertedIndex(term).toVector
    .map(word => {
      val combined = Vector(term, word).sorted.mkString("+")
      val p_near = if(joint.contains(combined) && joint(combined) > 2.0) joint(combined)
                   else 0.0
      (word, -1*log((p_near/totalJoint)/(unigrams(word)/totalUnigrams)))
    })
    .sortBy(_._2)
    .filter(_._2  < 8)
    .unzip
  }

  def contains(term: String) = unigrams.contains(term)

}

