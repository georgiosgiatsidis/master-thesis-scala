package com.giatsidis.spark

import com.typesafe.config.{Config => TypesafeConfig, ConfigFactory}

object Config {
  private val conf: TypesafeConfig = ConfigFactory.load()

  val consumerKey = conf.getString("CONSUMER_KEY")
  val consumerSecret = conf.getString("CONSUMER_SECRET")
  val accessToken = conf.getString("ACCESS_TOKEN")
  val accessTokenSecret = conf.getString("ACCESS_TOKEN_SECRET")
  val dbHost = conf.getString("DB_HOST")
  val dbPort = conf.getInt("DB_PORT")
  val dbName = conf.getString("DB_NAME")
  val dbUsername = conf.getString("DB_USERNAME")
  val dbPassword = conf.getString("DB_PASSWORD")
  val kafkaServers = conf.getString("KAFKA_SERVERS")
  val kafkaTopic = conf.getString("KAFKA_TOPIC")
  val avroSchemaRegistryUrl = conf.getString("AVRO_SCHEMA_REGISTRY_URL")
  val redisHost = conf.getString("REDIS_HOST")
  val redisPort = conf.getInt("REDIS_PORT")
  val redisChannel = conf.getString("REDIS_CHANNEL")
  val streamingBatchDuration = conf.getInt("STREAMING_BATCH_DURATION")
  val checkpointDirectory = conf.getString("CHECKPOINT_DIRECTORY")
  val trainingDataFilePath = conf.getString("TRAINING_DATA_FILE_PATH")
}
