package dash4twitter.util

trait Tokenizer {
  def apply(text: String): Vector[String]
}

object Twokenizer extends Tokenizer {

  private[this] val tokenRegex = """([#@]?[a-zA-Z0-9_]+['-]?[a-zA-Z0-9_-])""".r

  def apply(text: String): Vector[String] = (tokenRegex findAllIn text
    .toLowerCase
    .replaceAll("http[:/[a-zA-Z0-9_.?%]]+", " ")//remove links and numbers
  ).toVector

}
