package dash4twitter.app

import dash4twitter.util._
import java.io.File
import nak.NakContext._
import nak.core._
import nak.data._
import nak.liblinear._
import chalk.lang.eng.PorterStemmer

trait PolarityDetector {
  lazy val pos = "pos"
  lazy val neg = "neg"
  lazy val neutral = "neutral"

  def apply(data: FeatureExtractor): String
}

object LexicalPolarity extends PolarityDetector {

  /**
   * A function to use the lexicon method for SA.
   * 
   * @param evalFile - The path to the evaluation xml file
   */
  def apply(data: FeatureExtractor): String  = {
    val counts = data.tokens.foldLeft(0)((prevAssgn, token) => prevAssgn + English.LexicalPolarity(token))

    if(counts > 0) pos
    else if(counts < 0) neg
    else neutral
  }

}

object LibLinearPolarity extends PolarityDetector {
  
  lazy val Stemmer = new PorterStemmer
  
  //A featurizer using lowercase bag-of-words features
  //combined with lexicon based polarity features.
  val Featurizer = new Featurizer[FeatureExtractor, String] {
    def apply(data: FeatureExtractor) = {
      val words = data.tokens.mkString(" ")
        .replaceAll("(.)\\1{2,}", "$1")
        .replaceAll(" [0-9]+ ", " ")
        .split("\\s+")

      val wordFeatures = words
        .filterNot(English.removeWord)
        .map(tok => FeatureObservation("word="+Stemmer(tok)))

      val polarityFeature =
        Array(FeatureObservation("lexicalPolarity="+LexicalPolarity(data)))

      wordFeatures ++
      polarityFeature
    }
  }

  val rawData = readRaw("/home/cmdjarvis/code/gpp/data/debate08/train.xml")

  val classifier = trainClassifier(LiblinearConfig(solverType=Solver("L2R_LR"), cost=0.8),
                     Featurizer,
                     rawData)

  def maxLabelLiblinear = maxLabel(classifier.labels) _

  def apply(data: FeatureExtractor): String = maxLabelLiblinear(classifier.evalRaw(data))

  def mapper(label: String) = if(label.startsWith("p")) pos else if(label.startsWith("neg")) neg else neutral

  /**
   * A function to convert raw XML files to Example objects
   *
   * @param filename - The name of the file in the resources
   *                   folder containing the data to read. 
   */
  def readRaw(filename: String) =
    for(item <- (scala.xml.XML.loadFile(filename) \ "item"))
      yield Example(mapper((item \ "@label").text.trim), new FeatureExtractor(item.text.trim, Twokenizer))

}
