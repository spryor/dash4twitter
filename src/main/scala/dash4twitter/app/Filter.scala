package dash4twitter.app

import dash4twitter.util.Tokenizer

case class FeatureExtractor (text: String, tokenizer: Tokenizer) {
  lazy val tokens = tokenizer(text)
  lazy val distinct = tokens.distinct
  lazy val elements = tokens.toSet
}

trait Filter {
  def apply(data: FeatureExtractor): Boolean
}

class KeywordFilter(keyword: String) extends Filter {
  def apply(data: FeatureExtractor): Boolean = data.elements(keyword)
}

object KeywordFilter {
  def apply(keyword: String) = new KeywordFilter(keyword)
}

class PolarityFilter(result: String, detector: PolarityDetector) extends Filter {
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

