package com.giatsidis.spark.sentiment

import com.giatsidis.spark.sentiment.mllib.MLlibSentimentAnalyzer
import com.giatsidis.spark.sentiment.stanford.StanfordSentimentAnalyzer
import com.giatsidis.spark.utils.TextUtils
import org.apache.spark.sql.{Row, SparkSession}

object AccuracyEvaluator {
  def sentimentToPolarity(sentimentType: SentimentType): Double = {
    if (sentimentType == NEGATIVE || sentimentType == VERY_NEGATIVE) {
      0.0
    } else if (sentimentType == POSITIVE || sentimentType == VERY_POSITIVE) {
      4.0
    } else {
      2.0
    }
  }

  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder
      .master(sys.env.getOrElse("SPARK_MASTER", "local[*]"))
      .appName(this.getClass.getSimpleName)
      .getOrCreate()

    val naiveBayesModel = MLlibSentimentAnalyzer.createNBModel(sparkSession.sparkContext)

    val tweetsDF = MLlibSentimentAnalyzer.readSentimentFile(sparkSession)

    val t1 = System.nanoTime
    val actualVsPredictionRDD = tweetsDF.limit(10000).select("polarity", "tweet").rdd.map {
      case Row(polarity: Int, tweet: String) =>
        val words: Seq[String] = TextUtils.cleanText(tweet).split(" ")
        //        val stanford = sentimentToPolarity(StanfordSentimentAnalyzer.detectSentiment(tweet))
        (polarity.toDouble,
          naiveBayesModel.predict(MLlibSentimentAnalyzer.transformFeatures(words)))
      //          stanford)
    }

    val duration = (System.nanoTime - t1) / 1e9d
    println(duration)
    val accuracy = 100.0 * actualVsPredictionRDD.filter(x => x._1 == x._2).count() / actualVsPredictionRDD.count()
    println(accuracy)

  }
}
