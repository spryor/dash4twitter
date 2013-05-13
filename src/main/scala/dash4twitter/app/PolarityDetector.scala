package dash4twitter.app

import dash4twitter.util.{Twokenizer, English}
import java.io._
import java.util.zip._
import nak.NakContext._
import nak.core._
import nak.data._
import nak.liblinear._
import chalk.lang.eng.PorterStemmer

trait PolarityDetector {
  lazy val POS = "pos"
  lazy val NEG = "neg"
  lazy val NEUTRAL = "neutral"

  def apply(tokens: String): String
}

object LexicalPolarityDetector extends PolarityDetector {

  def apply(text: String): String = getPolarity(Twokenizer(text))

  def getPolarity(tokens: Vector[String]) = {
    val counts = tokens.foldLeft(0)((prevAssgn, token) => prevAssgn + English.LexicalPolarity(token))

    if(counts > 0) POS
    else if(counts < 0) NEG
    else NEUTRAL
  }

}

object ClassifierPolarityDetector extends PolarityDetector {

  PolarityClassifier.train_and_save("/home/cmdjarvis/code/gpp/data/debate08/train.xml","/home/cmdjarvis/code/model.gz")
  val classifier = PolarityClassifier.load("/home/cmdjarvis/code/model.gz") 

  def apply(text: String): String = classifier.predict(text)

}

object PolarityClassifier {
  
  lazy val Stemmer = new PorterStemmer
  
  //A featurizer using lowercase bag-of-words features
  //combined with lexicon based polarity features.
  val Featurizer = new Featurizer[String, String] {
    def apply(text: String) = {
      val tokens = Twokenizer(text)
      val words = tokens
        .mkString(" ")
        .replaceAll("(.)\\1{2,}", "$1")
        .replaceAll(" [0-9]+ ", " ")
        .split("\\s+")

      val wordFeatures = words
        .filterNot(English.removeWord)
        .map(tok => FeatureObservation("word="+Stemmer(tok)))

      val polarityFeature =
        Array(FeatureObservation("lexicalPolarity="+LexicalPolarityDetector.getPolarity(tokens)))

      wordFeatures ++
      polarityFeature
    }
  }
 
  def train_and_save(trainFile: String, saveFile: String) {
    val trainData = readRaw(trainFile)
    val config = LiblinearConfig(solverType=Solver("L2R_LR"), cost=0.8)
    val model: FeaturizedClassifier[String, String] = trainClassifier(config, Featurizer, trainData)
    saveClassifier(model, saveFile)
  }

  def load(filename: String) =  
    loadClassifier[FeaturizedClassifier[String, String]](filename)

  def mapper(label: String) = if(label.startsWith("p")) LexicalPolarityDetector.POS 
                              else if(label.startsWith("neg")) LexicalPolarityDetector.NEG 
                              else LexicalPolarityDetector.NEUTRAL

  /**
   * A function to convert raw XML files to Example objects
   *
   * @param filename - The name of the file in the resources
   *                   folder containing the data to read. 
   */
  def readRaw(filename: String) =
    for(item <- (scala.xml.XML.loadFile(filename) \ "item"))
      yield Example(mapper((item \ "@label").text.trim), item.text.trim)

}

