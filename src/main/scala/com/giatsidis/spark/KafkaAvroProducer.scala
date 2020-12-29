package com.giatsidis.spark

import java.io.File
import java.util.Properties

import com.giatsidis.spark.utils.OAuthUtils
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
      val savedRdd = rdd
        .filter(_.getLang == "en")
        // Filter retweets
        .filter(!_.isRetweet)
        // Filter tweets with big number of hashtags
        .filter(_.getHashtagEntities.toList.length < 5)
        // Filter tweets with short content length
        .filter(_.getText.length > 20)

      val writerTopic = "tweets1"

      savedRdd.foreachPartition { partition =>
        val producerProps = new Properties()

        producerProps.put("bootstrap.servers", "localhost:9092")
        producerProps.put("key.serializer", classOf[StringSerializer])
        producerProps.put("value.serializer", classOf[KafkaAvroSerializer])
        producerProps.put("schema.registry.url", "http://localhost:8081")

        val producer = new KafkaProducer[String, GenericData.Record](producerProps)
        val schemaParser = new Parser
        val schema = schemaParser.parse(new File("src/main/avro/status.avsc"))

        partition.foreach { line =>
          val record = new GenericData.Record(schema)
          record.put("name", "name")
          record.put("text", line.getText)
          val producerRecord = new ProducerRecord[String, GenericData.Record](writerTopic, record)
          producer.send(producerRecord)
        }

      }

    }

    streamingContext.start()
    streamingContext.awaitTermination()

  }

}
