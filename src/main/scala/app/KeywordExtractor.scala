package dash4twitter.app

import dash4twitter.util._

object KeywordExtractor {
  
  val PMI = new PMI()

  def tokenize(sentence: String) = ("([#@]?[a-zA-Z0-9_-]+)".r findAllIn sentence
      .toLowerCase
      .replaceAll("http[:/[a-zA-Z0-9_.?%]]+", " ") //remove links
    ).toVector

	def clean(sentence: Vector[String]) = sentence
    .filter(w => w.length > 2 && !English.removeWord(w))

  def main(args: Array[String]) {

    val filename = "/lang/eng/data/englishTweets_5_3_13.txt.gz"

    var lineCnt = 0
    print("Counting...")
    Resource.asSource(filename)
			.getLines
			.foreach(line => {
				lineCnt += 1
				PMI.update(clean(tokenize(line)).toSet.toVector)
			})
		println("Complete! " + lineCnt + " tweets")

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

  }
}
