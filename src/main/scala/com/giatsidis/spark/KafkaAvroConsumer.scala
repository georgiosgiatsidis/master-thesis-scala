package com.giatsidis.spark

import java.time.Instant

import collection.JavaConverters._
import com.giatsidis.avro.Status
import com.giatsidis.repositories.TermRepository
import com.giatsidis.spark.models.{Hashtag, Term, Tweet, User}
import com.giatsidis.spark.sentiment.mllib.MLlibSentimentAnalyzer
import com.giatsidis.spark.sentiment.stanford.StanfordSentimentAnalyzer
import com.giatsidis.spark.services.{MysqlService, RedisService}
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object KafkaAvroConsumer {
  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder
      .master(sys.env.getOrElse("SPARK_MASTER", "local[*]"))
      .appName(this.getClass.getSimpleName)
      .getOrCreate()

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

    val streamingContext = new StreamingContext(sparkSession.sparkContext, Seconds(2))

    streamingContext.sparkContext.setLogLevel("ERROR")

    val topics = Array(Config.kafkaTopic)

    val tweets = KafkaUtils.createDirectStream[String, Status](
      streamingContext,
      PreferConsistent,
      Subscribe[String, Status](topics, clientParams)
    )

    val model = MLlibSentimentAnalyzer.createNBModel(streamingContext.sparkContext)

    val terms = Await.result(TermRepository.getAll(), Duration.Inf)

    tweets.foreachRDD { rdd =>
      val savedRdd = rdd
        .map { rdd: ConsumerRecord[String, Status] =>
          Tweet(
            rdd.value.getId,
            rdd.value.getFullText,
            Option(rdd.value.getLocation),
            StanfordSentimentAnalyzer.detectSentiment(rdd.value.getFullText).toString,
            MLlibSentimentAnalyzer.detectSentiment(rdd.value.getFullText, model).toString,
            Instant.parse(rdd.value.getCreatedAt),
            rdd.value.getHashtags.asScala.toList
              .map(h => Hashtag(h.getText)),
            User(
              rdd.value.getUser.getId,
              rdd.value.getUser.getScreenName,
              rdd.value.getUser.getProfileImageHttps,
            ),
            terms.filter { term =>
              val keywords = term.keywords.split(",")
              keywords.exists(keyword => rdd.value.getFullText.toLowerCase.contains(keyword.toLowerCase()))
            }.map(t => Term(t.id.get, t.name))
          )
        }

      MysqlService.save(savedRdd)
      RedisService.save(savedRdd)
    }

    streamingContext.start()
    streamingContext.awaitTermination()

  }

}
