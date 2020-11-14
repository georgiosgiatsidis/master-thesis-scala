package com.giatsidis.spark.services

import java.sql.{DriverManager, PreparedStatement}

import com.giatsidis.spark.Config
import com.giatsidis.spark.Tables.{Tweets, User, Users}
import com.giatsidis.spark.models.Tweet
import org.apache.log4j.Logger

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import slick.jdbc.MySQLProfile.api._

object MysqlService extends Serializable {

  @transient lazy val log = Logger.getLogger(this.getClass)

  def save(partition: Iterator[Tweet]) = {

    Class.forName("com.mysql.jdbc.Driver")
    val connection = DriverManager.getConnection(
      s"jdbc:mysql://${Config.dbHost}:${Config.dbPort}/${Config.dbName}",
      Config.dbUsername,
      Config.dbPassword
    )

    val sql = "INSERT INTO tweets (id, full_text, location, sentiment, created_at, user_id) VALUES (?, ?, ?, ?, ?, ?)"
    val preparedStatement: PreparedStatement = connection.prepareStatement(sql)
    val db = Database.forConfig("mysql")

    try {
      connection.setAutoCommit(false)
      partition.foreach(record => {

        val f = {
          val users: TableQuery[Users] = TableQuery[Users]
          val updated = users.insertOrUpdate(User(record.user.id, record.user.screenName, record.user.profileImageURLHttps))
          db.run(updated)
        }
        Await.result(f, Duration.Inf)

        preparedStatement.setLong(1, record.id)
        preparedStatement.setString(2, record.fullText)
        preparedStatement.setString(3, if (record.location.isEmpty) null else record.location.get)
        preparedStatement.setString(4, record.sentiment)
        preparedStatement.setString(5, record.createdAt)
        preparedStatement.setLong(6, record.user.id)

        preparedStatement.addBatch()
      })
      preparedStatement.executeBatch()
      connection.commit
    } catch {
      case e: Exception =>
        log.error(e)
    } finally {
      connection.close()
      preparedStatement.close()
      db.close
    }
  }

}