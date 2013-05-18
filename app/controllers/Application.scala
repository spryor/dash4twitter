package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.libs.concurrent._
import play.api.libs.iteratee._
import play.api.libs.json.Json
import twitter4j._
import scala.collection.mutable._
import dash4twitter.util.{Twokenizer, LanguageDetector, Model, Lexicon, Cooccurrences, English}
import dash4twitter.app._

trait BasicStreamer extends StatusListener{
  def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}
  def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}
  def onException(ex: Exception) { } 
  def onScrubGeo(arg0: Long, arg1: Long) {}
  def onStallWarning(warning: StallWarning) {}
}

object Application extends Controller with BasicStreamer{
  
  import collection.mutable.HashMap

  val twitterStream = new TwitterStreamFactory().getInstance
  val tweetBufferSize = 20
  twitterStream.addListener(this)
  twitterStream.sample()
  val candidateBuffer = Queue[(twitter4j.Status, String)]()   
  val filters = HashMap[String, StreamFilter]()
  val keywordFilter = "keyword"

  private[this] def filterValidate(data: FeatureExtractor) = {
    var valid = false
    val it = filters.keys.toVector.iterator
    while(!valid && it.hasNext) valid = filters(it.next())(data)
    valid
  }

  private[this] def addFilter(dataType: String, value: String) {
    if(dataType==keywordFilter) {
      val filterName = keywordFilter+"="+value
      if(!filters.contains(filterName)) {
        filters(filterName) = KeywordFilter(value)
        println("Added filter: "+filterName)
      }
    }
  }

  private[this] def deleteFilter(filterName: String) = {
    if(filters.contains(filterName)) {
      filters.remove(filterName)
      println("Removed filter: "+filterName)
    }
    doNothing()
  }

  private[this] def clean(sentence: Vector[String]) = sentence
    .filter(w => w.length > 2 && !English.removeWord(w) && !(w matches "[0-9]+"))

  def doNothing() = Json.stringify(Json.obj("type" -> "empty"))

  def streamTweet() = {
    if (!candidateBuffer.isEmpty) {
      val (status, label) = candidateBuffer.dequeue
      Json.stringify(Json.obj("type" -> "tweet", "data" -> Json.obj("tweet" -> status.getText, 
                                                                    "id" -> status.getId,
                                                                    "userImage" -> status.getUser().getProfileImageURL,
                                                                    "screenName" -> status.getUser().getScreenName,
                                                                    "username" -> status.getUser().getName,
                                                                    "label" -> label)))
    } else doNothing()
  }

  def getKeywords(command: String) = {
     while(!candidateBuffer.isEmpty) candidateBuffer.dequeue
     val currentFilters = filters.keys
     currentFilters.foreach(filterName => 
       if(filterName.startsWith(keywordFilter)) deleteFilter(filterName)
     )
     val mappings = command
       .split(",").map(k => {
         val keyword = k.trim
         addFilter(keywordFilter, keyword)
         println("Getting keywords for: "+keyword)
         val keywords = if(Lexicon.contains(keyword)) Model.getKeywords(keyword)._1
                        else Vector()
         (keyword, keywords.take(50).toSeq)
       }).toMap
     Json.stringify(Json.obj("type" -> "keywords", "data" -> Json.toJson(mappings), "filters" -> Json.toJson(filters.keys.toSeq)) )
  }

  def index = WebSocket.adapter {implicit req =>
    var testMsg = ""
    Enumeratee.map[String] {msg =>
      val request = Json.parse(msg)
      val action = (request \ "action").as[String]
      val command = (request \ "message").as[String]
      if (action=="stream") streamTweet()
      else if (action=="removeFilter") deleteFilter(command)
      else if (action=="getkeywords") getKeywords(command)
      else doNothing()
    }
  }

  def onStatus(status: twitter4j.Status) {
    val tweet = status.getText
    //only use english tweets to train the model
    if(LanguageDetector(tweet) == "en") {
      println(tweet)
      //extract the features for the model
      val features = FeatureExtractor(tweet, Twokenizer)
      //update the model
      Model.update(clean(features.distinct))
      //if a tweet filters correctly, add it to the model
      if(filters.size > 0 && filterValidate(features)) {
        println("FILTERED: "+tweet)
        val label = ClassifierPolarityDetector(features.text)//LexicalPolarityDetector.getPolarity(features.tokens)
        if (candidateBuffer.size >= tweetBufferSize) {   
          candidateBuffer.enqueue((status, label))
          candidateBuffer.dequeue
        } else {
          candidateBuffer.enqueue((status, label))
        }
      }
    }
  }
}
