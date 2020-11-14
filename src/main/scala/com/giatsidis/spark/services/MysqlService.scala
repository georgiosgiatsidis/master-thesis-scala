package com.giatsidis.spark.services

import com.giatsidis.spark.Tables.{Tweets, Users, Tweet => TablesTweet, User => TablesUsers}
import com.giatsidis.spark.models.Tweet
import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import slick.jdbc.MySQLProfile.api._

object MysqlService {

  val log = Logger.getLogger(this.getClass)

  def save(rdd: RDD[Tweet]) = {
    rdd.foreachPartition(partition => {
      val db = Database.forConfig("mysql")

      try {
        partition.foreach {
          record => {
            val users = TableQuery[Users].insertOrUpdate(TablesUsers(record.user.id, record.user.screenName, record.user.profileImageURLHttps))
            Await.result(db.run(users), Duration.Inf)
            val tweets = TableQuery[Tweets].insertOrUpdate(
              TablesTweet(
                record.id,
                record.fullText,
                record.location,
                record.sentiment,
                record.createdAt,
                Option(record.user.id)
              )
            )
            Await.result(db.run(tweets), Duration.Inf)
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