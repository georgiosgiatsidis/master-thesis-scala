package com.giatsidis.spark.services

import com.giatsidis.spark.Config
import com.giatsidis.spark.models.Tweet
import com.giatsidis.spark.utils.{InstantSerializer}
import org.apache.spark.rdd.RDD
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import redis.clients.jedis.Jedis

object RedisService {
  implicit val formats = DefaultFormats + InstantSerializer

  def save(rdd: RDD[Tweet]): Unit = {
    rdd.foreachPartition(partition => {
      partition.foreach(m => {
        val jedis = new Jedis(Config.redisHost, Config.redisPort)
        val pipeline = jedis.pipelined
        pipeline.publish(Config.redisChannel, Serialization.write(m))
        pipeline.sync()
      })
    })
  }
}
