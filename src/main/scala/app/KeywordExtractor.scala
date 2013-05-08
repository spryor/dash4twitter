package dash4twitter.app

import dash4twitter.util._


/*
 * The KeywordExtractor currently contains the code for
 * testing the keyword extraction functionality
 */
object KeywordExtractor {
  
  val PMI = new PMI()

  def tokenize(sentence: String) = ("([#@]?[a-zA-Z0-9_-]+)".r findAllIn sentence
      .toLowerCase
      .replaceAll("http[:/[a-zA-Z0-9_.?%]]+", " ") //remove links
    ).toVector

  def clean(sentence: Vector[String]) = sentence
    .filter(w => w.length > 2 && !English.removeWord(w))

  /*
   * The main funciton assume that the args arrays is 
   * non-empty and contains search terms for which to
   * return keywords
   */
  def main(args: Array[String]) {

    val opts = ExtractorOpts(args)

    if(opts.load() != "") {
      print("Loading model...")
      PMI.load(Resource.asStream("/lang/eng/data/"+opts.load()))
      println("Complete!")
    }

    //Read the input file and build the data model
    if(opts.train() != "") {
      var lineCnt = 0
      print("Counting...")
      Resource.asSource("/lang/eng/data/"+opts.train())
        .getLines
        .foreach(line => {
          lineCnt += 1
          PMI.update(clean(tokenize(line)).toSet.toVector)
        })
      println("Complete!")
    }

//    PMI.load(Resource.asStream("/lang/eng/data/PMI.gz"))
//    PMI.save("/home/cmdjarvis/code/dash4twitter/PMI.gz")
    //Read the test search terms and get the results
    opts.query().foreach(term => {
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

    if(opts.save() != "") {
      print("Saving model...")
      val filename = if(opts.save().endsWith(".gz")) opts.save()
                     else opts.save()+".gz"
      PMI.save(this.getClass().getResource("/lang/eng/data/").getPath()+filename)
      println("Complete!")
    } 
   
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
