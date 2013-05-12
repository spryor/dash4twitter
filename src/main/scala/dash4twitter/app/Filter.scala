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

abstract class PolarityFilter(result: String, detector: PolarityDetector) extends Filter {
  def apply(data: FeatureExtractor): Boolean = detector(data) == result
}

object PosFilter extends PolarityFilter(LibLinearPolarity.pos, LibLinearPolarity)
object NegFilter extends PolarityFilter(LibLinearPolarity.neg, LibLinearPolarity)
object NeutralFilter extends PolarityFilter(LibLinearPolarity.neutral, LibLinearPolarity)

