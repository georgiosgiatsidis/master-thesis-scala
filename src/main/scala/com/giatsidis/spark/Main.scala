package com.giatsidis.spark

import com.giatsidis.spark.models.{Hashtag, Tweet, User}
import com.giatsidis.spark.sentiment.mllib.MLlibSentimentAnalyzer
import com.giatsidis.spark.sentiment.stanford.StanfordSentimentAnalyzer
import com.giatsidis.spark.services.{MysqlService, RedisService}
import com.giatsidis.spark.utils.{Helpers, OAuthUtils, TextUtils}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.twitter.TwitterUtils

object Main {
  def main(args: Array[String]): Unit = {
    OAuthUtils.init()
    val sparkConf = new SparkConf().setAppName(this.getClass.getSimpleName).setMaster("local[*]")
    val streamingContext = new StreamingContext(sparkConf, Seconds(Config.streamingBatchDuration))
    streamingContext.sparkContext.setLogLevel("ERROR")
    val filters = Array("Bitcoin", "BTC", "Ethereum", "ETH", "XRP", "Tether", "Litecoin");
    val tweets = TwitterUtils.createStream(streamingContext, None, filters)
    //    val model = MLlibSentimentAnalyzer.createNBModel(streamingContext.sparkContext)

    tweets.foreachRDD { rdd =>
      val savedRdd =
        Helpers.applyFilters(rdd)
          .map(status => {
            val cleanedText = TextUtils.cleanText(status.getText)
            Tweet(
              status.getId,
              status.getText,
              Option(status.getGeoLocation).map(geo => {
                s"${geo.getLatitude},${geo.getLongitude}"
              }),
              StanfordSentimentAnalyzer.detectSentiment(cleanedText).toString,
              //            MLlibSentimentAnalyzer.computeSentiment(status.getText, model),
              status.getCreatedAt.toInstant,
              status.getHashtagEntities.toList
                .filter(h => filters.map(_.toLowerCase).contains(h.getText.toLowerCase))
                .map(h => Hashtag(h.getText)),
              User(status.getUser.getId, status.getUser.getScreenName, status.getUser.getProfileImageURLHttps),
              filters.toList.filter(f => {
                status.getText.toLowerCase.contains(f.toLowerCase())
              })
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

