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
  private[this] val tweetBufferSize = 20
  twitterStream.addListener(this)
  twitterStream.sample()
  private[this] val candidateBuffer = Queue[(twitter4j.Status, String)]()   
  private[this] val filters = HashMap[String, StreamFilter]()
  private[this] val keywordFilter = "keyword"

  private[this] def emptyCandidateBuffer() {
    while(!candidateBuffer.isEmpty) candidateBuffer.dequeue
  }

  private[this] def filterValidate(data: FeatureExtractor) = {
    var valid = false
    val it = filters.keys.toVector.iterator
    while(!valid && it.hasNext) valid = filters(it.next())(data)
    valid
  }

  private[this] def addFilter(dataType: String, value: String) = {
    var added = false
    if(dataType==keywordFilter) {
      val filterName = keywordFilter+"="+value
      if(!filters.contains(filterName)) {
        filters(filterName) = KeywordFilter(value)
        println("Added filter: "+filterName)
        added = true
      }
    }
    added
  }

  private[this] def deleteFilter(filterName: String) = {
    emptyCandidateBuffer()
    if(filters.contains(filterName)) {
      filters.remove(filterName)
      emptyCandidateBuffer()
      println("Removed filter: "+filterName)
    }
    doNothing()
  }

  def doNothing() = Json.stringify(Json.obj("type" -> "empty"))

  def streamTweet() = {
    if (!candidateBuffer.isEmpty) {
      val (initStatus, label) = candidateBuffer.dequeue
      val status = if(initStatus.isRetweet()) initStatus.getRetweetedStatus
                   else initStatus
      Json.stringify(Json.obj("type" -> "tweet", "data" -> Json.obj("tweet" -> status.getText, 
                                                                    "id" -> status.getId.toString,
                                                                    "userImage" -> status.getUser().getProfileImageURL,
                                                                    "screenName" -> status.getUser().getScreenName,
                                                                    "username" -> status.getUser().getName,
                                                                    "label" -> label)))
    } else doNothing()
  }

  private[this] def refreshData(update: Boolean = true) = {
    val activeFilters = filters
       .keys
       .toVector
       .filter(_.startsWith("keyword="))
       .map(_.drop(8))
     val mappings = activeFilters.map(keyword => {
       println("Getting keywords for: "+keyword)
       val queryTerms = keyword.split("\\s+|\\+").map(_.trim)
       if(queryTerms.length < 2) {
         val keywords = if(Lexicon.contains(keyword)) Model.getKeywords(keyword)._1
                        else Vector()
         (keyword, keywords.take(50))
       } else {
         val results = queryTerms.map(term => {
           val result = if(Lexicon.contains(term)) Model.getKeywords(term)._1
                        else Vector()
           result
         }).sortBy(_.length)
         val intersection = results.take(1)(0).toSet
         (keyword, results
           .drop(1)
           .flatten
           .filter(intersection)
           .toVector
           .take(50))
       }
     }).toMap

     val termSentiment = activeFilters
       .flatMap{option => option.split("\\s+|\\+").map(_.trim)}
       .distinct
       .map(keyword => {
         val distribution = if(Lexicon.contains(keyword)) Lexicon(keyword).getPolarityDistribution()
                            else Vector()
         (keyword, distribution)
     }).toMap
   
     val rType = if(update) "keywordUpdate" else "keywordRefresh"
     Json.stringify(Json.obj(
       "type" -> rType,
       "data" -> Json.toJson(mappings),
       "sentiment" -> Json.toJson(termSentiment),
       "filters" -> Json.toJson(filters.keys.toSeq.sorted)
       )
     )
  }

  def getKeywords(command: String) = {
     command.split(",")
       .map(_.trim.toLowerCase)
       .distinct
       .foreach(filterOption => addFilter(keywordFilter, filterOption))
     refreshData()
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
      else if (action=="refreshData") refreshData(false)
      else doNothing()
    }
  }

  def onStatus(status: twitter4j.Status) {
    val tweet = status.getText
    //only use english tweets to train the model
    if(LanguageDetector(tweet) == "en") {
      //extract the features for the model
      val features = FeatureExtractor(tweet, ClassifierPolarityDetector(tweet), Twokenizer)
      //update the model
      Model.update(features)
      //if a tweet filters correctly, add it to the model
      if(filters.size > 0 && filterValidate(features)) {
        println("FILTERED: "+tweet)
        candidateBuffer.enqueue((status, features.polarity))
        if (candidateBuffer.size >= tweetBufferSize) candidateBuffer.dequeue
      }
    }
  }
}
