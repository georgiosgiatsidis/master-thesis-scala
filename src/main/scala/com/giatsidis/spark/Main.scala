package com.giatsidis.spark

import com.giatsidis.spark.models.{Tweet, User}
import com.giatsidis.spark.utils.{OAuthUtils, SentimentAnalysisUtils, TextUtils}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.twitter.TwitterUtils
import org.json4s.jackson.Serialization
import redis.clients.jedis.Jedis

object Main {
  implicit val formats = org.json4s.DefaultFormats

  def main(args: Array[String]): Unit = {
    OAuthUtils.init()
    val sparkConf = new SparkConf().setAppName("master-thesis-scala").setMaster("local[*]")
    val streamingContext = new StreamingContext(sparkConf, Seconds(Config.streamingBatchDuration))
    streamingContext.sparkContext.setLogLevel("OFF")
    val tweets = TwitterUtils.createStream(streamingContext, None, Array("covid"))

    tweets.foreachRDD { rdd =>
      rdd
        .filter(_.getLang.contains("en"))
        .filter(!_.isRetweet)
        .map(status => {
          val cleanedText = TextUtils.cleanText(status.getText)
          Tweet(
            status.getId,
            User(status.getUser.getScreenName, status.getUser.getProfileImageURLHttps),
            status.getCreatedAt.toInstant.toString,
            Option(status.getGeoLocation).map(geo => {
              s"${geo.getLatitude},${geo.getLongitude}"
            }),
            status.getText,
            cleanedText,
            status.getHashtagEntities.toList.map(_.getText),
            status.getRetweetCount,
            status.getLang,
            SentimentAnalysisUtils.detectSentiment(cleanedText).toString
          )
        }).foreach(m => {
        val jedis = new Jedis(Config.redisHost, Config.redisPort)
        val pipeline = jedis.pipelined
        pipeline.publish(Config.redisChannel, Serialization.write(m))
        pipeline.sync()
      })
    }

    streamingContext.start()
    streamingContext.awaitTermination()

  }
}
