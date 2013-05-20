package dash4twitter.util

/*
 * The English object contains information related to the English
 * language.
 */
object English {

  private[this] lazy val englishDir = "/lang/eng/"
  private[this] lazy val lexiconDir = englishDir + "lexicon/"
  private[this] lazy val getWordLabel = """.*word1=([a-z]+).*priorpolarity=([a-z]+).*""".r

  lazy val Stopwords = Resource.asSource(lexiconDir + "stopwords.english.gz").getLines.toSet ++ Set("don", "rt")
  lazy val Acronyms = Resource.asSource(lexiconDir + "acronyms.txt.gz").getLines.map(_.trim.toLowerCase).toSet
  lazy val VulgarWords = Resource.asSource(lexiconDir + "vulgar.txt.gz").getLines.toSet
  lazy val LexicalPolarity = getPolarityLexicon()

  /*
   * The removeWord funciton takes a word and returns true if it is
   * either a stopword, an acronym, or a vulgar word.
   *
   * @param word is the unigram to be tested for removeWord membership
   */
  def removeWord(word: String) = Acronyms(word) | Stopwords(word) | VulgarWords(word)

  def getPolarityLexicon() = {
    val mpqa = Resource.asSource("/lang/eng/lexicon/polarityLexicon.gz")
    .getLines
    .map{case getWordLabel(word, label) =>
      val numLabel = label match {
        case "negative" => -1
        case "positive" => 1
        case _ => 0
      }
      (word, numLabel)
    }
    .toMap
    .withDefaultValue(0)

    val posWords = getLexicon("positive-words.txt.gz").map(word => (word, 1)).toMap
    val negWords = getLexicon("negative-words.txt.gz").map(word => (word, -1)).toMap
    mpqa ++ posWords ++ negWords
  }

  def getLexicon(filename: String, checkForComments: Boolean = true) =
    Resource.asSource("/lang/eng/lexicon/"+filename)
      .getLines
      .filterNot(!checkForComments && _.startsWith(";")) // filter out comments
      .toSet

}
