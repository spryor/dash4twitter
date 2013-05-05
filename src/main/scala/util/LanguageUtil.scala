package dash4twitter.util

object English {

  val langDir = "/lang/eng/lexicon/"

	val stopwords = Resource.asSource(langDir + "stopwords.english.gz").getLines.toSet ++ Set("don")
	val acronyms = Resource.asSource(langDir + "acronyms.txt.gz").getLines.map(_.trim.toLowerCase).toSet
	val vulgarWords = Resource.asSource(langDir + "vulgar.txt.gz").getLines.toSet

	def removeWord(word: String) = acronyms(word) | stopwords(word) | vulgarWords(word)

}
