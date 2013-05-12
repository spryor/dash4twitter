package dash4twitter.app

case class Formats (text: String, tokens: Vector[String], elements: Set[String])

trait Filter {
  def apply(data: Formats): Boolean
}

class KeywordFilter(keyword: String) extends Filter {
  def apply(data: Formats): Boolean = data.elements(keyword)
}

class PolarityFilter(polarity: Int) extends Filter {

  private[this] def detector(text: String) = 1

  override def apply(data: Formats): Boolean = detector(data.text) == polarity
} 

object PosFilter extends PolarityFilter(1)
object NegFilter extends PolarityFilter(-1)
object NeutralFilter extends PolarityFilter(0)

