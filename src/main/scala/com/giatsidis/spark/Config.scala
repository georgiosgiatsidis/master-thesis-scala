package com.giatsidis.spark

import com.typesafe.config.{Config, ConfigFactory}

object Config {
  private val conf: Config = ConfigFactory.load()

  val consumerKey = conf.getString("CONSUMER_KEY")
  val consumerSecret = conf.getString("CONSUMER_SECRET")
  val accessToken = conf.getString("ACCESS_TOKEN")
  val accessTokenSecret = conf.getString("ACCESS_TOKEN_SECRET")
  val redisHost = conf.getString("REDIS_HOST")
  val redisPort = conf.getInt("REDIS_PORT")
  val redisChannel = conf.getString("REDIS_CHANNEL")
  val streamingBatchDuration = conf.getInt("STREAMING_BATCH_DURATION")
}
