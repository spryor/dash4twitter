package dash4twitter.util

/*
 * The PMI class tracks the co-occurrence of strings. This class 
 * is used to collect data and extract keywords from the data
 * using pointwise mutual information.
 */
class PMI {

  import java.io._
  import java.util.zip._
  import collection.mutable.{HashMap, Set}
  import math.log 

  //An index of words to all the words with which each word co-occurred
  private[this] val invertedIndex = HashMap[String,Set[String]]()
  //The counts for computing the joint probability
  private[this] val joint = HashMap[String,Double]()
  //The unigram counts
  private[this] val unigrams = HashMap[String,Double]()
  private[this] var totalUnigrams = 0.0
  private[this] var totalJoint = 0.0

  def save(filename: String) {

    val lexicon = HashMap[String, Int]()
    var i = 0

    val output = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(filename)))
    output.writeDouble(totalUnigrams)
    output.writeDouble(totalJoint)
    //write unigram data
    output.writeInt(unigrams.size)
    unigrams.foreach{case (unigram, cnt) => {
      lexicon(unigram) = i
      output.writeUTF(unigram)
      output.writeInt(i)
      output.writeDouble(cnt)
      i += 1
    }}
    //write joint
    output.writeInt(joint.size)
    joint.foreach{case (pair, cnt) => {
      val Array(word1, word2) = pair.split("\\+")
      output.writeInt(lexicon(word1))
      output.writeInt(lexicon(word2))
      output.writeDouble(cnt)
    }}
    output.close
  }

  def load(input: DataInputStream) = {
    val lexicon = HashMap[Int, String]()  
 
    totalUnigrams = input.readDouble
    totalJoint = input.readDouble

    val numUnigrams = input.readInt
  
    (1 to numUnigrams).foreach(_ => {
      val unigram = input.readUTF
      val unigramCode = input.readInt
      val cnt = input.readDouble
      unigrams(unigram) = cnt
      lexicon(unigramCode) = unigram
    })

    val numJoint = input.readInt
    (1 to numJoint).foreach(_ => {
      val word1 = lexicon(input.readInt)
      val word2 = lexicon(input.readInt)
      val cnt = input.readDouble
      joint(word1+"+"+word2) = cnt
      if(!invertedIndex.contains(word1)) invertedIndex(word1) = Set()
      if(!invertedIndex.contains(word2)) invertedIndex(word2) = Set()
      invertedIndex(word1) += word2
      invertedIndex(word2) += word1
    })
  }

  /*
   * The update function takes a vector of tokens and updates
   * the counts
   * 
   * @param tweet is a vector of all the unique unigrams in a tweet
   */
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

  /*
   * The getKeywords function returns the keywords associated with
   * a given unigram.
   *
   * @param term is the unigram about which to find related words
   */
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

  /*
   * Checks if the provided string is in the lexicon
   *
   * @param term is the unigram looked for in the lexicon
   */
  def contains(term: String) = unigrams.contains(term)

}

