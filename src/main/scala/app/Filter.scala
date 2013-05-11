package dash4twitter.app

trait Filter {
  def apply(tokens: Vector[String]): Boolean
}

class KeywordFilter(keyword: String) extends Filter {
  def apply(tokens: Vector[String]): Boolean = tokens.toSet(keyword)
}

