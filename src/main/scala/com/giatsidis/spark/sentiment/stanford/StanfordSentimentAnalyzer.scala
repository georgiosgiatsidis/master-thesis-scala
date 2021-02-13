package com.giatsidis.spark.sentiment.stanford

import java.util.Properties

import com.giatsidis.spark.sentiment.{NEGATIVE, NEUTRAL, NOT_UNDERSTOOD, POSITIVE, SentimentType, VERY_NEGATIVE, VERY_POSITIVE}
import com.giatsidis.spark.utils.TextUtils

import collection.JavaConverters._
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations

import scala.collection.mutable.ListBuffer

object StanfordSentimentAnalyzer {

  lazy val pipeline = {
    val props = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment")
    new StanfordCoreNLP(props)
  }

  def detectSentiment(message: String): SentimentType = {

    val annotation: Annotation = pipeline.process(TextUtils.cleanText(message))
    var sentiments: ListBuffer[Double] = ListBuffer()
    var sizes: ListBuffer[Int] = ListBuffer()

    var longest = 0
    var mainSentiment = 0

    val sentencesList = annotation.get(classOf[CoreAnnotations.SentencesAnnotation]).asScala;

    sentencesList.foreach(sentence => {
      val tree = sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])
      val sentiment = RNNCoreAnnotations.getPredictedClass(tree)
      val partText = sentence.toString

      if (partText.length() > longest) {
        mainSentiment = sentiment
        longest = partText.length()
      }
      sentiments += sentiment.toDouble
      sizes += partText.length
    })


    val averageSentiment: Double = {
      if (sentiments.nonEmpty) sentiments.sum / sentiments.size
      else -1
    }

    val weightedSentiments = (sentiments, sizes).zipped.map((sentiment, size) => sentiment * size)
    var weightedSentiment = weightedSentiments.sum / sizes.sum

    if (sentiments.isEmpty) {
      mainSentiment = -1
      weightedSentiment = -1
    }

    weightedSentiment match {
      case s if s <= 0.0 => NOT_UNDERSTOOD
      case s if s < 1.0 => VERY_NEGATIVE
      case s if s < 2.0 => NEGATIVE
      case s if s < 3.0 => NEUTRAL
      case s if s < 4.0 => POSITIVE
      case s if s < 5.0 => VERY_POSITIVE
      case s if s > 5.0 => NOT_UNDERSTOOD
    }
  }

}
