package com.giatsidis.spark

import com.giatsidis.repositories.TermRepository
import com.giatsidis.spark.models.{Hashtag, Tweet, User, Term}
import com.giatsidis.spark.sentiment.mllib.MLlibSentimentAnalyzer
import com.giatsidis.spark.sentiment.stanford.StanfordSentimentAnalyzer
import com.giatsidis.spark.services.{MysqlService, RedisService}
import com.giatsidis.spark.utils.{Helpers, OAuthUtils, TextUtils}
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.twitter.TwitterUtils

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main {
  def main(args: Array[String]): Unit = {
    OAuthUtils.init()

    val terms = Await.result(TermRepository.getAll(), Duration.Inf)

    val sparkSession = SparkSession.builder
      .master(sys.env.getOrElse("SPARK_MASTER", "local[*]"))
      .appName(this.getClass.getSimpleName)
      .getOrCreate()

    val sparkContext = sparkSession.sparkContext

    val streamingContext = new StreamingContext(sparkContext, Seconds(Config.streamingBatchDuration))
    streamingContext.sparkContext.setLogLevel("ERROR")
    streamingContext.checkpoint(Config.checkpointDirectory)
    val filters = Helpers.getKeywordsFromTerms(terms);
    val tweets = TwitterUtils.createStream(streamingContext, None, filters.toArray)
    val model = MLlibSentimentAnalyzer.createNBModel(streamingContext.sparkContext)

    Helpers.topHashtags(tweets)

    tweets.foreachRDD { rdd =>
      val savedRdd =
        Helpers.applyFilters(rdd)
          .map(status => {
            Tweet(
              status.getId,
              status.getText,
              Option(status.getGeoLocation).map(geo => {
                s"${geo.getLatitude},${geo.getLongitude}"
              }),
              StanfordSentimentAnalyzer.detectSentiment(status.getText).toString,
              MLlibSentimentAnalyzer.detectSentiment(status.getText, model).toString,
              status.getCreatedAt.toInstant,
              status.getHashtagEntities.toList
                .filter(h => filters.map(_.toLowerCase).contains(h.getText.toLowerCase))
                .map(h => Hashtag(h.getText)),
              User(status.getUser.getId, status.getUser.getScreenName, status.getUser.getProfileImageURLHttps),
              terms.filter { term =>
                val keywords = term.keywords.split(",")
                keywords.exists(keyword => status.getText.toLowerCase.contains(keyword.toLowerCase()))
              }.map(t => Term(t.id.get, t.name))
            )
          })

      // save to MySQL
      MysqlService.save(savedRdd)
      // publish to Redis
      RedisService.save(savedRdd)
    }

    streamingContext.start()
    streamingContext.awaitTermination()

  }
}

