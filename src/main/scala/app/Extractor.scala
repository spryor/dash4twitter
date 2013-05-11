package dash4twitter.app

import dash4twitter.util._

/*
 * The KeywordExtractor currently contains the code for
 * testing the keyword extraction functionality
 */
object Extractor {

  import java.util.zip.GZIPInputStream
  import java.io._

  val filters: Vector[Filter] = Vector() //Vector(new KeywordFilter("google"), new KeywordFilter("facebook"))

  private[this] def filterValidate(tokens: Vector[String]) = {
    var valid = true
    val it = filters.iterator
    while(valid && it.hasNext) valid = it.next()(tokens)
    valid
  }

  def tokenize(sentence: String) = ("([#@]?[a-zA-Z0-9_-]+)".r findAllIn sentence
      .toLowerCase
      .replaceAll("http[:/[a-zA-Z0-9_.?%]]+", " ") //remove links
    ).toVector

  def clean(sentence: Vector[String]) = sentence
    .filter(w => w.length > 2 && !English.removeWord(w))

  def onStatus(status: String) {
    val tokens = clean(tokenize(status))
    Model.update(tokens.distinct)
    if(filters.length > 0 && filterValidate(tokens)) println(status)
  }

  /*
   * The main funciton assume that the args arrays is 
   * non-empty and contains search terms for which to
   * return keywords
   */
  def main(args: Array[String]) {

    val opts = ExtractorOpts(args)

    //Read the input file and build the data model
    if(opts.train() != "") {
      print("Counting...")
      io.Source
        .fromInputStream(new GZIPInputStream(new FileInputStream(opts.train())))
        .getLines
        .foreach(onStatus)
      println("Complete!")
    }

    //Read the test search terms and get the results
    opts.query().foreach(term => {
      println("\nSearching for: "+term)
      if(Lexicon.contains(term)) {
        val (keywords, _) = Model.getKeywords(term)
        if(keywords.length == 0) println("\tInsufficient Data")
        else {
          println("\tMentions: "+keywords.filter(_.startsWith("@")).mkString(" "))
          println("\tHashtags: "+keywords.filter(_.startsWith("#")).mkString(" "))
          println("\tRelated Words: "+keywords.filterNot(x => x.startsWith("@") || x.startsWith("#")).take(40).mkString(" "))
        }
      } else {
        println("\tNot available")
      }
    })
   
  }
}

object ExtractorOpts {

  import org.rogach.scallop._

  def apply(args: Array[String]) = new ScallopConf(args) {
    banner("""
Keyword extraction application.

For usage see below:
""")

    val load = opt[String]("load", short='l', default=Some(""), descr="The name of the prebuilt model to load")
    val save = opt[String]("save", short='s', default=Some(""), descr="The name of the model to save")
    val train = opt[String]("train", short='t', default=Some(""), descr="The path to the train file of tweets")
    val query = opt[List[String]]("query", short='q', descr="A list of search terms")
    val version = opt[Boolean]("version", noshort=true, default=Some(false), descr="Show version of this program")
    val help = opt[Boolean]("help", noshort = true, descr = "Show this message")
    val verbose = opt[Boolean]("verbose", short='v')
  }
}
