package com.giatsidis.spark.utils

import com.giatsidis.database.models.Term
import com.giatsidis.spark.Config
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.DStream
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import redis.clients.jedis.Jedis
import twitter4j.Status

object Helpers {
  implicit val formats = DefaultFormats

  def applyFilters(rdd: RDD[Status]): RDD[Status] = {
    rdd
      .filter(_.getLang == "en")
      .filter(!_.isRetweet)
      .filter(_.getHashtagEntities.toList.length < 5)
      .filter(_.getText.length > 50)
      .filter(_.getUser.getFollowersCount > 200)
  }

  def topHashtags(tweets: DStream[Status]): Unit = {
    // Inspired by
    // https://github.com/stdatalabs/SparkTwitterStreamAnalysis/blob/master/src/main/scala/com/stdatalabs/Streaming
    //    val hashtags = tweets.map(_.getText).flatMap(_.split(" ")).filter(_.startsWith("#"))
    val hashtags = tweets.flatMap(_.getHashtagEntities.toList.map(_.getText.toLowerCase))

    val topHashtags = hashtags.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(60))
      .map { case (topic, count) => (count, topic) }
      .transform(_.sortByKey(false))

    topHashtags.foreachRDD(rdd => {
      val jedis = new Jedis(Config.redisHost, Config.redisPort)
      val pipeline = jedis.pipelined
      val m = rdd.take(10)
      pipeline.publish("topHashtags", Serialization.write(m))
      pipeline.sync()
    })
  }

  def getKeywordsFromTerms(terms: List[Term]): List[String] = {
    terms.flatMap(term => term.keywords.split(","))
  }
}
