object Alphabet {
  
  import collection.mutable.HashMap

  private[this] val alphabet = HashMap[String,Int]()
  private[this] val inverted_alphabet = HashMap[Int,String]()
  private[this] var nextIndex = 1

  def apply(word: String) = alphabet(word)
  def apply(word: Int) = inverted_alphabet(word) 
  def contains(word: String) = alphabet.contains(word)
  def add(word: String) = {
    if(!alphabet.contains(word)) {
      alphabet(word) = nextIndex
      inverted_alphabet(nextIndex) = word
      nextIndex += 1
    }
    alphabet(word)
  }
}

object OccurrenceGraph {
  
  import collection.mutable.HashMap

  private[this] val graph = HashMap[Int,HashMap[Int, Double]]()

  def updateGraph(tweet: Vector[String]) {
    var i, j = 0;
    while(i < tweet.length){
      val word = Alphabet.add(tweet(i))
      if(!graph.contains(word)) graph(word) = HashMap[Int, Double]()
      j = 0;
      while(j < tweet.length) {
        val cWord = Alphabet.add(tweet(j))
        if(word != cWord) addWord(word, cWord)
        j += 1
      }
      i += 1
    }
  }

  def getKeywords(term: String) = {
    graph(Alphabet(term)).toVector
      .map{case (w, cnt) => (Alphabet(w), cnt)}
      .sortBy(_._2)
      .reverse
      .unzip
  }

  def contains(word: String) = Alphabet.contains(word) && graph.contains(Alphabet(word))

  private[this] def addWord(word: Int, coWord: Int) {
    if(!graph(word).contains(coWord)) graph(word)(coWord) = 1.0
    else graph(word)(coWord) += 1.0
  }

}

val stopwords = io.Source.fromFile("stopwords.english").getLines.toSet
val acronyms = io.Source.fromFile("acronyms.txt").getLines.map(_.trim.toLowerCase).toSet
val vulgarWords = io.Source.fromFile("vulgar.txt").getLines.toSet
val removeWords = acronyms ++ stopwords ++ vulgarWords

def tokenize(sentence: String) = sentence
  .replaceAll("""<(mention|link|hashtag)>|rt\s|\b[0-9]+\b|(ha)+|\b(lo)+l?\b|\b(aj)+j?\b""", " ")
  .replaceAll("[^a-zA-Z0-9\\s]", " ")
  .trim
  .split("\\s+")
  .toVector
  .filter(_.length > 2)
  .filterNot(removeWords)

val filename = "trainingTweets.txt"

print("Counting...")
io.Source.fromFile(filename)("UTF-8")
  .getLines
  .foreach(line => OccurrenceGraph.updateGraph(tokenize(line)))
println("Complete!")

val searchWords = args

searchWords.foreach(term => {
  println("\nSearching for: "+term)
  if(OccurrenceGraph.contains(term)) {
    val (keywords, _) = OccurrenceGraph.getKeywords(term)
    println("\t"+keywords.mkString(" "))
  } else { 
    println("\tNo data") 
  } 
})
