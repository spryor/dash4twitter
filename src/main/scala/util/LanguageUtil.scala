package dash4twitter.util

object English {

  private val englishDir = "/lang/eng/"
  private val lexiconDir = englishDir + "lexicon/"

  lazy val stopwords = Resource.asSource(lexiconDir + "stopwords.english.gz").getLines.toSet ++ Set("don")
  lazy val acronyms = Resource.asSource(lexiconDir + "acronyms.txt.gz").getLines.map(_.trim.toLowerCase).toSet
  lazy val vulgarWords = Resource.asSource(lexiconDir + "vulgar.txt.gz").getLines.toSet

  def removeWord(word: String) = acronyms(word) | stopwords(word) | vulgarWords(word)

}
