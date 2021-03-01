package com.giatsidis.spark.services

import com.giatsidis.spark.Config
import com.giatsidis.database.Tables.{Hashtags, TweetHashtag, TweetHashtags, TweetTerm, TweetTerms, Tweets, Users}
import com.giatsidis.database.models.{Hashtag => HashtagRow, Tweet => TweetRow, User => UserRow}
import com.giatsidis.spark.models.Tweet
import com.giatsidis.spark.utils.TextUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.rdd.RDD

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import slick.jdbc.MySQLProfile.api._

object MysqlService {

  val log = Logger.getLogger(this.getClass)
  log.setLevel(Level.INFO)

  def save(rdd: RDD[Tweet]): Unit = {
    rdd.foreachPartition(partition => {
      val url = s"jdbc:mysql://${Config.dbHost}:${Config.dbPort}/${Config.dbName}?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8&useSSL=false";

      val db = Database.forURL(
        url,
        Config.dbUsername,
        Config.dbPassword,
        driver = "com.mysql.jdbc.Driver"
      )

      try {
        partition.foreach {
          record => {

            val usersQuery = TableQuery[Users].insertOrUpdate(
              UserRow(record.user.id, record.user.screenName, record.user.profileImageURLHttps)
            )
            Await.result(db.run(usersQuery), Duration.Inf)

            val tweetsQuery = TableQuery[Tweets] +=
              TweetRow(
                record.id,
                TextUtils.remove4ByteChars(record.fullText),
                record.location,
                record.sentiment,
                record.sentimentML,
                record.createdAt,
                Option(record.user.id)
              )

            Await.result(db.run(tweetsQuery), Duration.Inf)

            record.categories.foreach { category =>
              val tweetTermQuery = TableQuery[TweetTerms] += TweetTerm(0, record.id, category.id)
              Await.result(db.run(tweetTermQuery), Duration.Inf)
            }

            record.hashtags.foreach(hashtag => {
              val hashtagsQuery = TableQuery[Hashtags].insertOrUpdate(HashtagRow(0, hashtag.text))
              Await.result(db.run(hashtagsQuery), Duration.Inf)

              val hashtagQuery = TableQuery[Hashtags]
                .filter(_.text === hashtag.text)
                .map(_.id).result

              val hashtagId = Await.result(db.run(hashtagQuery), Duration.Inf).take(1).headOption

              if (hashtagId.isDefined) {
                val tweetHashtagsQuery = TableQuery[TweetHashtags] += TweetHashtag(0, record.id, hashtagId.get)
                Await.result(db.run(tweetHashtagsQuery), Duration.Inf)
              }

            })

          }
        }
      } catch {
        case e: Exception =>
          log.error(e)
      } finally {
        db.close
      }
    })
  }

}