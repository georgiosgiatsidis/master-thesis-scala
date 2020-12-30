package com.giatsidis.spark

import java.io.File
import java.util.Properties

import com.giatsidis.avro.Status
import com.giatsidis.spark.utils.{Helpers, OAuthUtils}
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.Schema.Parser
import org.apache.avro.generic.GenericData
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object KafkaAvroProducer {

  def main(args: Array[String]): Unit = {
    OAuthUtils.init()
    val sparkConf = new SparkConf().setAppName(this.getClass.getSimpleName).setMaster("local[*]")
    val streamingContext = new StreamingContext(sparkConf, Seconds(Config.streamingBatchDuration))
    streamingContext.sparkContext.setLogLevel("ERROR")
    val filters = Array("Bitcoin", "BTC", "Ethereum", "ETH", "XRP", "Tether", "Litecoin");
    val tweets = TwitterUtils.createStream(streamingContext, None, filters)

    tweets.foreachRDD { rdd =>
      val savedRdd = Helpers.applyFilters(rdd)

      savedRdd.foreachPartition { partition =>
        val producerProps = new Properties()

        producerProps.put("bootstrap.servers", Config.kafkaServers)
        producerProps.put("key.serializer", classOf[StringSerializer])
        producerProps.put("value.serializer", classOf[KafkaAvroSerializer])
        producerProps.put("schema.registry.url", Config.avroSchemaRegistryUrl)

        val producer = new KafkaProducer[String, Status](producerProps)

        partition.foreach { line =>
          val status = Status.newBuilder()
            .setId(line.getId)
            .setFullText(line.getText)
            .setCreatedAt(line.getCreatedAt.toInstant.toString)
            .setUserId(line.getUser.getId)
            .setUserScreenName(line.getUser.getScreenName)
            .setUserProfileImageHttps(line.getUser.getProfileImageURLHttps)
            .build()
          val producerRecord = new ProducerRecord[String, Status](Config.kafkaTopic, status)
          producer.send(producerRecord)
        }

      }

    }

    streamingContext.start()
    streamingContext.awaitTermination()

  }

}
