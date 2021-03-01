package com.giatsidis.spark.utils

import com.giatsidis.database.models.Term
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.DStream
import twitter4j.Status

object Helpers {
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
    val hashtags = tweets.map(_.getText).flatMap(_.split(" ")).filter(_.startsWith("#"))

    val topHashtags = hashtags.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(60))
      .map { case (topic, count) => (count, topic) }
      .transform(_.sortByKey(false))

    topHashtags.foreachRDD(rdd => {
      println("\nPopular topics in last 60 seconds (%s total):".format(rdd.count()))
      rdd.foreach { case (count, tag) => println("%s (%s tweets)".format(tag, count)) }
    })
  }

  def getKeywordsFromTerms(terms: List[Term]): List[String] = {
    terms.flatMap(term => term.keywords.split(","))
  }
}
