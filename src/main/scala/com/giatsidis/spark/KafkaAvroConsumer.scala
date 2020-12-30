package com.giatsidis.spark

import java.time.Instant

import com.giatsidis.avro.Status
import com.giatsidis.spark.models.{Tweet, User}
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.generic.GenericData
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object KafkaAvroConsumer {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName(this.getClass.getSimpleName).setMaster("local[*]")

    val clientParams = Map[String, Object](
      "bootstrap.servers" -> Config.kafkaServers,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[KafkaAvroDeserializer],
      "auto.offset.reset" -> "latest",
      "group.id" -> "1",
      "enable.auto.commit" -> (false: java.lang.Boolean),
      "schema.registry.url" -> Config.avroSchemaRegistryUrl,
      "specific.avro.reader" -> (true: java.lang.Boolean)
    )

    val streamingContext = new StreamingContext(sparkConf, Seconds(2))

    streamingContext.sparkContext.setLogLevel("ERROR")

    val topics = Array(Config.kafkaTopic)

    val dStream = KafkaUtils.createDirectStream[String, Status](
      streamingContext,
      PreferConsistent,
      Subscribe[String, Status](topics, clientParams)
    )

    dStream.foreachRDD { rdd =>
      rdd
        .map { rdd: ConsumerRecord[String, Status] =>
          Tweet(
            rdd.value.getId,
            rdd.value.getFullText,
            null,
            "POSITIVE",
            Instant.parse(rdd.value.getCreatedAt),
            List(),
            User(
              rdd.value.getUserId,
              rdd.value.getUserScreenName,
              rdd.value.getUserProfileImageHttps,
            ),
            List()
          )
        }
        .foreachPartition { partition =>
          partition.foreach { line =>
            println(line)
          }
        }
    }

    streamingContext.start()
    streamingContext.awaitTermination()

  }

}
