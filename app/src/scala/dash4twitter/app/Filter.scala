package dash4twitter.app

import dash4twitter.util.Tokenizer

case class FeatureExtractor (text: String, polarity: String, tokenizer: Tokenizer) {
  val tokens = tokenizer(text)
  val distinct = tokens.distinct
  val elements = tokens.toSet
}

trait StreamFilter {
  def apply(data: FeatureExtractor): Boolean
}

class KeywordFilter(keywords: Vector[String]) extends StreamFilter {
  def apply(data: FeatureExtractor): Boolean = {
    var valid = true
    val it = keywords.iterator
    while(valid && it.hasNext) valid = data.elements(it.next())
    valid
  }
}

object KeywordFilter {
  def apply(keywords: String) = new KeywordFilter(keywords.split("\\+").toVector.map(_.trim))
}

class PolarityFilter(result: String, detector: PolarityDetector) extends StreamFilter {
  def apply(data: FeatureExtractor): Boolean = detector(data.text) == result
}

object PolarityFilter {

  lazy val detector: PolarityDetector = ClassifierPolarityDetector
  
  def apply(result: String) = {
    val polarity = if(result == "POS") detector.POS
                   else if(result == "NEG") detector.NEG
                   else detector.NEUTRAL
    new PolarityFilter(polarity, detector)
  }
}

