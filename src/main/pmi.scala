object Resource {
  import java.util.zip.GZIPInputStream
  import java.io.DataInputStream

  def asSource(location: String) = {
    val stream = this.getClass.getResourceAsStream(location)
    if (location.endsWith(".gz"))
      io.Source.fromInputStream(new GZIPInputStream(stream))
    else
      io.Source.fromInputStream(stream)
  
  }

  def asStream(location: String) = {
    val stream = this.getClass.getResourceAsStream(location)
    val stream2 = if (location.endsWith(".gz")) new GZIPInputStream(stream) else stream
    new DataInputStream(stream2)
  }
}

object PMI {

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
    .filter(_._2  < 9)//Double.PositiveInfinity)
    .unzip
  }

  def contains(term: String) = unigrams.contains(term)

}

val stopwords = io.Source.fromFile("stopwords.english").getLines.toSet
val acronyms = io.Source.fromFile("acronyms.txt").getLines.map(_.trim.toLowerCase).toSet
val vulgarWords = io.Source.fromFile("vulgar.txt").getLines.toSet
val removeWords = acronyms ++ stopwords ++ vulgarWords ++ Set("don")

val tokenRegex = "([#@]?[a-zA-Z0-9_-]+)".r

def tokenize(sentence: String) = 
  (tokenRegex findAllIn sentence
    .toLowerCase
    .replaceAll("http[:/[a-zA-Z0-9_.?%]]+", " ") //remove links
  ).toVector

def clean(sentence: Vector[String]) = sentence
  .filter(w => w.length > 2 && !removeWords(w))

val filename = "5_3_13_twitterstore.txt.gz"

print("Counting...")
Resource.asSource(filename)
  .getLines
  .map(tokenize)
  .filter(_.count(stopwords) >= 6)
  .foreach(line => PMI.update(clean(line).toSet.toVector))
println("Complete!")

val searchWords = args

searchWords.foreach(term => {
  println("\nSearching for: "+term)
  if(PMI.contains(term)) {
    val (keywords, _) = PMI.getKeywords(term)
    if(keywords.length == 0) println("\tInsufficient Data")
    else {
    	println("\tMentions: "+keywords.filter(_.startsWith("@")).mkString(" "))
    	println("\tHashtags: "+keywords.filter(_.startsWith("#")).mkString(" "))
    	println("\tRelated Words: "+keywords.filterNot(x => x.startsWith("@") || x.startsWith("#")).mkString(" "))
    }
  } else {
    println("\tInsufficient Data")
  }
})

