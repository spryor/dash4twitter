package dash4twitter.util

/*
 * The English object contains information related to the English
 * language.
 */
object English {

  private val englishDir = "/lang/eng/"
  private val lexiconDir = englishDir + "lexicon/"

  lazy val stopwords = Resource.asSource(lexiconDir + "stopwords.english.gz").getLines.toSet ++ Set("don")
  lazy val acronyms = Resource.asSource(lexiconDir + "acronyms.txt.gz").getLines.map(_.trim.toLowerCase).toSet
  lazy val vulgarWords = Resource.asSource(lexiconDir + "vulgar.txt.gz").getLines.toSet

  /*
   * The removeWord funciton takes a word and returns true if it is
   * either a stopword, an acronym, or a vulgar word.
   *
   * @param word is the unigram to be tested for removeWord membership
   */
  def removeWord(word: String) = acronyms(word) | stopwords(word) | vulgarWords(word)

}
