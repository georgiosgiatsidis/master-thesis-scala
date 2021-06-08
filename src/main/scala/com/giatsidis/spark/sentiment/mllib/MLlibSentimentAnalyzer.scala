package com.giatsidis.spark.sentiment.mllib

import com.giatsidis.spark.Config
import com.giatsidis.spark.sentiment.{NEGATIVE, NEUTRAL, POSITIVE, SentimentType}
import com.giatsidis.spark.utils.TextUtils
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.SparkContext


object MLlibSentimentAnalyzer {

  def detectSentiment(text: String, model: NaiveBayesModel): SentimentType = {
    val words: Seq[String] = TextUtils.cleanText(text).split(" ")
    val polarity = model.predict(MLlibSentimentAnalyzer.transformFeatures(words))
    normalizeSentiment(polarity)
  }

  def normalizeSentiment(sentiment: Double): SentimentType = {
    sentiment match {
      case x if x == 0 => NEGATIVE
      case x if x == 2 => NEUTRAL
      case x if x == 4 => POSITIVE
      case _ => NEUTRAL
    }
  }

  val hashingTF = new HashingTF()

  def transformFeatures(text: Seq[String]): Vector = {
    hashingTF.transform(text)
  }

  def readSentimentFile(sparkSession: SparkSession): DataFrame = {
    sparkSession.read
      .format("com.databricks.spark.csv")
      .option("header", "false")
      .option("inferSchema", "true")
      .load(Config.trainingDataFilePath)
      .toDF("polarity", "id", "date", "query", "user", "tweet")
  }

  def createNBModel(sc: SparkContext): NaiveBayesModel = {
    val sparkSession = SparkSession.builder.config(sc.getConf).getOrCreate()

    val df = readSentimentFile(sparkSession)

    val labeledRDD: RDD[LabeledPoint] = df.select("polarity", "tweet").rdd.map {
      case Row(polarity: Int, tweet: String) =>
        val words = TextUtils.cleanText(tweet).split(" ")
        LabeledPoint(polarity, MLlibSentimentAnalyzer.transformFeatures(words))
    }

    labeledRDD.cache()

    val bayesModel: NaiveBayesModel = NaiveBayes.train(labeledRDD, lambda = 1.0, modelType = "multinomial")
    bayesModel
  }
}
