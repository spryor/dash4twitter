import collection.mutable.HashMap  

val stopwords = io.Source.fromFile("stopwords.english").getLines.toSet

def tokenize(sentence: String) = sentence
  .replaceAll("""<(mention|link|hashtag)>|rt\s""", " ")
  .replaceAll("[^a-zA-Z0-9\\s]", " ")
  .trim
  .split("\\s+")
  .toIndexedSeq
  .filterNot(stopwords)

val filename = "trainingTweets.txt"

val lines = io.Source
    .fromFile(filename)("UTF-8")
    .getLines
    .toList
    .map(tokenize)
    .take(100000)

val words = lines.flatten
  .groupBy(w => w)
  .mapValues(_.length.toDouble)
  .toMap

var graph = HashMap[String,HashMap[String, Double]]()
val greatestOccurence = HashMap[String,Double]().withDefaultValue(0.0)

lines.foreach(tweet => {
  tweet.foreach(cWord => {
    if(!graph.contains(cWord)) graph(cWord) = HashMap[String, Double]()
    tweet.foreach(coWord => {
      if(coWord != cWord && cWord.length > 2)
        if(!graph(cWord).contains(coWord)) graph(cWord)(coWord) = 1.0
        else graph(cWord)(coWord) += 1.0
    })
  })
})

graph.keys.foreach(k => {
  graph(k).keys.foreach(internal => {
    if(graph(k)(internal) > greatestOccurence(internal))
      greatestOccurence(internal) = graph(k)(internal)
  })
})

val searchWords = if(args.length > 0) args
                  else Array("republican")

searchWords.foreach(term => {
  println("\nSearching for: "+term)
  if(graph.contains(term)) {
    val m2 = graph(term).toList
      .map{case (w, c) => (w, c/greatestOccurence(w))}
      .sortBy(_._2)
      .reverse 
      .map(_._1)
 
    println("\t"+m2.take(40).mkString(" "))
    println
    val m1 = graph(term).toList
      .sortBy(_._2)
      .reverse
      .map(_._1)

    println("\t"+m1.take(40).mkString(" "))
    println

    val intersect = m1.take(40).toSet & m2.take(40).toSet
    val sortIntersect = m1.filter(intersect)
    
    println("\t"+sortIntersect.mkString(" "))
  } else { println("\tNo data") } 
})
