package dash4twitter.util

import dash4twitter.app.FeatureExtractor

/*
 * The Model class tracks the co-occurrence of strings. This class 
 * is used to collect data and extract keywords from the data
 * using pointwise mutual information.
 */
object Model {

  import math.log

  private[this] def clean(sentence: Vector[String]) = sentence
    .filter(w => w.length > 2 && !English.removeWord(w) && !(w matches "[0-9]+"))

  /*
   * The update function takes a vector of tokens and updates
   * the counts
   * 
   * @param tweet is a vector of all the unique unigrams in a tweet
   */
  def update(features: FeatureExtractor) {
    var tokens = clean(features.distinct)
    var i, j = 0;
    while(i < tokens.length) {
      Lexicon.update(tokens(i), features.polarity)
      j = i+1;
      while(j < tokens.length) {
        Cooccurrences.update(tokens(i), tokens(j))
        j += 1
      }
      i += 1
    }
  }

  /*
   * The getKeywords function returns the keywords associated with
   * a given unigram.
   *
   * @param queryToken is the unigram about which to find related words
   */
  def getKeywords(query: String) = {
    Cooccurrences(query)
      .toVector
      .map(PMI(query, _))
      .sortBy(_._2)
      .filter(_._2  < 8)
      .unzip
  }

  /*
   * The PMI function computes the pointwise mutual information
   * between the query term and the token
   */
  private[this] def PMI(query: String, token: String) = (token, -1*log(Cooccurrences.prob(query, token)/Lexicon.prob(token)))

}

