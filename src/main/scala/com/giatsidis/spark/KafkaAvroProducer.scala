package com.giatsidis.spark

import java.util.Properties

import scala.collection.JavaConverters._
import com.giatsidis.avro.{Hashtag, Status, User}
import com.giatsidis.spark.utils.{Helpers, OAuthUtils}
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object KafkaAvroProducer {

  def main(args: Array[String]): Unit = {
    OAuthUtils.init()

    val sparkSession = SparkSession.builder
      .master(sys.env.getOrElse("SPARK_MASTER", "local[*]"))
      .appName(this.getClass.getSimpleName)
      .getOrCreate()

    val streamingContext = new StreamingContext(sparkSession.sparkContext, Seconds(Config.streamingBatchDuration))
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
          val location = Option(line.getGeoLocation).map(geo => {
            s"${geo.getLatitude},${geo.getLongitude}"
          })

          val status = Status.newBuilder()
            .setId(line.getId)
            .setFullText(line.getText)
            .setLocation(location.getOrElse(null))
            .setCreatedAt(line.getCreatedAt.toInstant.toString)
            .setUser(
              User.newBuilder()
                .setId(line.getUser.getId)
                .setScreenName(line.getUser.getScreenName)
                .setProfileImageHttps(line.getUser.getProfileImageURLHttps)
                .build()
            )
            .setHashtags(
              line.getHashtagEntities
                .toList
                .map(h => Hashtag.newBuilder().setText(h.getText).build())
                .asJava
            )
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
