package com.giatsidis.spark

import com.giatsidis.spark.models.{Hashtag, Tweet, User}
import com.giatsidis.spark.services.MysqlService
import com.giatsidis.spark.utils.{InstantSerializer, OAuthUtils, SentimentAnalysisUtils, TextUtils}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.twitter.TwitterUtils
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import redis.clients.jedis.Jedis

object Main {
  implicit val formats = DefaultFormats + InstantSerializer

  def main(args: Array[String]): Unit = {
    OAuthUtils.init()
    val sparkConf = new SparkConf().setAppName(this.getClass.getSimpleName).setMaster("local[*]")
    val streamingContext = new StreamingContext(sparkConf, Seconds(Config.streamingBatchDuration))
    streamingContext.sparkContext.setLogLevel("ERROR")
    val filters = Array("Bitcoin", "BTC", "Ethereum", "ETH", "XRP", "Tether", "Litecoin");
    val tweets = TwitterUtils.createStream(streamingContext, None, filters)

    tweets.foreachRDD { rdd =>
      val savedRdd = rdd
        .filter(_.getLang == "en")
        // Filter retweets
        .filter(!_.isRetweet)
        // Filter tweets with big number of hashtags
        .filter(_.getHashtagEntities.toList.length < 5)
        // Filter tweets with short content length
        .filter(_.getText.length > 20)
        .map(status => {
          val cleanedText = TextUtils.cleanText(status.getText)
          Tweet(
            status.getId,
            status.getText,
            Option(status.getGeoLocation).map(geo => {
              s"${geo.getLatitude},${geo.getLongitude}"
            }),
            SentimentAnalysisUtils.detectSentiment(cleanedText).toString,
            status.getCreatedAt.toInstant,
            status.getHashtagEntities.toList.map(h => Hashtag(h.getText)),
            User(status.getUser.getId, status.getUser.getScreenName, status.getUser.getProfileImageURLHttps),
          )
        })

      // save to MySQL
      MysqlService.save(savedRdd)
      // publish to Redis
      savedRdd.foreachPartition(partition => {
        partition.foreach(m => {
          val jedis = new Jedis(Config.redisHost, Config.redisPort)
          val pipeline = jedis.pipelined
          pipeline.publish(Config.redisChannel, Serialization.write(m))
          pipeline.sync()
        })
      })
    }

    streamingContext.start()
    streamingContext.awaitTermination()

  }
}

