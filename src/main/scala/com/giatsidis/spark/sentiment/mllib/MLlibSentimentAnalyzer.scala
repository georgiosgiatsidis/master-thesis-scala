package com.giatsidis.spark.sentiment.mllib

import com.giatsidis.spark.utils.TextUtils
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.SparkContext


object MLlibSentimentAnalyzer {

  def computeSentiment(text: String, model: NaiveBayesModel): String = {
    val words: Seq[String] = TextUtils.cleanText(text).split(" ")
    val polarity = model.predict(MLlibSentimentAnalyzer.transformFeatures(words))
    normalizeSentiment(polarity)
  }

  def normalizeSentiment(sentiment: Double): String = {
    sentiment match {
      case x if x == 0 => "NEGATIVE"
      case x if x == 2 => "NEUTRAL"
      case x if x == 4 => "POSITIVE"
      case _ => "NEUTRAL"
    }
  }

  val hashingTF = new HashingTF()

  def transformFeatures(text: Seq[String]): Vector = {
    hashingTF.transform(text)
  }

  def createModel(sc: SparkContext): NaiveBayesModel = {
    val htf = new HashingTF()
    val spark = SparkSession.builder.config(sc.getConf).getOrCreate()

    val df = spark.read
      .format("com.databricks.spark.csv")
      .option("header", "false")
      .option("inferSchema", "true")
      //      .load("data/testdata.manual.2009.06.14.csv")
      .load("data/training.1600000.processed.noemoticon.csv")
      .toDF("polarity", "id", "date", "query", "user", "tweet")

    val labledRdd: RDD[LabeledPoint] = df.select("polarity", "tweet").rdd.map {
      case Row(polarity: Int, tweet: String) =>
        val words = TextUtils.cleanText(tweet).split(" ")
        LabeledPoint(polarity, htf.transform(words))
    }

    labledRdd.cache()

    val bayesModel: NaiveBayesModel = NaiveBayes.train(labledRdd, lambda = 1.0, modelType = "multinomial")
    bayesModel
  }
}
