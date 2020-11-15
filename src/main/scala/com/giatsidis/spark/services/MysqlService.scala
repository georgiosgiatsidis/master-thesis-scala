package com.giatsidis.spark.services

import com.giatsidis.spark.Config
import com.giatsidis.spark.Tables.{Tweets, Users, tweets, Tweet => TablesTweet, User => TablesUser}
import com.giatsidis.spark.models.Tweet
import org.apache.log4j.{Level, Logger}
import org.apache.spark.rdd.RDD

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import slick.jdbc.MySQLProfile.api._

import scala.collection.mutable.ArrayBuffer

object MysqlService {

  val log = Logger.getLogger(this.getClass)
  log.setLevel(Level.INFO)

  def save(rdd: RDD[Tweet]) = {
    rdd.foreachPartition(partition => {
      val db = Database.forURL(
        s"jdbc:mysql://${Config.dbHost}:${Config.dbPort}/${Config.dbName}",
        Config.dbUsername,
        Config.dbPassword,
      )

      val tweetsToInsert: ArrayBuffer[TablesTweet] = ArrayBuffer()
      val usersToInsert: ArrayBuffer[TablesUser] = ArrayBuffer()

      try {
        partition.foreach {
          record => {
            usersToInsert +=
              TablesUser(record.user.id, record.user.screenName, record.user.profileImageURLHttps)

            tweetsToInsert +=
              TablesTweet(
                record.id,
                record.fullText,
                record.location,
                record.sentiment,
                record.createdAt,
                Option(record.user.id)
              )
          }
        }
        val usersSequence = usersToInsert.toList.map(TableQuery[Users].insertOrUpdate(_))
        val tweetsSequence = tweetsToInsert.toList.map(TableQuery[Tweets].insertOrUpdate(_))
        val usersInserted = Await.result(db.run(DBIO.sequence(usersSequence)), Duration.Inf).sum
        val tweetsInserted = Await.result(db.run(DBIO.sequence(tweetsSequence)), Duration.Inf).sum
        log.info(s"Inserted ${usersInserted} users")
        log.info(s"Inserted ${tweetsInserted} tweets")
      } catch {
        case e: Exception =>
          log.error(e)
      } finally {
        db.close
      }
    })
  }

}