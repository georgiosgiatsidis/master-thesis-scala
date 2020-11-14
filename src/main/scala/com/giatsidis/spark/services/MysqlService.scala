package com.giatsidis.spark.services

import java.sql.{DriverManager, PreparedStatement}

import com.giatsidis.spark.Config
import com.giatsidis.spark.models.Tweet
import org.apache.log4j.{Logger}

object MysqlService extends Serializable {

  @transient lazy val log = Logger.getLogger(this.getClass)

  def save(partition: Iterator[Tweet]) = {
    Class.forName("com.mysql.jdbc.Driver").newInstance
    val connection = DriverManager.getConnection(
      s"jdbc:mysql://${Config.dbHost}:${Config.dbPort}/${Config.dbName}",
      Config.dbUsername,
      Config.dbPassword
    )

    val preparedStatement: PreparedStatement = connection.prepareStatement("INSERT INTO tweets (full_text) VALUES (?)")
    try {
      connection.setAutoCommit(false)
      partition.foreach(record => {
        preparedStatement.setString(1, record.fullText)
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
    }
  }

}