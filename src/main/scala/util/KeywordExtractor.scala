package dash4twitter.util

/*
 * The PMI class tracks the co-occurrence of strings. This class 
 * is used to collect data and extract keywords from the data
 * using pointwise mutual information.
 */
object PMI {

  import java.io._
  import java.util.zip._
  import collection.mutable.{HashMap, Set}
  import math.log 

  //An index of words to all the words with which each word co-occurred
  private[this] lazy val invertedIndex = HashMap[Int,Set[Int]]()
  //The counts for computing the joint probability
  private[this] lazy val joint = HashMap[Vector[Int],Double]()
  //The unigram counts
  private[this] var totalJoint = 0.0

  /*
   * The update function takes a vector of tokens and updates
   * the counts
   * 
   * @param tweet is a vector of all the unique unigrams in a tweet
   */
  def update(tokens: Vector[Int]) {
    var i, j = 0;
    while(i < tokens.length){
      val word = tokens(i)
      j = i;
      while(j < tokens.length) {
        val co_occurrence = tokens(j)
        if(word != co_occurrence) {
          val combined = Vector(word, co_occurrence).sorted
          joint(combined) = 
            if(joint.contains(combined)) {
              joint(combined) + 1.0
            } else {
              if(!invertedIndex.contains(word)) invertedIndex(word) = Set()
              if(!invertedIndex.contains(co_occurrence)) invertedIndex(co_occurrence) = Set()
              invertedIndex(word) += co_occurrence
              invertedIndex(co_occurrence) += word
              1.0
            }
          totalJoint += 1
        }
        j += 1
      }
      i += 1
    }
  }

  def getKeywords(queryToken: String): (Vector[String], Vector[Double]) = getKeywords(Code(queryToken))

  /*
   * The getKeywords function returns the keywords associated with
   * a given unigram.
   *
   * @param queryToken is the unigram about which to find related words
   */
  def getKeywords(queryToken: Int): (Vector[String], Vector[Double]) = {
    invertedIndex(queryToken)
      .toVector
      .map(word => {
        val combined = Vector(queryToken, word).sorted
        val p_near = if(joint.contains(combined) && joint(combined) > 2.0) joint(combined)
                     else 0.0
        (Code(word), -1*log((p_near/totalJoint)/(Lexicon(word).count/Lexicon.count)))
      })
      .sortBy(_._2)
      .filter(_._2  < 8)
      .unzip
  }

}

